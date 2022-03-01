package com.ick.kalambury.net

import android.os.Build
import com.ick.kalambury.BuildConfig
import okhttp3.*
import okhttp3.Credentials.basic
import okhttp3.internal.immutableListOf
import java.nio.charset.StandardCharsets.*
import java.security.KeyStore
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object ProdHttpClientFactory : HttpClientFactory {

    private const val TIMEOUT_SECONDS = 60L

    override fun create(trustStore: TrustStore, credentials: CredentialsProvider?): OkHttpClient {
        return try {
            val managers: Array<TrustManager> = createTrustManagers(trustStore)
            val sslContext: SSLContext = SSLContext.getInstance("TLS")
            sslContext.init(null, managers, null)
            val builder = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, managers[0] as X509TrustManager)
                .certificatePinner(CertificatePinner.Builder()
                    .add(BuildConfig.SERVICE_URL.replace("https://", ""), "sha256/...")
                    .add(BuildConfig.SERVICE_URL.replace("https://", ""), "sha256/...")
                    .build())
                .connectionSpecs(immutableListOf(ConnectionSpec.RESTRICTED_TLS))
                .readTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .pingInterval(Duration.ofSeconds(10))
                .addInterceptor(createUserAgentHeaderInterceptor())

            if (credentials != null) {
                builder.addInterceptor(createAuthorizationHeaderInterceptor(credentials))
            }

            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun createTrustManagers(trustStore: TrustStore): Array<TrustManager> {
        return try {
            val keyStore = KeyStore.getInstance(trustStore.keyStoreType).apply {
                load(trustStore.keyStoreInputStream, trustStore.keyStorePassword.toCharArray())
            }
            TrustManagerFactory.getInstance("X509").run {
                init(keyStore)
                trustManagers
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
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

}