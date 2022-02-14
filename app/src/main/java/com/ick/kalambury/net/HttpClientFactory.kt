package com.ick.kalambury.net

import android.content.Context
import okhttp3.OkHttpClient

interface HttpClientFactory {
    fun create(context: Context, credentials: CredentialsProvider? = null): OkHttpClient
}