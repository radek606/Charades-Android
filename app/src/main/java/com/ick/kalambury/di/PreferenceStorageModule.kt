package com.ick.kalambury.di

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.settings.*
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
        keysProvider: PreferenceKeysProvider,
    ): MainPreferenceStorage {
        val oldName = "${context.packageName}_preferences"
        val name = BuildConfig.APPLICATION_ID

        return MainPreferenceStorageImpl(
            RxPreferenceDataStoreBuilder(context, name)
                .addDataMigration(SharedPreferencesMigration(context, oldName))
                .addRxDataMigration(MainPreferencesFirstRunMigration(context, keysProvider))
                .addRxDataMigration(MainPreferencesMigrations(context, keysProvider))
                .build(), keysProvider
        )
    }

    @Singleton
    @Provides
    fun provideEncryptionKeysStorage(
        @ApplicationContext context: Context,
        keysProvider: PreferenceKeysProvider,
    ): EncryptionKeysStorage {
        val name = "no_bck_prefs.preferences_pb"

        return EncryptionKeysStorageImpl(RxPreferenceDataStoreBuilder {
            File(context.noBackupFilesDir, name)
        }.build(), keysProvider)
    }

}