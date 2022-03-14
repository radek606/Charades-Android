package com.ick.kalambury.remoteconfig

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.R
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag

object RemoteConfigHelper {

    private const val REMOTE_KEY_IN_APP_REVIEW_ENABLED =
        BuildConfig.REMOTE_CONFIG_KEY_PREFIX + "in_app_review_enabled"
    private const val REMOTE_KEY_APP_UPDATES = BuildConfig.REMOTE_CONFIG_KEY_PREFIX + "app_updates"
    private const val REMOTE_KEY_WORDS_MANIFEST = BuildConfig.REMOTE_CONFIG_KEY_PREFIX + "words_manifest"

    init {
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = BuildConfig.REMOTE_CONFIG_MIN_FETCH_INTERVAL_SECONDS
        }
        Firebase.remoteConfig.apply {
            setDefaultsAsync(R.xml.remote_config_defaults)
            setConfigSettingsAsync(settings)
                .addOnSuccessListener { fetchConfig(this) }
        }
    }

    private fun fetchConfig(instance: FirebaseRemoteConfig) {
        instance.fetchAndActivate()
            .addOnSuccessListener { updated: Boolean ->
                Log.d(logTag, "Remote config updated: $updated")
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(logTag, "Failed updating remote config.", e)
            }
    }

    val isInAppReviewEnabled: Boolean
        get() = Firebase.remoteConfig[REMOTE_KEY_IN_APP_REVIEW_ENABLED].asBoolean()

    val appUpdatesData: String
        get() = Firebase.remoteConfig[REMOTE_KEY_APP_UPDATES].asString()

    val remoteWordsManifest: String
        get() = Firebase.remoteConfig[REMOTE_KEY_WORDS_MANIFEST].asString()
}