package com.ick.kalambury.util

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NetworkUtils @Inject constructor(@ApplicationContext val context: Context) {

    @Suppress("DEPRECATION")
    fun hasNetworkConnection(): Boolean {
        return context.getSystemService<ConnectivityManager>()
                ?.activeNetworkInfo?.isConnectedOrConnecting ?: false
    }

}
