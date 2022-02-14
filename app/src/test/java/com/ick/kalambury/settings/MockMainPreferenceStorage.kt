package com.ick.kalambury.settings

import com.ick.kalambury.BuildConfig
import com.ick.kalambury.GameMode
import com.ick.kalambury.PlayerChooseMethod
import com.ick.kalambury.model.TestData
import com.ick.kalambury.net.connection.User
import com.ick.kalambury.words.Language
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import java.util.*

class MockMainPreferenceStorage(
    nickname: String = "FakePlayer1",
    uuid: String = UUID.randomUUID().toString(),
    password: String? = null,
    localUserData: User = User(uuid, uuid, nickname),
    wordsLanguage: Language = Language.EN,
    chatSize: Int = 5,
    showCategoryName: Boolean = true,
    vibrationEnabled: Boolean = true,
    sendUsageStatistics: Boolean = true,
    firstRun: Boolean = false,
    firstInstallTime: Long = 0L,
    firstInstallVersion: Int = BuildConfig.VERSION_CODE,
    appCrashed: Boolean = false,
    rateAppNextPromptTime: Long = 0L,
    lastGameWithoutError: Boolean = true,
    appUpdateInProgress: Boolean = false,
    appUpdateNextPromptTime: Long = 0L,
    roundLength: Int = 120,
    pointsLimit: Int = 5,
    drawingPlayerChooseMethod: PlayerChooseMethod = PlayerChooseMethod.GUESSING_PLAYER,
    selectedSets: List<String> = listOf(TestData.set2Id),
) : MainPreferenceStorage {

    private var _nickname = Flowable.just(nickname)
    override val nickname: Flowable<String> = _nickname
    override fun setNickname(nickname: String) {
        _nickname = Flowable.just(nickname)
    }

    private var _uuid = Flowable.just(uuid)
    override val uuid: Flowable<String> = _uuid
    override fun setUUID(uuid: String) {
        _uuid = Flowable.just(uuid)
    }

    private var _password = Flowable.just(password ?: "")
    override val password: Flowable<String> = _password
    override fun setPassword(password: String?) {
        _password = Flowable.just(password ?: "")
    }

    override val localUserData: Flowable<User> = Flowable.just(localUserData)

    private var _wordsLanguage = Flowable.just(wordsLanguage)
    override val wordsLanguage: Flowable<Language> = _wordsLanguage
    override fun setWordsLanguage(language: Language) {
        _wordsLanguage = Flowable.just(language)
    }

    private var _chatSize = Flowable.just(chatSize)
    override val chatSize: Flowable<Int> = _chatSize
    override fun setChatSize(size: Int) {
        _chatSize = Flowable.just(size)
    }

    private var _showCategoryName = Flowable.just(showCategoryName)
    override val showCategoryName: Flowable<Boolean> = _showCategoryName
    override fun setShowCategoryName(show: Boolean) {
        _showCategoryName = Flowable.just(show)
    }

    private var _vibrationEnabled = Flowable.just(vibrationEnabled)
    override val vibrationEnabled: Flowable<Boolean> = _vibrationEnabled
    override fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled = Flowable.just(enabled)
    }

    private var _sendUsageStatistics = Flowable.just(sendUsageStatistics)
    override val sendUsageStatistics: Flowable<Boolean> = _sendUsageStatistics
    override fun setSendUsageStatistics(enabled: Boolean) {
        _sendUsageStatistics = Flowable.just(enabled)
    }

    private var _firstRun = Flowable.just(firstRun)
    override val firstRun: Flowable<Boolean> = _firstRun
    override fun setFirstRun(firstRun: Boolean) {
        _firstRun = Flowable.just(firstRun)
    }

    private var _firstInstallTime = Flowable.just(firstInstallTime)
    override val firstInstallTime: Flowable<Long> = _firstInstallTime
    override fun setFirstInstallTime(firstInstallTime: Long) {
        _firstInstallTime = Flowable.just(firstInstallTime)
    }

    private var _firstInstallVersion = Flowable.just(firstInstallVersion)
    override val firstInstallVersion: Flowable<Int> = _firstInstallVersion
    override fun setFirstInstallVersion(firstInstallVersion: Int) {
        _firstInstallVersion = Flowable.just(firstInstallVersion)
    }

    private var _appCrashed = Flowable.just(appCrashed)
    override val appCrashed: Flowable<Boolean> = _appCrashed
    override fun setAppCrashed(crashed: Boolean) {
        _appCrashed = Flowable.just(crashed)
    }

    private var _rateAppNextPromptTime = Flowable.just(rateAppNextPromptTime)
    override val rateAppNextPromptTime: Flowable<Long> = _rateAppNextPromptTime
    override fun setRateAppNextPromptTime(time: Long) {
        _rateAppNextPromptTime = Flowable.just(time)
    }

    private var _lastGameWithoutError = Flowable.just(lastGameWithoutError)
    override val lastGameWithoutError: Flowable<Boolean> = _lastGameWithoutError
    override fun setLastGameWithoutError(state: Boolean) {
        _lastGameWithoutError = Flowable.just(state)
    }

    private var _appUpdateInProgress = Flowable.just(appUpdateInProgress)
    override val appUpdateInProgress: Flowable<Boolean> = _appUpdateInProgress
    override fun setAppUpdateInProgress(state: Boolean) {
        _appUpdateInProgress = Flowable.just(state)
    }

    private var _appUpdateNextPromptTime = Flowable.just(appUpdateNextPromptTime)
    override val appUpdateNextPromptTime: Flowable<Long> = _appUpdateNextPromptTime
    override fun setAppUpdateNextPromptTime(time: Long) {
        _appUpdateNextPromptTime = Flowable.just(time)
    }

    private var _roundLength = Flowable.just(roundLength)
    override fun getRoundLength(gameMode: GameMode): Flowable<Int> = _roundLength
    override fun setRoundLength(gameMode: GameMode, length: Int) {
        _roundLength = Flowable.just(length)
    }

    private var _pointsLimit = Flowable.just(pointsLimit)
    override fun getPointsLimit(gameMode: GameMode): Flowable<Int> = _pointsLimit
    override fun setPointsLimit(gameMode: GameMode, points: Int) {
        _pointsLimit = Flowable.just(points)
    }

    private var _drawingPlayerChooseMethod = Flowable.just(drawingPlayerChooseMethod)
    override fun getDrawingPlayerChooseMethod(gameMode: GameMode): Flowable<PlayerChooseMethod> =
        _drawingPlayerChooseMethod

    override fun setDrawingPlayerChooseMethod(gameMode: GameMode, method: PlayerChooseMethod) {
        _drawingPlayerChooseMethod = Flowable.just(method)
    }

    private var _selectedSets = Flowable.just(selectedSets)

    override fun getSelectedWordsSets(instanceId: String): Flowable<List<String>> = _selectedSets
    override fun setSelectedWordsSets(instanceId: String, sets: List<String>): Completable {
        _selectedSets = Flowable.just(sets)
        return Completable.complete()
    }

}