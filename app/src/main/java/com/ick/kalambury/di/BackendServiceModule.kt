package com.ick.kalambury.di

import com.ick.kalambury.BuildConfig
import com.ick.kalambury.net.api.RestApiManager
import com.ick.kalambury.util.SchedulerProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BackendServiceModule {

    @Singleton
    @Provides
    fun provideBackendServiceManager(
        client: OkHttpClient,
        schedulerProvider: SchedulerProvider,
    ): RestApiManager {
        return RestApiManager(client, BuildConfig.SERVICE_URL, schedulerProvider)
    }

}