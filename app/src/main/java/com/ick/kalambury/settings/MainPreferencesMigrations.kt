package com.ick.kalambury.settings

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.rxjava3.RxDataMigration
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.logging.Log
import com.ick.kalambury.util.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class MainPreferencesMigrations(
    private val context: Context,
    private val keysProvider: PreferenceKeysProvider,
) : RxDataMigration<Preferences> {

    override fun shouldMigrate(currentData: Preferences?): Single<Boolean> {
        return Single.fromCallable {
            val prefsVersion = currentData?.get(keysProvider.prefsVersionCode.asDataStoreKey()) ?: 0
            prefsVersion < BuildConfig.VERSION_CODE
        }
    }

    override fun migrate(currentData: Preferences?): Single<Preferences> {
        return Single.fromCallable {
            currentData!!.toMutablePreferences().let { prefs ->
                val prefsVersion = prefs[keysProvider.prefsVersionCode.asDataStoreKey()] ?: 0

                Log.d(logTag(), "Executing preferences migration from version " +
                            "$prefsVersion to ${BuildConfig.VERSION_CODE}...")

                doMigrate(prefsVersion, prefs)

                prefs[keysProvider.prefsVersionCode.asDataStoreKey()] = BuildConfig.VERSION_CODE

                prefs.toPreferences()
            }
        }
    }

    private fun doMigrate(prefsVersion: Int, prefs: MutablePreferences) {
        if (prefsVersion < CLEAR_USERNAME) {
            prefs[keysProvider.nickname.asDataStoreKey()]?.let {
                prefs[keysProvider.nickname.asDataStoreKey()] =
                    it.replace("<.*?>".toRegex(), "")
            }
        }

        if (prefsVersion < REFACTOR) {
            prefs[keysProvider.firstInstallTime.asDataStoreKey()] =
                context.packageManager.getPackageInfo(context.packageName, 0)
                    .firstInstallTime

            prefs.remove(keysProvider.wordsUnencryptedSecret.asDataStoreKey())
            prefs.remove(keysProvider.wordsEncryptedSecret.asDataStoreKey())
        }
    }

    override fun cleanUp(): Completable = Completable.complete()

    companion object {
        private const val CLEAR_USERNAME = 61
        private const val REFACTOR = 82
    }

}