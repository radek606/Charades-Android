package com.ick.kalambury.prompt

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.InstallStatus
import com.ick.kalambury.R
import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.util.action
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import com.ick.kalambury.util.snackbar
import io.reactivex.rxjava3.core.Single

class AppUpdateInstallPrompt(
    private val preferenceStorage: MainPreferenceStorage,
) : Prompt {

    private lateinit var updateManager: AppUpdateManager

    private var installStatus = 0

    override fun isEligible(): Single<Boolean> {
        return preferenceStorage.appUpdateInProgress.firstOrError()
    }

    override fun preparePrompt(context: Context): Single<Boolean> {
        return Single.create { emitter ->
            Log.d(logTag(), "Requesting in-app update info.")
            updateManager = AppUpdateManagerFactory.create(context)
            updateManager.appUpdateInfo
                .addOnSuccessListener {
                    installStatus = it.installStatus()
                    Log.d(logTag(), "In-app update status: " + getInstallStatusString(installStatus))
                    when(installStatus)  {
                        InstallStatus.DOWNLOADED -> emitter.onSuccess(true)
                        InstallStatus.CANCELED,
                        InstallStatus.FAILED,
                        InstallStatus.UNKNOWN -> {
                            preferenceStorage.setAppUpdateInProgress(false)
                            emitter.onSuccess(false)
                        }
                        else -> emitter.onSuccess(false)
                    }
                }
                .addOnFailureListener {
                    emitter.onSuccess(false)
                }
        }
    }

    override fun launchPrompt(activity: FragmentActivity, navController: NavController) {
        activity.findViewById<View>(R.id.root).snackbar(
            R.string.in_app_update_ready_text, Snackbar.LENGTH_INDEFINITE) {
            action(R.string.in_app_update_ready_action, R.color.secondaryColor) {
                Log.d(logTag(), "Installing downloaded update.")
                preferenceStorage.setAppUpdateInProgress(false)
                updateManager.completeUpdate()
            }
        }
    }

    private fun getInstallStatusString(installStatus: Int): String {
        return when (installStatus) {
            InstallStatus.UNKNOWN -> "UNKNOWN"
            InstallStatus.PENDING -> "PENDING"
            InstallStatus.DOWNLOADING -> "DOWNLOADING"
            InstallStatus.DOWNLOADED -> "DOWNLOADED"
            InstallStatus.INSTALLING -> "INSTALLING"
            InstallStatus.INSTALLED -> "INSTALLED"
            InstallStatus.CANCELED -> "CANCELED"
            InstallStatus.FAILED -> "FAILED"
            else -> ""
        }
    }

}