package com.ick.kalambury.net

import okhttp3.OkHttpClient

interface HttpClientFactory {
    fun create(trustStore: TrustStore, credentials: CredentialsProvider? = null): OkHttpClient
}