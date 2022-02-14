package com.ick.kalambury.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import com.ick.kalambury.*
import com.ick.kalambury.GameMode.*
import com.ick.kalambury.create.CreateGameNavigationActions.*
import com.ick.kalambury.list.model.WordsSetData
import com.ick.kalambury.net.connection.SupportedVersionInfo
import com.ick.kalambury.service.GameEvent
import com.ick.kalambury.service.GameHandlerRepository
import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.words.Language
import com.ick.kalambury.words.WordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class CreateGameViewModel @Inject constructor(
    private val preferenceStorage: MainPreferenceStorage,
    private val wordsRepository: WordsRepository,
    private val gameHandlerRepository: GameHandlerRepository,
    stateHandle: SavedStateHandle,
) : BaseViewModel<CreateGameNavigationActions>() {

    val gameMode: GameMode =
        stateHandle.get("gameMode") ?: throw IllegalArgumentException("No game mode provided")

    private val _setsData: MutableLiveData<List<WordsSetData>> = MutableLiveData()

    val availableSets: LiveData<List<WordsSetData>> = Transformations.map(_setsData) {
        it.sortedWith(Comparator.comparing(WordsSetData::new)
                    .thenComparing(WordsSetData::updated).reversed()
                    .thenComparing(WordsSetData::text))
    }

    val selectedSets: LiveData<List<WordsSetData>> = Transformations.map(_setsData) {
        it.filter(WordsSetData::selected)
            .sortedWith(Comparator.comparing(WordsSetData::text))
    }

    val hasNewSets: LiveData<Boolean> =
        Transformations.map(_setsData) { it.any(WordsSetData::new) }

    private val _incompatibleVersion: MutableLiveData<Event<SupportedVersionInfo>> =
        MutableLiveData()
    val incompatibleVersion: LiveData<Event<SupportedVersionInfo>> = _incompatibleVersion

    private val _createInProgress: MutableLiveData<Boolean> = MutableLiveData(false)
    val createInProgress: LiveData<Boolean> =
        Transformations.distinctUntilChanged(_createInProgress)

    val playerChooseMethodId: MutableLiveData<Int> =
        MutableLiveData(PlayerChooseMethod.GUESSING_PLAYER.ordinal)
    val playerChooseMethod = PlayerChooseMethod.values()[playerChooseMethodId.value!!]
    val roundLength: MutableLiveData<Int> = MutableLiveData(0)
    val pointsLimit: MutableLiveData<Int> = MutableLiveData(0)

    private var language: Language = Language.EN
    private var categories: List<String> = listOf()

    init {
        disposables += preferenceStorage.getRoundLength(gameMode)
            .firstOrError()
            .subscribe(roundLength::postValue)

        disposables += preferenceStorage.getPointsLimit(gameMode)
            .firstElement()
            .subscribe(pointsLimit::postValue)

        disposables += preferenceStorage.getDrawingPlayerChooseMethod(gameMode)
            .firstElement()
            .subscribe { method -> playerChooseMethodId.postValue(method.ordinal) }

        disposables += preferenceStorage.wordsLanguage
            .firstOrError()
            .doOnSuccess { language = it }
            .flatMap { wordsRepository.getSetsData(gameMode, it) }
            .doOnSuccess { categories = it.filter(WordsSetData::selected).map(WordsSetData::id) }
            .subscribe(_setsData::postValue)
    }

    fun onEdit() {
        _navigationActions.value = Event(NavigateToSelectCategories)
    }

    fun onCategoriesSelected(items: List<String>): Boolean {
        if (items.isEmpty()) {
            _snackbarMessage.value = Event(R.string.alert_choose_categories)
            return false
        }

        categories = items

        wordsRepository.saveSelectedSetIds(gameMode, language, items)
            .toSingleDefault(true)
            .flatMap { wordsRepository.getSetsData(gameMode, language) }
            .subscribe(_setsData::postValue)

        return true
    }

    fun onCompleted() {
        _createInProgress.value = true
        saveValues()
        when (gameMode) {
            SHOWING -> startShowingGame()
            DRAWING_LOCAL -> startLocalGame()
            DRAWING_ONLINE -> startOnlineGame()
            else -> throw IllegalArgumentException("Unsupported game mode: $gameMode")
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
            {
                when(gameMode) {
                    SHOWING -> {
                        _createInProgress.value = false
                        _navigationActions.value = Event(NavigateToShowingFragment(getGameConfig()))
                    }
                    DRAWING_LOCAL -> {
                        _createInProgress.value = false
                        _navigationActions.value = Event(NavigateToGameFragment)
                    }
                    DRAWING_ONLINE -> {
                    /* for game mode DRAWING_ONLINE we must wait for connection result returned as GameEvent */
                    }
                    else -> throw IllegalArgumentException("Unsupported game mode: $gameMode")
                }
            },
            {
                _createInProgress.value = false
                _snackbarMessage.value = Event(R.string.alert_create_game_failed)
            }
        )
    }

    private fun saveValues() {
        roundLength.value?.let { preferenceStorage.setRoundLength(gameMode, it) }
        pointsLimit.value?.let { preferenceStorage.setPointsLimit(gameMode, it) }
        preferenceStorage.setDrawingPlayerChooseMethod(gameMode, playerChooseMethod)
    }

    private fun startShowingGame(): Completable {
        return wordsRepository.prepareWordsInstance(gameMode, language, categories)
    }

    private fun startLocalGame(): Completable {
        gameHandlerRepository.createHostHandler(getGameConfig())
        return wordsRepository.prepareWordsInstance(gameMode, language, categories)
    }

    private fun startOnlineGame(): Completable {
        val handler = gameHandlerRepository.createHostHandler(getGameConfig())
        disposables += handler.getGameEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleGameEvent)

        return handler.connect()
    }

    private fun handleGameEvent(event: GameEvent) {
        _createInProgress.value = false
        when (event.state) {
            GameEvent.State.CONNECTED -> {
                _navigationActions.value = Event(NavigateToGameFragment)
            }
            GameEvent.State.UNSUPPORTED_VERSION -> _incompatibleVersion.value =
                Event(event.supportedVersionInfo!!)
            else -> _snackbarMessage.value = Event(R.string.alert_create_game_failed)
        }
    }

    private fun getGameConfig() = GameConfig(
        gameMode = gameMode,
        isHost = true,
        roundTime = roundLength.value!!,
        pointsLimit = pointsLimit.value!!,
        playerChooseMethod = playerChooseMethod,
        language = language,
        categories = categories
    )

}

sealed class CreateGameNavigationActions {
    class NavigateToShowingFragment(val gameConfig: GameConfig) : CreateGameNavigationActions()
    object NavigateToGameFragment : CreateGameNavigationActions()
    object NavigateToSelectCategories : CreateGameNavigationActions()
}