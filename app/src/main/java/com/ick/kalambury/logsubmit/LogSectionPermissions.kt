package com.ick.kalambury.logsubmit

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.ick.kalambury.BuildConfig

class LogSectionPermissions(val context: Context) : LogSection {

    override val title: String
        get() = "PERMISSIONS"

    override fun getContent() = buildString {
        val status: MutableList<Pair<String, Boolean>> = mutableListOf()
        val info = context.packageManager.getPackageInfo(
            BuildConfig.APPLICATION_ID,
            PackageManager.GET_PERMISSIONS
        )

        info.requestedPermissions.forEachIndexed { index, string ->
            status.add(string to (info.requestedPermissionsFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0))
        }

        status.sortedWith(compareBy { it.first }).forEach {
            append(it.first).append(": ").append(it.second).appendLine()
        }
    }

}