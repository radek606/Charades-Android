package com.ick.kalambury.prompt

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.ick.kalambury.MainNavDirections
import com.ick.kalambury.R
import com.ick.kalambury.logging.Log
import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.util.logTag
import com.ick.kalambury.util.showMessageDialog
import io.reactivex.rxjava3.core.Single

class SubmitLogsPrompt(private val preferenceStorage: MainPreferenceStorage) : Prompt {

    override fun isEligible(): Single<Boolean> {
        return preferenceStorage.appCrashed.firstOrError()
    }

    override fun preparePrompt(context: Context): Single<Boolean> {
        return Single.just(true)
    }

    override fun launchPrompt(activity: FragmentActivity, navController: NavController) {
        Log.d(logTag(), "Showing submit log request dialog.")

        activity.showMessageDialog(
            title = R.string.alert_crash_title,
            messageId = R.string.alert_crash_message,
            positiveButton = R.string.dialog_button_continue,
            negativeButton = android.R.string.cancel,
        ) { dialog: DialogInterface, which: Int ->
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> navigateToSubmitDebugLogFragment(navController)
                else -> dialog.dismiss()
            }
        }

        preferenceStorage.setAppCrashed(false)
    }

    private fun navigateToSubmitDebugLogFragment(navController: NavController) {
        navController.navigate(MainNavDirections.actionGlobalSubmitDebugLogFragment())
    }

}