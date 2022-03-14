package com.ick.kalambury.prompt

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.ick.kalambury.AnalyticsHelper
import com.ick.kalambury.remoteconfig.RemoteConfigHelper
import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

class RateAppPrompt(
    private val preferenceStorage: MainPreferenceStorage,
    private val analyticsHelper: AnalyticsHelper,
) : Prompt {

    private lateinit var reviewManager: ReviewManager

    private var reviewInfo: ReviewInfo? = null
    private var rateAppNextPromptTime = 0L
    private var daysSinceInstalled = 0L

    override fun isEligible(): Single<Boolean> {
        return Single.zip(
            preferenceStorage.appCrashed.firstOrError(),
            preferenceStorage.lastGameWithoutError.firstOrError(),
            preferenceStorage.rateAppNextPromptTime.firstOrError(),
            preferenceStorage.firstInstallTime.firstOrError()
        ) { crash, noError, nextTime, installTime ->
            if (!RemoteConfigHelper.isInAppReviewEnabled || crash || !noError) {
                return@zip false
            }

            rateAppNextPromptTime = nextTime
            daysSinceInstalled =
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - installTime)
            return@zip daysSinceInstalled >= DAYS_SINCE_INSTALL_THRESHOLD &&
                    System.currentTimeMillis() >= rateAppNextPromptTime
        }
    }

    override fun preparePrompt(context: Context): Single<Boolean> {
        Log.d(logTag, "Requesting in-app review info.")
        return Single.create { emitter ->
            reviewManager = ReviewManagerFactory.create(context)
            reviewManager.requestReviewFlow()
                .addOnSuccessListener {
                    reviewInfo = it
                    emitter.onSuccess(true)
                }
                .addOnFailureListener {
                    Log.d(logTag, "Failed getting review info.")
                    emitter.onSuccess(false)
                }
        }
    }

    override fun launchPrompt(activity: FragmentActivity, navController: NavController) {
        if (reviewInfo == null) {
            Log.w(logTag, "Requesting in-app review flow with null reviewInfo!")
            return
        }

        Log.d(logTag, "Requesting in-app review flow.")

        reviewManager.launchReviewFlow(activity, reviewInfo!!)
            .addOnCompleteListener {
                Log.d(logTag, "Finished review flow. Success: ${it.isSuccessful}")
                analyticsHelper.logRateAppPrompt(rateAppNextPromptTime == 0L,
                    daysSinceInstalled)
                val waitUntil = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(
                    DAYS_UNTIL_REPROMPT_THRESHOLD.toLong())
                preferenceStorage.setRateAppNextPromptTime(waitUntil)
            }
    }

    companion object {
        private const val DAYS_SINCE_INSTALL_THRESHOLD = 3
        private const val DAYS_UNTIL_REPROMPT_THRESHOLD = 30
    }

}