package com.ick.kalambury.net

import android.os.Build
import com.ick.kalambury.BuildConfig
import okhttp3.ConnectionSpec
import okhttp3.Credentials.basic
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.immutableListOf
import okhttp3.logging.HttpLoggingInterceptor
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Duration

object DevHttpClientFactory : HttpClientFactory {

    private const val TIMEOUT_SECONDS = 60L

    override fun create(trustStore: TrustStore, credentials: CredentialsProvider?): OkHttpClient {
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

    private fun createAuthorizationHeaderInterceptor(credentials: CredentialsProvider): Interceptor {
        return Interceptor { chain ->
            chain.proceed(chain.request().newBuilder()
                .addHeader("Authorization", basic(credentials.user, credentials.password, UTF_8))
                .build())
        }
    }

    private fun createUserAgentHeaderInterceptor(): Interceptor {
        val userAgent = "Kalambury ${BuildConfig.VERSION_NAME} (API ${Build.VERSION.SDK_INT})"
        return Interceptor { chain ->
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

}