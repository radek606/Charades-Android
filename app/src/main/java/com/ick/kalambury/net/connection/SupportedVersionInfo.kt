package com.ick.kalambury.net.connection

import kotlinx.serialization.Serializable

@Serializable
data class SupportedVersionInfo(val minVersionCode: Int = -1, val minVersionName: String = "")