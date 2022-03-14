package com.ick.kalambury.startup

import android.content.Context
import androidx.startup.Initializer
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag

class AdsInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        MobileAds.initialize(context) { status ->
            Log.d(logTag, buildString {
                appendLine("Mobile ads initialized. Adapters statuses:")
                status.adapterStatusMap.forEach {
                    append("    ").append(it.key).append(": ").append(it.value.initializationState)
                        .append(", latency: ").append(it.value.latency)
                        .append(", description: ").append(it.value.description).appendLine()
                }
            })
        }
        MobileAds.setRequestConfiguration(MobileAds.getRequestConfiguration()
            .toBuilder()
            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}