package com.ick.kalambury.di

import com.ick.kalambury.net.DevHttpClientFactory
import com.ick.kalambury.net.TrustStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.InputStream

@Module
@InstallIn(SingletonComponent::class)
class DevHttpClientModule {

    @Provides
    fun provideHttpClient(): OkHttpClient {
        return DevHttpClientFactory.create(NoopTrustStore, null)
    }

}

object NoopTrustStore : TrustStore {
    override val keyStoreInputStream: InputStream
        get() = throw NotImplementedError()
    override val keyStoreType: String
        get() = ""
    override val keyStorePassword: String
        get() = ""
}