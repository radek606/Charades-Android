package com.ick.kalambury.net

import android.content.Context
import android.os.Build
import com.ick.kalambury.BuildConfig
import okhttp3.ConnectionSpec
import okhttp3.Credentials.basic
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.immutableListOf
import okhttp3.logging.HttpLoggingInterceptor
import java.nio.charset.StandardCharsets
import java.time.Duration
import javax.inject.Inject

class DevHttpClientFactory @Inject constructor() : HttpClientFactory {

    override fun create(context: Context, credentials: CredentialsProvider?): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectionSpecs(immutableListOf(ConnectionSpec.CLEARTEXT))
            .readTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .pingInterval(Duration.ofSeconds(10))
            .addInterceptor(createUserAgentHeaderInterceptor())
            .addInterceptor(createLoggingInterceptor())

        if (credentials != null) {
            builder.addInterceptor(createAuthorizationHeaderInterceptor(credentials))
        }

        return builder.build()
    }

    private fun createAuthorizationHeaderInterceptor(credentialsProvider: CredentialsProvider): Interceptor {
        val credentials = basic(credentialsProvider.user,
            credentialsProvider.password, StandardCharsets.UTF_8)
        return Interceptor { chain: Interceptor.Chain ->
            chain.proceed(chain.request().newBuilder()
                .addHeader("Authorization", credentials)
                .build())
        }
    }

    private fun createUserAgentHeaderInterceptor(): Interceptor {
        val userAgent = "Kalambury ${BuildConfig.VERSION_NAME} (API ${Build.VERSION.SDK_INT})"
        return Interceptor { chain: Interceptor.Chain ->
            chain.proceed(chain.request().newBuilder()
                .addHeader("User-Agent", userAgent)
                .build())
        }
    }

    private fun createLoggingInterceptor(): Interceptor {
        return HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BASIC)
        }
    }

    companion object {
        private const val TIMEOUT_SECONDS = 60L
    }

}