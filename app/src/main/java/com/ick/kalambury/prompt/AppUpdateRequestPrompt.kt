package com.ick.kalambury.prompt

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.R
import com.ick.kalambury.logging.Log
import com.ick.kalambury.remoteconfig.AppUpdateData
import com.ick.kalambury.remoteconfig.RemoteConfigHelper
import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.util.JsonUtils
import com.ick.kalambury.util.logTag
import com.ick.kalambury.util.snackbar
import io.reactivex.rxjava3.core.Single
import java.io.IOException
import java.util.concurrent.TimeUnit

@SuppressLint("WrongConstant")
class AppUpdateRequestPrompt(
    private val preferenceStorage: MainPreferenceStorage,
) : Prompt {

    private lateinit var updateManager: AppUpdateManager

    private var appUpdateInfo: AppUpdateInfo? = null
    private var updateMode = UPDATE_MODE_NONE

    override fun isEligible(): Single<Boolean> {
        return Single.zip(
            preferenceStorage.appUpdateInProgress.firstOrError(),
            preferenceStorage.appUpdateNextPromptTime.firstOrError(),
        ) { b1, b2 ->
            !b1 && System.currentTimeMillis() > b2 && getUpdateMode(
                getHighestPriorityUpdate(), null
            ) != UPDATE_MODE_NONE
        }
    }

    override fun preparePrompt(context: Context): Single<Boolean> {
        return Single.create { emitter ->
            Log.d(logTag(), "Requesting in-app update info.")
            updateManager = AppUpdateManagerFactory.create(context)
            updateManager.appUpdateInfo
                .addOnSuccessListener {
                    if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        Log.d(logTag(), "Update available.")
                        updateMode =
                            getUpdateMode(
                                getHighestPriorityUpdate(),
                                it.clientVersionStalenessDays()
                            )
                        if (updateMode != UPDATE_MODE_NONE && it.isUpdateTypeAllowed(updateMode)) {
                            appUpdateInfo = it
                            emitter.onSuccess(true)
                        } else {
                            Log.d(
                                logTag(),
                                "Low priority update or desired update type unavailable."
                            )
                            scheduleNextUpdateRequest()
                            emitter.onSuccess(false)
                        }
                    } else if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        appUpdateInfo = it
                        updateMode = UPDATE_MODE_IMMEDIATE
                        emitter.onSuccess(true)
                    } else {
                        Log.d(logTag(), "No update available or unknown state.")
                        scheduleNextUpdateRequest()
                        emitter.onSuccess(false)
                    }
                }
                .addOnFailureListener { emitter.onSuccess(false) }
        }
    }

    override fun launchPrompt(activity: FragmentActivity, navController: NavController) {
        if (appUpdateInfo == null) {
            Log.w(logTag(), "Starting in-app update flow with null appUpdateInfo!")
            return
        }

        preferenceStorage.setAppUpdateInProgress(true)
        Log.d(logTag(), "Starting in-app update ${getUpdateTypeString(updateMode)} flow.")

        updateManager.startUpdateFlow(
            appUpdateInfo!!,
            activity,
            AppUpdateOptions.newBuilder(updateMode).build()
        )
            .addOnSuccessListener { result: Int ->
                if (result == Activity.RESULT_OK) {
                    Log.d(logTag(), "User agreed for update. Downloading...")
                    showDownloadingSnackbar(activity)
                } else if (result == Activity.RESULT_CANCELED || result == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
                    Log.w(logTag(), "User denied update request or error occurred.")
                    scheduleNextUpdateRequest()
                }
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(logTag(), "Failed starting in-app update flow.", e)
            }
    }

    private fun getUpdateMode(priority: Int, daysSinceUpdate: Int?): Int {
        return when (priority) {
            in 0..1 -> UPDATE_MODE_NONE
            in 2..4 -> {
                if (daysSinceUpdate != null && daysSinceUpdate > STALENESS_DAYS_THRESHOLD) {
                    UPDATE_MODE_IMMEDIATE
                } else {
                    UPDATE_MODE_FLEXIBLE
                }
            }
            else -> UPDATE_MODE_IMMEDIATE
        }
    }

    private fun getHighestPriorityUpdate(): Int {
        val dataString = RemoteConfigHelper.appUpdatesData
        return try {
            JsonUtils.fromJson(dataString, AppUpdateData::class.java).updates
                .filter { it.versionCode > BuildConfig.VERSION_CODE }
                .maxOfOrNull { it.priority } ?: 0
        } catch (e: IOException) {
            Log.d(logTag(), "Failed parsing update data.")
            0
        }
    }

    private fun showDownloadingSnackbar(activity: FragmentActivity) {
        activity.findViewById<View>(R.id.root)
            .snackbar(R.string.in_app_update_downloading_text) { setBackgroundTint(0) }
    }

    private fun scheduleNextUpdateRequest() {
        val nextTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        preferenceStorage.setAppUpdateNextPromptTime(nextTime)
        Log.d(logTag(), "Scheduling next update check to: $nextTime")
    }

    private fun getUpdateTypeString(updateType: Int) = when (updateType) {
        UPDATE_MODE_FLEXIBLE -> "flexible"
        UPDATE_MODE_IMMEDIATE -> "immediate"
        else -> ""
    }

    companion object {
        private const val UPDATE_MODE_NONE = -1
        private const val UPDATE_MODE_FLEXIBLE = AppUpdateType.FLEXIBLE
        private const val UPDATE_MODE_IMMEDIATE = AppUpdateType.IMMEDIATE
        private const val STALENESS_DAYS_THRESHOLD = 7
    }
}