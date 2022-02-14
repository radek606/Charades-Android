package com.ick.kalambury.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.rxjava3.RxDataMigration
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.logging.Log
import com.ick.kalambury.util.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.util.*

class MainPreferencesFirstRunMigration(
    private val context: Context,
    private val keysProvider: PreferenceKeysProvider,
) : RxDataMigration<Preferences> {

    override fun shouldMigrate(currentData: Preferences?): Single<Boolean> {
        return Single.fromCallable {
            currentData?.get(keysProvider.isFirstRun.asDataStoreKey()) ?: true
        }
    }

    override fun migrate(currentData: Preferences?): Single<Preferences> {
        return Single.fromCallable {
            Log.d(logTag(), "Executing first run preference migration...")

            val locale = Locale.getDefault()
            val language = if (locale.language == "pl") "pl" else "en"
            val installTimestamp = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime

            currentData!!.toMutablePreferences().let { prefs ->
                prefs[keysProvider.wordsLanguage.asDataStoreKey()] = language
                prefs[keysProvider.userUuid.asDataStoreKey()] = UUID.randomUUID().toString()
                prefs[keysProvider.firstInstallTime.asDataStoreKey()] = installTimestamp
                prefs[keysProvider.firstInstallVersion.asDataStoreKey()] = BuildConfig.VERSION_CODE
                prefs[keysProvider.isFirstRun.asDataStoreKey()] = false

                prefs.toPreferences()
            }
        }
    }

    override fun cleanUp(): Completable = Completable.complete()

}