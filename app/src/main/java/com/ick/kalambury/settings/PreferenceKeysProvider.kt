package com.ick.kalambury.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.*
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class PreferenceKeysProvider @Inject internal constructor(@ApplicationContext context: Context) {

    val prefsVersionCode = create(context, R.string.key_settings_version_code, Int::class)

    val isFirstRun = create(context, R.string.key_first_run, Boolean::class)
    val firstInstallTime = create(context, R.string.key_first_install_time, Long::class)
    val firstInstallVersion = create(context, R.string.key_first_install_version, Int::class)

    val userUuid = create(context, R.string.key_user_uuid, String::class)
    val nickname = create(context, R.string.key_nickname, String::class)
    val password = create(context, R.string.key_password, String::class)
    val wordsLanguage = create(context, R.string.key_words_language, String::class)
    val chatSize = create(context, R.string.key_chat_size, Int::class)
    val showCategoryName = create(context, R.string.key_show_category_name, Boolean::class)
    val vibrationNotificationEnabled = create(context, R.string.key_vibration_notification, Boolean::class)
    val sendUsageStatistics = create(context, R.string.key_send_usage_statistics, Boolean::class)

    val roundLengthShowing = create(context, R.string.key_round_length_showing, Int::class)
    val roundLengthLocal = create(context, R.string.key_round_length_local, Int::class)
    val roundLengthOnline = create(context, R.string.key_round_length_online, Int::class)

    val pointsLimitLocal = create(context, R.string.key_points_limit_local, Int::class)
    val pointsLimitOnline = create(context, R.string.key_points_limit_online, Int::class)

    val drawingPlayerChooseMethodLocal = create(context, R.string.key_drawing_player_choose_method_local, String::class)
    val drawingPlayerChooseMethodOnline = create(context, R.string.key_drawing_player_choose_method_online, String::class)

    val hasAppCrashed = create(context, R.string.key_has_app_crashed, Boolean::class)

    val rateAppNextPromptTime = create(context, R.string.key_rate_app_next_prompt_time, Long::class)
    val wasLastGameWithoutError = create(context, R.string.key_last_game_without_error, Boolean::class)

    val isAppUpdateInProgress = create(context, R.string.key_app_update_in_progress, Boolean::class)
    val appUpdateNextPromptTime = create(context, R.string.key_app_update_next_prompt_time, Long::class)

    val wordsUnencryptedSecret = create(context, R.string.key_words_unencrypted_secret, String::class)
    val wordsEncryptedSecret = create(context, R.string.key_words_encrypted_secret, String::class)

    private fun <T : Any> create(context: Context, @StringRes keyId: Int, type: KClass<T>): PrefKey<T> {
        return context.getString(keyId) to type
    }

    class PrefKey<T : Any> internal constructor(val stringKey: String, val type: KClass<T>)

    companion object {
        fun intKey(key: String): PrefKey<Int> = BuildConfig.APPLICATION_ID + "." +  key to Int::class
        fun doubleKey(key: String): PrefKey<Double> = BuildConfig.APPLICATION_ID + "." + key to Double::class
        fun stringKey(key: String): PrefKey<String> = BuildConfig.APPLICATION_ID + "." + key to String::class
        fun booleanKey(key: String): PrefKey<Boolean> = BuildConfig.APPLICATION_ID + "." + key to Boolean::class
        fun floatKey(key: String): PrefKey<Float> = BuildConfig.APPLICATION_ID + "." + key to Float::class
        fun longKey(key: String): PrefKey<Long> = BuildConfig.APPLICATION_ID + "." + key to Long::class
        fun stringSetKey(key: String): PrefKey<Set<*>> = BuildConfig.APPLICATION_ID + "." + key to Set::class
    }

}

private infix fun <T : Any> String.to(type: KClass<T>): PreferenceKeysProvider.PrefKey<T> =
    PreferenceKeysProvider.PrefKey(this, type)

@JvmName("toDataStoreKeyInt")
fun PreferenceKeysProvider.PrefKey<Int>.asDataStoreKey(): Preferences.Key<Int> {
    return intPreferencesKey(this.stringKey)
}

@JvmName("toDataStoreKeyDouble")
fun PreferenceKeysProvider.PrefKey<Double>.asDataStoreKey(): Preferences.Key<Double> {
    return doublePreferencesKey(this.stringKey)
}

@JvmName("toDataStoreKeyString")
fun PreferenceKeysProvider.PrefKey<String>.asDataStoreKey(): Preferences.Key<String> {
    return stringPreferencesKey(this.stringKey)
}

@JvmName("toDataStoreKeyBoolean")
fun PreferenceKeysProvider.PrefKey<Boolean>.asDataStoreKey(): Preferences.Key<Boolean> {
    return booleanPreferencesKey(this.stringKey)
}

@JvmName("toDataStoreKeyFloat")
fun PreferenceKeysProvider.PrefKey<Float>.asDataStoreKey(): Preferences.Key<Float> {
    return floatPreferencesKey(this.stringKey)
}

@JvmName("toDataStoreKeyLong")
fun PreferenceKeysProvider.PrefKey<Long>.asDataStoreKey(): Preferences.Key<Long> {
    return longPreferencesKey(this.stringKey)
}

@JvmName("toDataStoreKeyStringSet")
fun PreferenceKeysProvider.PrefKey<Set<*>>.asDataStoreKey(): Preferences.Key<Set<String>> {
    return stringSetPreferencesKey(this.stringKey)
}