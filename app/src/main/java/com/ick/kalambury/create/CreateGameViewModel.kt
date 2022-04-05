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
import com.ick.kalambury.util.SchedulerProvider
import com.ick.kalambury.words.InstanceId
import com.ick.kalambury.wordsrepository.Language
import com.ick.kalambury.wordsrepository.WordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

@HiltViewModel
class CreateGameViewModel @Inject constructor(
    private val preferenceStorage: MainPreferenceStorage,
    private val wordsRepository: WordsRepository,
    private val gameHandlerRepository: GameHandlerRepository,
    private val schedulers: SchedulerProvider,
    stateHandle: SavedStateHandle,
) : BaseViewModel<CreateGameNavigationActions>() {

    val gameMode: GameMode = checkNotNull(stateHandle.get("gameMode")) { "No game mode provided" }

    private val _setsData: MutableLiveData<List<WordsSetData>> = MutableLiveData()

    val availableSets: LiveData<List<WordsSetData>> = Transformations.map(_setsData) {
        it.sortedWith(
            compareByDescending(WordsSetData::new)
            .thenByDescending(WordsSetData::updated)
            .thenBy(WordsSetData::text)
        )
    }

    val selectedSets: LiveData<List<WordsSetData>> = Transformations.map(_setsData) {
        it.filter(WordsSetData::selected)
            .sortedWith(compareBy(WordsSetData::text))
    }

    val hasNewSets: LiveData<Boolean> =
        Transformations.map(_setsData) { it.any(WordsSetData::new) }

    private val _incompatibleVersion: MutableLiveData<Event<SupportedVersionInfo>> =
        MutableLiveData()
    val incompatibleVersion: LiveData<Event<SupportedVersionInfo>> = _incompatibleVersion

    private val _createInProgress: MutableLiveData<Boolean> = MutableLiveData(false)
    val createInProgress: LiveData<Boolean> = Transformations.distinctUntilChanged(_createInProgress)

    val playerChooseMethodId: MutableLiveData<Int> = MutableLiveData(0)
    val roundLength: MutableLiveData<Int> = MutableLiveData(0)
    val pointsLimit: MutableLiveData<Int> = MutableLiveData(0)

    private var playerChooseMethod: PlayerChooseMethod = PlayerChooseMethod.GUESSING_PLAYER
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
            .subscribe { method ->
                playerChooseMethodId.postValue(method.ordinal)
                playerChooseMethod = method
            }

        disposables += preferenceStorage.wordsLanguage
            .firstOrError()
            .doOnSuccess { language = it }
            .flatMapObservable { wordsRepository.getSetsData(gameMode.wordsSetUsage, it, InstanceId(gameMode, language)) }
            .map(::WordsSetData)
            .toList()
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

        wordsRepository.saveSelectedSetIds(InstanceId(gameMode, language), items)
            .andThen(wordsRepository.getSetsData(gameMode.wordsSetUsage, language,
                InstanceId(gameMode, language)
            ))
            .map(::WordsSetData)
            .toList()
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
        }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.main())
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
        return wordsRepository.prepareWordsInstance(InstanceId(gameMode, language), categories)
    }

    private fun startLocalGame(): Completable {
        val handler = gameHandlerRepository.createHostHandler(getGameConfig())
        return preferenceStorage.localUserData
            .firstOrError()
            .flatMapCompletable { handler.connect(it) }
            .andThen(wordsRepository.prepareWordsInstance(InstanceId(gameMode, language), categories))

    }

    private fun startOnlineGame(): Completable {
        val handler = gameHandlerRepository.createHostHandler(getGameConfig())
        disposables += handler.getGameEvents()
            .observeOn(schedulers.main())
            .subscribe(::handleGameEvent)

        return preferenceStorage.localUserData
            .firstOrError()
            .flatMapCompletable { handler.connect(it) }
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