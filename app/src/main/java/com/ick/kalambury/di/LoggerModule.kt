package com.ick.kalambury.di

import android.content.Context
import com.ick.kalambury.logging.LogFilesRepository
import com.ick.kalambury.logsubmit.*
import com.ick.kalambury.settings.MainPreferenceStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class LoggerModule {

    @Provides
    fun provideLogSectionList(
        @ApplicationContext context: Context,
        preferenceStorage: MainPreferenceStorage,
        logFilesRepository: LogFilesRepository,
    ): ArrayList<LogSection> {
        return ArrayList(listOf(
            LogSectionSystemInfo(context, preferenceStorage),
            LogSectionThreads(),
            LogSectionBlockedThreads(),
            LogSectionLogcat(),
            LogSectionLogger(logFilesRepository)
        ))
    }

}