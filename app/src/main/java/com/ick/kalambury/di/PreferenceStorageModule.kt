package com.ick.kalambury.di

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.settings.*
import com.ick.kalambury.settings.migrations.MainPreferencesFirstRunMigration
import com.ick.kalambury.settings.migrations.MainPreferencesMigrations
import com.ick.kalambury.util.SchedulerProvider
import com.ick.kalambury.util.settings.DataStoreWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class PreferenceStorageModule {

    @Singleton
    @Provides
    fun providePreferenceStorage(
        @ApplicationContext context: Context,
        keys: PreferenceKeys,
        schedulerProvider: SchedulerProvider,
    ): MainPreferenceStorage {
        val oldName = "${BuildConfig.APPLICATION_ID}_preferences"
        val name = BuildConfig.APPLICATION_ID

        return MainPreferenceStorageImpl(DataStoreWrapper(
            RxPreferenceDataStoreBuilder(context, name)
                .setIoScheduler(schedulerProvider.io())
                .addDataMigration(SharedPreferencesMigration(context, oldName, keys.mainKeys))
                .addRxDataMigration(MainPreferencesFirstRunMigration(context, keys))
                .addRxDataMigration(MainPreferencesMigrations(context, keys))
                .build()), keys
        )
    }

    @Singleton
    @Provides
    fun provideEncryptionKeysStorage(
        @ApplicationContext context: Context,
        keys: PreferenceKeys,
        schedulerProvider: SchedulerProvider,
    ): EncryptionKeysStorage {
        val oldName = "no_bck_prefs"
        val name = "no_bck_prefs.preferences_pb"

        return EncryptionKeysStorageImpl(DataStoreWrapper(RxPreferenceDataStoreBuilder {
            File(context.noBackupFilesDir, name)
        }
            .setIoScheduler(schedulerProvider.io())
            .addDataMigration(SharedPreferencesMigration(context, oldName, keys.encryptionKeys))
            .build()), keys
        )
    }

}