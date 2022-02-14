package com.ick.kalambury.ads

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.ick.kalambury.R
import com.ick.kalambury.util.inflate

class ListNativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var nativeAdView: NativeAdView
    private lateinit var primaryView: TextView
    private lateinit var secondaryView: TextView
    private lateinit var iconView: ImageView
    private lateinit var callToActionView: ImageButton

    init {
        context.inflate(R.layout.native_ad_view, this, true)
    }

    fun setNativeAd(nativeAd: NativeAd) {
        primaryView.text = nativeAd.headline
        secondaryView.text = nativeAd.body
        nativeAd.icon?.let {
            iconView.visibility = VISIBLE
            iconView.setImageDrawable(it.drawable)
        } ?: run { iconView.visibility = GONE }

        nativeAdView.setNativeAd(nativeAd)
    }

    public override fun onFinishInflate() {
        super.onFinishInflate()
        nativeAdView = findViewById(R.id.native_ad_view)
        primaryView = findViewById(R.id.primary)
        secondaryView = findViewById(R.id.secondary)
        callToActionView = findViewById(R.id.cta)
        iconView = findViewById(R.id.icon)

        nativeAdView.callToActionView = callToActionView
        nativeAdView.headlineView = primaryView
        nativeAdView.bodyView = secondaryView
        nativeAdView.iconView = iconView
    }
}