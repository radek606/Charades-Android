package com.ick.kalambury.settings.migrations

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import com.ick.kalambury.settings.PreferenceKeys

class MainPreferencesMigrations(
    private val context: Context,
    private val keys: PreferenceKeys,
) : VersionMigration(keys) {

    override fun doMigrate(prefsVersion: Int, prefs: MutablePreferences) {
        if (prefsVersion < CLEAR_USERNAME) {
            prefs[keys.nickname]?.let {
                prefs[keys.nickname] =
                    it.replace("<.*?>".toRegex(), "")
            }
        }

        if (prefsVersion < REFACTOR) {
            prefs[keys.firstInstallTime] =
                context.packageManager.getPackageInfo(context.packageName, 0)
                    .firstInstallTime
        }
    }

    companion object {
        private const val CLEAR_USERNAME = 61
        private const val REFACTOR = 82
    }

}