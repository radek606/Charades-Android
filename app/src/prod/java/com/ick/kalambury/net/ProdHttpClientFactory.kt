package com.ick.kalambury.net

import android.content.Context
import android.os.Build
import com.ick.kalambury.BuildConfig
import okhttp3.*
import okhttp3.internal.immutableListOf
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.time.Duration
import javax.inject.Inject
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class ProdHttpClientFactory @Inject constructor() : HttpClientFactory {

    override fun create(context: Context, credentials: CredentialsProvider?): OkHttpClient {
        return try {
            val managers: Array<TrustManager> =
                createTrustManagers(BackendServiceTrustStore(context))
            val sslContext: SSLContext = SSLContext.getInstance("TLS")
            sslContext.init(null, managers, null)
            val builder = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), managers[0] as X509TrustManager)
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
            val keyStoreInputStream: InputStream = trustStore.keyStoreInputStream
            val keyStore: KeyStore = KeyStore.getInstance(trustStore.keyStoreType)
            keyStore.load(keyStoreInputStream, trustStore.keyStorePassword.toCharArray())
            val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance("X509")
            trustManagerFactory.init(keyStore)
            trustManagerFactory.getTrustManagers()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun createAuthorizationHeaderInterceptor(credentialsProvider: CredentialsProvider): Interceptor {
        val credentials: String = Credentials.basic(credentialsProvider.user,
            credentialsProvider.password, StandardCharsets.UTF_8)
        return Interceptor { chain ->
            chain.proceed(chain.request().newBuilder()
                .addHeader("Authorization", credentials)
                .build())
        }
    }

    private fun createUserAgentHeaderInterceptor(): Interceptor {
        val userAgent =
            "Kalambury " + BuildConfig.VERSION_NAME.toString() + " (API " + Build.VERSION.SDK_INT.toString() + ")"
        return Interceptor { chain ->
            chain.proceed(chain.request().newBuilder()
                .addHeader("User-Agent", userAgent)
                .build())
        }
    }

    companion object {
        private const val TIMEOUT_SECONDS = 60L
    }
}