package com.ick.kalambury.settings.migrations

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.rxjava3.RxDataMigration
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.settings.PreferenceKeys
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

abstract class VersionMigration(
    private val keys: PreferenceKeys,
) : RxDataMigration<Preferences> {

    final override fun shouldMigrate(currentData: Preferences?): Single<Boolean> {
        return Single.fromCallable {
            val prefsVersion = currentData?.get(keys.prefsVersionCode) ?: 0
            prefsVersion < BuildConfig.VERSION_CODE
        }
    }


    final override fun migrate(currentData: Preferences?): Single<Preferences> {
        return Single.fromCallable {
            currentData!!.toMutablePreferences().let { prefs ->
                val prefsVersion = prefs[keys.prefsVersionCode] ?: 0

                Log.d(logTag(), "Executing preferences migration from version " +
                        "$prefsVersion to ${BuildConfig.VERSION_CODE}...")

                doMigrate(prefsVersion, prefs)

                prefs[keys.prefsVersionCode] = BuildConfig.VERSION_CODE

                prefs.toPreferences()
            }
        }
    }

    abstract fun doMigrate(prefsVersion: Int, prefs: MutablePreferences)

    override fun cleanUp(): Completable = Completable.complete()

}