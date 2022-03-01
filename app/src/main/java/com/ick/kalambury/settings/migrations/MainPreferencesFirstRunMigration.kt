package com.ick.kalambury.settings.migrations

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.rxjava3.RxDataMigration
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.settings.PreferenceKeys
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.util.*

class MainPreferencesFirstRunMigration(
    private val context: Context,
    private val keys: PreferenceKeys,
) : RxDataMigration<Preferences> {

    override fun shouldMigrate(currentData: Preferences?): Single<Boolean> {
        return Single.fromCallable {
            currentData?.get(keys.isFirstRun) ?: true
        }
    }

    override fun migrate(currentData: Preferences?): Single<Preferences> {
        return Single.fromCallable {
            Log.d(logTag(), "Executing first run preference migration...")

            val locale = Locale.getDefault()
            val language = if (locale.language == "pl") "pl" else "en"
            val installTimestamp =
                context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime

            currentData!!.toMutablePreferences().apply {
                putAll(
                    keys.wordsLanguage to language,
                    keys.userUuid to UUID.randomUUID().toString(),
                    keys.firstInstallTime to installTimestamp,
                    keys.firstInstallVersion to BuildConfig.VERSION_CODE,
                    keys.isFirstRun to false,
                    keys.prefsVersionCode to BuildConfig.VERSION_CODE
                )
            }.toPreferences()
        }
    }

    override fun cleanUp(): Completable = Completable.complete()

}