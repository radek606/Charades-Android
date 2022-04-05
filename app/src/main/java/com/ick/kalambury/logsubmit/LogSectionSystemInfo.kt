package com.ick.kalambury.logsubmit

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.CommonStatusCodes
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.settings.MainPreferenceStorage
import io.reactivex.rxjava3.core.Single
import java.util.*
import java.util.concurrent.TimeUnit

class LogSectionSystemInfo(
    private val context: Context,
    private val preferenceStorage: MainPreferenceStorage,
) : LogSection {

    override val title: String
        get() = "SYSINFO"

    override fun getContent(): String {
        return Single.zip(
            preferenceStorage.firstInstallTime.firstOrError(),
            preferenceStorage.firstInstallVersion.firstOrError()
        ) { installTime, installVersion ->
            buildString {
                append("Time          : ").appendLine(System.currentTimeMillis())
                append("Manufacturer  : ").appendLine(Build.MANUFACTURER)
                append("Model         : ").appendLine(Build.MODEL)
                append("Product       : ").appendLine(Build.PRODUCT)
                append("Screen        : ").append(getScreenResolution(context)).append(", ")
                    .append(getScreenDensity(context)).append(", ")
                    .append(getScreenRefreshRate(context)).appendLine()
                append("Font Scale    : ").appendLine(context.resources.configuration.fontScale)
                append("Android       : ").append(Build.VERSION.RELEASE).append(" (")
                    .append(Build.VERSION.INCREMENTAL).append(", ")
                    .append(Build.DISPLAY).appendLine(")")
                append("Play Services : ").appendLine(getPlayServicesString(context))
                append("Locale        : ").appendLine(Locale.getDefault().toString())
                append("First Version : ").appendLine(installVersion)
                append("Days Installed: ").appendLine(getDaysSinceFirstInstalled(installTime))
                append("Build Variant : ").append(BuildConfig.FLAVOR)
                    .append(BuildConfig.BUILD_TYPE).appendLine()
                append("App           : ")
                try {
                    val pm: PackageManager = context.packageManager
                    append(pm.getApplicationLabel(context.applicationInfo))
                        .append(" ")
                        .append(BuildConfig.VERSION_NAME)
                        .append(" (")
                        .append(BuildConfig.VERSION_CODE)
                        .appendLine(")")
                } catch (nnfe: PackageManager.NameNotFoundException) {
                    appendLine("Unknown")
                }
                append("Package       : ").append(BuildConfig.APPLICATION_ID)
            }
        }.blockingGet()
    }

    private fun getScreenResolution(context: Context): String {
        return context.getSystemService<WindowManager>()?.let {
            val dm = DisplayMetrics()
            it.defaultDisplay.getMetrics(dm)
            "${dm.widthPixels}x${dm.heightPixels}"
        } ?: "Unavailable"
    }

    private fun getScreenRefreshRate(context: Context): String {
        return context.getSystemService<WindowManager>()?.let {
            String.format(Locale.ENGLISH, "%.2f hz", it.defaultDisplay.refreshRate)
        } ?: "Unavailable"
    }

    private fun getPlayServicesString(context: Context): String {
        val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return if (result == ConnectionResult.SUCCESS) {
            "true"
        } else {
            "false (${CommonStatusCodes.getStatusCodeString(result)})"
        }
    }

    private fun getScreenDensity(context: Context): String {
        val density = context.resources.displayMetrics.densityDpi

        var bucket = ""
        for ((key, value) in densityLevels) {
            bucket = value
            if (key > density) {
                break
            }
        }

        return "$bucket ($density)"
    }

    private fun getDaysSinceFirstInstalled(installTimestamp: Long): Long {
        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - installTimestamp)
    }

    private val densityLevels = mapOf(
        DisplayMetrics.DENSITY_LOW to "ldpi",
        DisplayMetrics.DENSITY_MEDIUM to "mdpi",
        DisplayMetrics.DENSITY_HIGH to "hdpi",
        DisplayMetrics.DENSITY_XHIGH to "xhdpi",
        DisplayMetrics.DENSITY_XXHIGH to "xxhdpi",
        DisplayMetrics.DENSITY_XXXHIGH to "xxxhdpi"
    )

}