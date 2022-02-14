package com.ick.kalambury.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.rxjava3.RxDataStore
import com.ick.kalambury.GameMode
import com.ick.kalambury.PlayerChooseMethod
import com.ick.kalambury.net.connection.User
import com.ick.kalambury.settings.MainPreferenceStorage.Companion.DEFAULT_CHAT_SIZE
import com.ick.kalambury.words.Language
import com.ick.kalambury.words.Language.Companion.forLanguageName
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.util.*
import kotlin.NoSuchElementException

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

    fun getSelectedWordsSets(instanceId: String): Flowable<List<String>>
    fun setSelectedWordsSets(instanceId: String, sets: List<String>): Completable

    companion object {
        const val DEFAULT_CHAT_SIZE = 5
    }
}

class MainPreferenceStorageImpl(
    private val dataStore: RxDataStore<Preferences>,
    private val keys: PreferenceKeysProvider,
) : MainPreferenceStorage {

    private fun <T : Any> getValue(key: Preferences.Key<T>, defaultValue: T): Flowable<T> {
        return dataStore.data().map { it[key] ?: defaultValue }
    }

    private fun <T> setValue(key: Preferences.Key<T>, value: T?): Single<Preferences> {
        return dataStore.updateDataAsync {
            Single.fromCallable {
                it.toMutablePreferences().apply {
                    when(value) {
                        null -> remove(key)
                        else -> set(key, value)
                    }
                }.toPreferences()
            }
        }
    }

    override val nickname =
        getValue(keys.nickname.asDataStoreKey(), "")
    override fun setNickname(nickname: String) {
        setValue(keys.nickname.asDataStoreKey(), nickname)
    }

    override val uuid: Flowable<String> =
        getValue(keys.userUuid.asDataStoreKey(), UUID.randomUUID().toString())
    override fun setUUID(uuid: String) {
        setValue(keys.userUuid.asDataStoreKey(), uuid)
    }

    override val password: Flowable<String> =
        getValue(keys.password.asDataStoreKey(), "")
    override fun setPassword(password: String?) {
        setValue(keys.password.asDataStoreKey(), password)
    }

    override val localUserData: Flowable<User> = Flowable.zip(uuid, nickname) { u, n -> User(u, u, n) }

    override val wordsLanguage: Flowable<Language> = dataStore.data().map {
        forLanguageName(it[keys.wordsLanguage.asDataStoreKey()] ?: "") }
    override fun setWordsLanguage(language: Language) {
        setValue(keys.wordsLanguage.asDataStoreKey(), language.asString())
    }

    override val chatSize: Flowable<Int> =
        getValue(keys.chatSize.asDataStoreKey(), DEFAULT_CHAT_SIZE)
    override fun setChatSize(size: Int) {
        setValue(keys.chatSize.asDataStoreKey(), size)
    }

    override val showCategoryName: Flowable<Boolean> =
        getValue(keys.showCategoryName.asDataStoreKey(), false)
    override fun setShowCategoryName(show: Boolean) {
        setValue(keys.showCategoryName.asDataStoreKey(), show)
    }

    override val vibrationEnabled: Flowable<Boolean> =
        getValue(keys.vibrationNotificationEnabled.asDataStoreKey(), false)
    override fun setVibrationEnabled(enabled: Boolean) {
        setValue(keys.vibrationNotificationEnabled.asDataStoreKey(), enabled)
    }

    override val sendUsageStatistics: Flowable<Boolean> =
        getValue(keys.sendUsageStatistics.asDataStoreKey(), true)
    override fun setSendUsageStatistics(enabled: Boolean) {
        setValue(keys.sendUsageStatistics.asDataStoreKey(), enabled)
    }

    override val firstRun: Flowable<Boolean> =
        getValue(keys.isFirstRun.asDataStoreKey(), true)
    override fun setFirstRun(firstRun: Boolean) {
        setValue(keys.isFirstRun.asDataStoreKey(), firstRun)
    }

    override val firstInstallTime: Flowable<Long> =
        getValue(keys.firstInstallTime.asDataStoreKey(), 0L)
    override fun setFirstInstallTime(firstInstallTime: Long) {
        setValue(keys.firstInstallTime.asDataStoreKey(), firstInstallTime)
    }

    override val firstInstallVersion: Flowable<Int> =
        getValue(keys.firstInstallVersion.asDataStoreKey(), 0)
    override fun setFirstInstallVersion(firstInstallVersion: Int) {
        setValue(keys.firstInstallVersion.asDataStoreKey(), firstInstallVersion)
    }

    override val appCrashed: Flowable<Boolean> =
        getValue(keys.hasAppCrashed.asDataStoreKey(), false)
    override fun setAppCrashed(crashed: Boolean) {
        setValue(keys.hasAppCrashed.asDataStoreKey(), crashed)
    }

    override val rateAppNextPromptTime: Flowable<Long> =
        getValue(keys.rateAppNextPromptTime.asDataStoreKey(), 0L)
    override fun setRateAppNextPromptTime(time: Long) {
        setValue(keys.rateAppNextPromptTime.asDataStoreKey(), time)
    }

    override val lastGameWithoutError: Flowable<Boolean> =
        getValue(keys.wasLastGameWithoutError.asDataStoreKey(), true)
    override fun setLastGameWithoutError(state: Boolean) {
        setValue(keys.wasLastGameWithoutError.asDataStoreKey(), state)
    }

    override val appUpdateInProgress: Flowable<Boolean> =
        getValue(keys.isAppUpdateInProgress.asDataStoreKey(), false)
    override fun setAppUpdateInProgress(state: Boolean) {
        setValue(keys.isAppUpdateInProgress.asDataStoreKey(), state)
    }

    override val appUpdateNextPromptTime: Flowable<Long> =
        getValue(keys.appUpdateNextPromptTime.asDataStoreKey(), 0L)
    override fun setAppUpdateNextPromptTime(time: Long) {
        setValue(keys.appUpdateNextPromptTime.asDataStoreKey(), time)
    }

    override fun getRoundLength(gameMode: GameMode): Flowable<Int> = when (gameMode) {
        GameMode.SHOWING -> getValue(keys.roundLengthShowing.asDataStoreKey(), 60)
        GameMode.DRAWING_LOCAL ->  getValue(keys.roundLengthLocal.asDataStoreKey(), 120)
        GameMode.DRAWING_ONLINE -> getValue(keys.roundLengthOnline.asDataStoreKey(), 120)
        GameMode.NONE -> Flowable.empty()
    }
    override fun setRoundLength(gameMode: GameMode, length: Int) {
        when (gameMode) {
            GameMode.SHOWING ->        setValue(keys.roundLengthShowing.asDataStoreKey(), length)
            GameMode.DRAWING_LOCAL ->  setValue(keys.roundLengthLocal.asDataStoreKey(), length)
            GameMode.DRAWING_ONLINE -> setValue(keys.roundLengthOnline.asDataStoreKey(), length)
            GameMode.NONE -> { }
        }
    }

    override fun getPointsLimit(gameMode: GameMode): Flowable<Int> = when (gameMode) {
        GameMode.DRAWING_LOCAL ->  getValue(keys.pointsLimitLocal.asDataStoreKey(), 5)
        GameMode.DRAWING_ONLINE -> getValue(keys.pointsLimitOnline.asDataStoreKey(), 5)
        else -> Flowable.empty()
    }
    override fun setPointsLimit(gameMode: GameMode, points: Int) {
        when (gameMode) {
            GameMode.DRAWING_LOCAL ->  setValue(keys.pointsLimitLocal.asDataStoreKey(), points)
            GameMode.DRAWING_ONLINE -> setValue(keys.pointsLimitOnline.asDataStoreKey(), points)
            else -> { }
        }
    }

    override fun getDrawingPlayerChooseMethod(gameMode: GameMode): Flowable<PlayerChooseMethod> = when (gameMode) {
        GameMode.DRAWING_LOCAL -> dataStore.data().map {
            it[keys.drawingPlayerChooseMethodLocal.asDataStoreKey()]?.let { method ->
                PlayerChooseMethod.valueOf(method)
            } ?: PlayerChooseMethod.GUESSING_PLAYER
        }
        GameMode.DRAWING_ONLINE -> dataStore.data().map {
            it[keys.drawingPlayerChooseMethodOnline.asDataStoreKey()]?.let { method ->
                PlayerChooseMethod.valueOf(method)
            } ?: PlayerChooseMethod.GUESSING_PLAYER
        }
        else -> Flowable.empty()
    }
    override fun setDrawingPlayerChooseMethod(gameMode: GameMode, method: PlayerChooseMethod) {
        when (gameMode) {
            GameMode.DRAWING_LOCAL -> setValue(keys.drawingPlayerChooseMethodLocal.asDataStoreKey(), method.name)
            GameMode.DRAWING_ONLINE -> setValue(keys.drawingPlayerChooseMethodOnline.asDataStoreKey(), method.name)
            else -> { }
        }
    }

    override fun getSelectedWordsSets(instanceId: String): Flowable<List<String>> {
        val key = PreferenceKeysProvider.stringSetKey(instanceId).asDataStoreKey()
        return dataStore.data().switchMap {
            val set = it[key]
            if (set != null) {
                Flowable.just(set.toList())
            } else {
                Flowable.error(NoSuchElementException())
            }
        }
    }
    override fun setSelectedWordsSets(instanceId: String, sets: List<String>): Completable {
        val key = PreferenceKeysProvider.stringSetKey(instanceId).asDataStoreKey()
        return setValue(key, sets.toSet()).flatMapCompletable { Completable.complete() }
    }

}