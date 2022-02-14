package com.ick.kalambury.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ick.kalambury.BaseViewModel
import com.ick.kalambury.Event
import com.ick.kalambury.R
import com.ick.kalambury.words.Language
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mainPreferenceStorage: MainPreferenceStorage,
) : BaseViewModel<Unit>() {

    private val _nickname: MutableLiveData<String> = MutableLiveData()
    val nickname: LiveData<String> = _nickname

    private val _language: MutableLiveData<String> = MutableLiveData()
    val language: LiveData<String> = _language

    private val _chatSize: MutableLiveData<Int> = MutableLiveData()
    val chatSize: LiveData<Int> = _chatSize

    private val _notifications: MutableLiveData<Boolean> = MutableLiveData()
    val notifications: LiveData<Boolean> = _notifications

    private val _statistics: MutableLiveData<Boolean> = MutableLiveData()
    val statistics: LiveData<Boolean> = _statistics

    init {
        loadData()
    }

    private fun loadData() {
        disposables += mainPreferenceStorage.nickname
            .subscribe(_nickname::postValue)

        disposables += mainPreferenceStorage.wordsLanguage
            .subscribe { _language.postValue(it.asString()) }

        disposables += mainPreferenceStorage.chatSize
            .subscribe(_chatSize::postValue)

        disposables += mainPreferenceStorage.vibrationEnabled
            .subscribe(_notifications::postValue)

        disposables += mainPreferenceStorage.sendUsageStatistics
            .subscribe(_statistics::postValue)
    }

    fun onNickname(nickname: String?) {
        if (nickname.isNullOrEmpty()) {
            _snackbarMessage.value = Event(R.string.alert_empty_nickname)
        } else {
            mainPreferenceStorage.setNickname(nickname)
        }
    }

    fun onLanguage(language: String) {
        mainPreferenceStorage.setWordsLanguage(Language.forLanguageName(language))
    }

    fun onChatSize(size: Int) {
        mainPreferenceStorage.setChatSize(size)
    }

    fun onVibrationNotification(enabled: Boolean) {
        mainPreferenceStorage.setVibrationEnabled(enabled)
    }

    fun onSendUsageStatistics(enabled: Boolean) {
        mainPreferenceStorage.setSendUsageStatistics(enabled)
    }

}