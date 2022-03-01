package com.ick.kalambury.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceKeys @Inject internal constructor(@ApplicationContext context: Context) {

    val prefsVersionCode = intPreferencesKey(context.getString(R.string.key_settings_version_code))

    val isFirstRun = booleanPreferencesKey(context.getString(R.string.key_first_run))
    val firstInstallTime = longPreferencesKey(context.getString(R.string.key_first_install_time))
    val firstInstallVersion = intPreferencesKey(context.getString(R.string.key_first_install_version))

    val userUuid = stringPreferencesKey(context.getString(R.string.key_user_uuid))
    val nickname = stringPreferencesKey(context.getString(R.string.key_nickname))
    val password = stringPreferencesKey(context.getString(R.string.key_password))
    val wordsLanguage = stringPreferencesKey(context.getString(R.string.key_words_language))
    val chatSize = intPreferencesKey(context.getString(R.string.key_chat_size))
    val showCategoryName = booleanPreferencesKey(context.getString(R.string.key_show_category_name))
    val vibrationNotificationEnabled = booleanPreferencesKey(context.getString(R.string.key_vibration_notification))
    val sendUsageStatistics = booleanPreferencesKey(context.getString(R.string.key_send_usage_statistics))

    val roundLengthShowing = intPreferencesKey(context.getString(R.string.key_round_length_showing))
    val roundLengthLocal = intPreferencesKey(context.getString(R.string.key_round_length_local))
    val roundLengthOnline = intPreferencesKey(context.getString(R.string.key_round_length_online))

    val pointsLimitLocal = intPreferencesKey(context.getString(R.string.key_points_limit_local))
    val pointsLimitOnline = intPreferencesKey(context.getString(R.string.key_points_limit_online))

    val drawingPlayerChooseMethodLocal = stringPreferencesKey(context.getString(R.string.key_drawing_player_choose_method_local))
    val drawingPlayerChooseMethodOnline = stringPreferencesKey(context.getString(R.string.key_drawing_player_choose_method_online))

    val hasAppCrashed = booleanPreferencesKey(context.getString(R.string.key_has_app_crashed))

    val rateAppNextPromptTime = longPreferencesKey(context.getString(R.string.key_rate_app_next_prompt_time))
    val wasLastGameWithoutError = booleanPreferencesKey(context.getString(R.string.key_last_game_without_error))

    val isAppUpdateInProgress = booleanPreferencesKey(context.getString(R.string.key_app_update_in_progress))
    val appUpdateNextPromptTime = longPreferencesKey(context.getString(R.string.key_app_update_next_prompt_time))

    val wordsUnencryptedSecret = stringPreferencesKey(context.getString(R.string.key_words_unencrypted_secret))
    val wordsEncryptedSecret = stringPreferencesKey(context.getString(R.string.key_words_encrypted_secret))
    
    val allKeys: Set<String> = setOf(
        prefsVersionCode.name,
        isFirstRun.name,
        firstInstallTime.name,
        firstInstallVersion.name,
        userUuid.name,
        nickname.name,
        password.name,
        wordsLanguage.name,
        chatSize.name,
        showCategoryName.name,
        vibrationNotificationEnabled.name,
        sendUsageStatistics.name,
        roundLengthShowing.name,
        roundLengthLocal.name,
        roundLengthOnline.name,
        pointsLimitLocal.name,
        pointsLimitOnline.name,
        drawingPlayerChooseMethodLocal.name,
        drawingPlayerChooseMethodOnline.name,
        hasAppCrashed.name,
        rateAppNextPromptTime.name,
        wasLastGameWithoutError.name,
        isAppUpdateInProgress.name,
        appUpdateNextPromptTime.name,

        wordsUnencryptedSecret.name,
        wordsEncryptedSecret.name,
    )

    val encryptionKeys: Set<String> = setOf(wordsUnencryptedSecret.name, wordsEncryptedSecret.name)
    val mainKeys: Set<String> = allKeys - encryptionKeys

    companion object {
        fun intKey(key: String)       = intPreferencesKey("${BuildConfig.APPLICATION_ID}.$key")
        fun doubleKey(key: String)    = doublePreferencesKey("${BuildConfig.APPLICATION_ID}.$key")
        fun stringKey(key: String)    = stringPreferencesKey("${BuildConfig.APPLICATION_ID}.$key")
        fun booleanKey(key: String)   = booleanPreferencesKey("${BuildConfig.APPLICATION_ID}.$key")
        fun floatKey(key: String)     = floatPreferencesKey("${BuildConfig.APPLICATION_ID}.$key")
        fun longKey(key: String)      = longPreferencesKey("${BuildConfig.APPLICATION_ID}.$key")
        fun stringSetKey(key: String) = stringSetPreferencesKey("${BuildConfig.APPLICATION_ID}.$key")
    }

}