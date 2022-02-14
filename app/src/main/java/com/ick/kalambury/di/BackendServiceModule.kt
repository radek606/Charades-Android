package com.ick.kalambury.di

import android.content.Context
import com.ick.kalambury.net.HttpClientFactory
import com.ick.kalambury.net.api.RestApiManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BackendServiceModule {

    @Provides
    fun provideHttpClient(
        @ApplicationContext context: Context,
        factory: HttpClientFactory
    ): OkHttpClient {
        return factory.create(context, null)
    }

    @Singleton
    @Provides
    fun provideBackendServiceManager(client: OkHttpClient): RestApiManager {
        return RestApiManager(client)
    }

}