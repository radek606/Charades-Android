package com.ick.kalambury

import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    preferenceStorage: MainPreferenceStorage,
) {

    private val instance: FirebaseAnalytics = Firebase.analytics

    init {
        preferenceStorage.sendUsageStatistics
            .firstOrError()
            .subscribeBy(
                onSuccess = {
                    val enable = it && !BuildConfig.DEBUG
                    instance.setAnalyticsCollectionEnabled(enable)
                    Log.i(logTag, "Analytics enabled: $enable")
                },
                onError = { Log.w(logTag, "Failed initializing analytics.", it) }
            )
    }

    fun logGameStarted(gameMode: GameMode) {
        instance.logEvent(
            "game_started", bundleOf(
                "mode" to gameMode.name
            )
        )
    }

    fun logRateAppPrompt(firstTime: Boolean, daysSinceInstall: Long) {
        instance.logEvent(
            "rate_app_prompt", bundleOf(
                "first_time" to firstTime,
                "days_since_install" to daysSinceInstall
            )
        )
    }

}