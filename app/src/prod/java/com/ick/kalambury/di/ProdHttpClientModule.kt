package com.ick.kalambury.di

import android.content.Context
import com.ick.kalambury.R
import com.ick.kalambury.net.HttpClientFactory
import com.ick.kalambury.net.ProdHttpClientFactory
import com.ick.kalambury.net.TrustStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.InputStream

@Module
@InstallIn(SingletonComponent::class)
class ProdHttpClientModule {

    @Provides
    fun provideHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return ProdHttpClientFactory.create(BackendServiceTrustStore(context), null)
    }
}

class BackendServiceTrustStore(val context: Context) : TrustStore {

    override val keyStoreInputStream: InputStream
        get() = context.resources.openRawResource(R.raw.truststore)

    override val keyStoreType: String
        get() = "PKCS12"

    override val keyStorePassword: String
        get() = ""

}