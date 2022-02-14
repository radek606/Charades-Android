package com.ick.kalambury

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ick.kalambury.MainMenuNavigationActions.*
import com.ick.kalambury.prompt.Prompt
import com.ick.kalambury.prompt.PromptManager
import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
    private val preferenceStorage: MainPreferenceStorage,
    private val networkUtils: NetworkUtils,
    promptManager: PromptManager,
) : BaseViewModel<MainMenuNavigationActions>() {

    private val _prompt: MutableLiveData<Event<Prompt>> = MutableLiveData()
    val prompt: LiveData<Event<Prompt>> = _prompt

    private var nickname: String = ""
    private var currentOption: MainMenuOptions? = null

    init {
        disposables += promptManager.getPrompt()
            .subscribe { success -> _prompt.postValue(Event(success)) }

        disposables += preferenceStorage.nickname.firstOrError()
            .subscribe { s -> nickname = s }
    }

    fun onOption(option: MainMenuOptions) {
        currentOption = option
        when (option) {
            MainMenuOptions.GENERATOR -> _navigationActions.value = Event(NavigateToCreateGame(GameMode.SHOWING))
            MainMenuOptions.SETTINGS -> _navigationActions.value = Event(NavigateToSettings)
            MainMenuOptions.HELP -> _navigationActions.value = Event(NavigateToHelp)
            else -> checkNicknameAndNavigate(option)
        }
    }

    private fun checkNicknameAndNavigate(option: MainMenuOptions) {
        if (nickname.isNotBlank()) {
            navigate(option)
        } else {
            _navigationActions.value = Event(NavigateToNicknameDialogAction)
        }
    }

    fun onNickname(nickname: String) {
        this.nickname = nickname
        preferenceStorage.setNickname(nickname)
        currentOption?.let { navigate(it) }
    }

    fun onPermissionRequestResult() {
        currentOption?.let { navigate(it) }
    }

    private fun navigate(option: MainMenuOptions) {
        when(option) {
            MainMenuOptions.CREATE_LOCAL -> _navigationActions.value = Event(NavigateToCreateGame(GameMode.DRAWING_LOCAL))
            MainMenuOptions.JOIN_LOCAL -> _navigationActions.value = Event(NavigateToJoinLocalGame)
            MainMenuOptions.JOIN_ONLINE -> {
                if (networkUtils.hasNetworkConnection()) {
                    _navigationActions.value = Event(NavigateToJoinOnlineGame)
                } else {
                    _snackbarMessage.value = Event(R.string.alert_no_internet)
                }
            }
            else -> throw UnsupportedOperationException("Unsupported option: $option")
        }
    }

}

sealed class MainMenuNavigationActions {
    object NavigateToNicknameDialogAction : MainMenuNavigationActions()
    class NavigateToCreateGame(val gameMode: GameMode) : MainMenuNavigationActions()
    object NavigateToJoinLocalGame : MainMenuNavigationActions()
    object NavigateToJoinOnlineGame : MainMenuNavigationActions()
    object NavigateToSettings : MainMenuNavigationActions()
    object NavigateToHelp : MainMenuNavigationActions()
}