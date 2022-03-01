package com.ick.kalambury.settings

import com.ick.kalambury.GameMode
import com.ick.kalambury.PlayerChooseMethod
import com.ick.kalambury.net.connection.User
import com.ick.kalambury.util.settings.DataStoreWrapper
import com.ick.kalambury.util.settings.PreferencesStorage
import com.ick.kalambury.wordsrepository.Language
import com.ick.kalambury.wordsrepository.Language.Companion.forLanguageName
import io.reactivex.rxjava3.core.Flowable
import java.util.*

interface MainPreferenceStorage {
    val nickname: Flowable<String>
    fun setNickname(nickname: String)

    val uuid: Flowable<String>
    fun setUUID(uuid: String)

    val password: Flowable<String>
    fun setPassword(password: String?)

    val localUserData: Flowable<User>

    val wordsLanguage: Flowable<Language>
    fun setWordsLanguage(language: Language)

    val chatSize: Flowable<Int>
    fun setChatSize(size: Int)

    val showCategoryName: Flowable<Boolean>
    fun setShowCategoryName(show: Boolean)

    val vibrationEnabled: Flowable<Boolean>
    fun setVibrationEnabled(enabled: Boolean)

    val sendUsageStatistics: Flowable<Boolean>
    fun setSendUsageStatistics(enabled: Boolean)

    val firstRun: Flowable<Boolean>
    fun setFirstRun(firstRun: Boolean)

    val firstInstallTime: Flowable<Long>
    fun setFirstInstallTime(firstInstallTime: Long)

    val firstInstallVersion: Flowable<Int>
    fun setFirstInstallVersion(firstInstallVersion: Int)

    val appCrashed: Flowable<Boolean>
    fun setAppCrashed(crashed: Boolean)

    val rateAppNextPromptTime: Flowable<Long>
    fun setRateAppNextPromptTime(time: Long)

    val lastGameWithoutError: Flowable<Boolean>
    fun setLastGameWithoutError(state: Boolean)

    val appUpdateInProgress: Flowable<Boolean>
    fun setAppUpdateInProgress(state: Boolean)

    val appUpdateNextPromptTime: Flowable<Long>
    fun setAppUpdateNextPromptTime(time: Long)

    fun getRoundLength(gameMode: GameMode): Flowable<Int>
    fun setRoundLength(gameMode: GameMode, length: Int)

    fun getPointsLimit(gameMode: GameMode): Flowable<Int>
    fun setPointsLimit(gameMode: GameMode, points: Int)

    fun getDrawingPlayerChooseMethod(gameMode: GameMode): Flowable<PlayerChooseMethod>
    fun setDrawingPlayerChooseMethod(gameMode: GameMode, method: PlayerChooseMethod)

}

class MainPreferenceStorageImpl(
    private val dataStore: DataStoreWrapper,
    private val keys: PreferenceKeys,
) : MainPreferenceStorage, PreferencesStorage by dataStore {

    override val nickname: Flowable<String>
        get() = getValue(keys.nickname, "")
    override fun setNickname(nickname: String) {
        setValue(keys.nickname, nickname)
    }

    override val uuid: Flowable<String>
        get() = getValue(keys.userUuid, UUID.randomUUID().toString())
    override fun setUUID(uuid: String) {
        setValue(keys.userUuid, uuid)
    }

    override val password: Flowable<String>
        get() = getValue(keys.password, "")
    override fun setPassword(password: String?) {
        setValue(keys.password, password)
    }

    override val localUserData: Flowable<User>
        get() = Flowable.zip(uuid, nickname) { u, n -> User(u, u, n) }

    override val wordsLanguage: Flowable<Language>
        get() = dataStore.data().map { forLanguageName(it[keys.wordsLanguage] ?: "") }
    override fun setWordsLanguage(language: Language) {
        setValue(keys.wordsLanguage, language.toString())
    }

    override val chatSize: Flowable<Int>
        get() = getValue(keys.chatSize, 5)
    override fun setChatSize(size: Int) {
        setValue(keys.chatSize, size)
    }

    override val showCategoryName: Flowable<Boolean>
        get() = getValue(keys.showCategoryName, false)
    override fun setShowCategoryName(show: Boolean) {
        setValue(keys.showCategoryName, show)
    }

    override val vibrationEnabled: Flowable<Boolean>
        get() = getValue(keys.vibrationNotificationEnabled, false)
    override fun setVibrationEnabled(enabled: Boolean) {
        setValue(keys.vibrationNotificationEnabled, enabled)
    }

    override val sendUsageStatistics: Flowable<Boolean>
        get() = getValue(keys.sendUsageStatistics, true)
    override fun setSendUsageStatistics(enabled: Boolean) {
        setValue(keys.sendUsageStatistics, enabled)
    }

    override val firstRun: Flowable<Boolean>
        get() = getValue(keys.isFirstRun, true)
    override fun setFirstRun(firstRun: Boolean) {
        setValue(keys.isFirstRun, firstRun)
    }

    override val firstInstallTime: Flowable<Long>
        get() = getValue(keys.firstInstallTime, 0L)
    override fun setFirstInstallTime(firstInstallTime: Long) {
        setValue(keys.firstInstallTime, firstInstallTime)
    }

    override val firstInstallVersion: Flowable<Int>
        get() = getValue(keys.firstInstallVersion, 0)
    override fun setFirstInstallVersion(firstInstallVersion: Int) {
        setValue(keys.firstInstallVersion, firstInstallVersion)
    }

    override val appCrashed: Flowable<Boolean>
        get() = getValue(keys.hasAppCrashed, false)
    override fun setAppCrashed(crashed: Boolean) {
        setValue(keys.hasAppCrashed, crashed)
    }

    override val rateAppNextPromptTime: Flowable<Long>
        get() = getValue(keys.rateAppNextPromptTime, 0L)
    override fun setRateAppNextPromptTime(time: Long) {
        setValue(keys.rateAppNextPromptTime, time)
    }

    override val lastGameWithoutError: Flowable<Boolean>
        get() = getValue(keys.wasLastGameWithoutError, true)
    override fun setLastGameWithoutError(state: Boolean) {
        setValue(keys.wasLastGameWithoutError, state)
    }

    override val appUpdateInProgress: Flowable<Boolean>
        get() = getValue(keys.isAppUpdateInProgress, false)
    override fun setAppUpdateInProgress(state: Boolean) {
        setValue(keys.isAppUpdateInProgress, state)
    }

    override val appUpdateNextPromptTime: Flowable<Long>
        get() = getValue(keys.appUpdateNextPromptTime, 0L)
    override fun setAppUpdateNextPromptTime(time: Long) {
        setValue(keys.appUpdateNextPromptTime, time)
    }

    override fun getRoundLength(gameMode: GameMode): Flowable<Int> = when (gameMode) {
        GameMode.SHOWING -> getValue(keys.roundLengthShowing, 60)
        GameMode.DRAWING_LOCAL ->  getValue(keys.roundLengthLocal, 120)
        GameMode.DRAWING_ONLINE -> getValue(keys.roundLengthOnline, 120)
        else -> Flowable.empty()
    }
    override fun setRoundLength(gameMode: GameMode, length: Int) {
        when (gameMode) {
            GameMode.SHOWING ->        setValue(keys.roundLengthShowing, length)
            GameMode.DRAWING_LOCAL ->  setValue(keys.roundLengthLocal, length)
            GameMode.DRAWING_ONLINE -> setValue(keys.roundLengthOnline, length)
            else -> { }
        }
    }

    override fun getPointsLimit(gameMode: GameMode): Flowable<Int> = when (gameMode) {
        GameMode.DRAWING_LOCAL ->  getValue(keys.pointsLimitLocal, 5)
        GameMode.DRAWING_ONLINE -> getValue(keys.pointsLimitOnline, 5)
        else -> Flowable.empty()
    }
    override fun setPointsLimit(gameMode: GameMode, points: Int) {
        when (gameMode) {
            GameMode.DRAWING_LOCAL ->  setValue(keys.pointsLimitLocal, points)
            GameMode.DRAWING_ONLINE -> setValue(keys.pointsLimitOnline, points)
            else -> { }
        }
    }

    override fun getDrawingPlayerChooseMethod(gameMode: GameMode): Flowable<PlayerChooseMethod> = when (gameMode) {
        GameMode.DRAWING_LOCAL -> dataStore.data().map {
            it[keys.drawingPlayerChooseMethodLocal]?.let { method ->
                PlayerChooseMethod.valueOf(method)
            } ?: PlayerChooseMethod.GUESSING_PLAYER
        }
        GameMode.DRAWING_ONLINE -> dataStore.data().map {
            it[keys.drawingPlayerChooseMethodOnline]?.let { method ->
                PlayerChooseMethod.valueOf(method)
            } ?: PlayerChooseMethod.GUESSING_PLAYER
        }
        else -> Flowable.empty()
    }
    override fun setDrawingPlayerChooseMethod(gameMode: GameMode, method: PlayerChooseMethod) {
        when (gameMode) {
            GameMode.DRAWING_LOCAL -> setValue(keys.drawingPlayerChooseMethodLocal, method.name)
            GameMode.DRAWING_ONLINE -> setValue(keys.drawingPlayerChooseMethodOnline, method.name)
            else -> { }
        }
    }

}