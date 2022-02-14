package com.ick.kalambury.di

import com.ick.kalambury.AnalyticsHelper
import com.ick.kalambury.prompt.*
import com.ick.kalambury.settings.MainPreferenceStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class PromptModule {

    @Provides
    fun providePromptList(
            preferenceStorage: MainPreferenceStorage,
            analyticsHelper: AnalyticsHelper,
    ): ArrayList<Prompt> {
        return ArrayList(listOf(
            SubmitLogsPrompt(preferenceStorage),
            AppUpdateRequestPrompt(preferenceStorage),
            AppUpdateInstallPrompt(preferenceStorage),
            RateAppPrompt(preferenceStorage, analyticsHelper)
        ))
    }

}