package com.ick.kalambury.remoteconfig

import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateData(val updates: List<Update>) {

    @Serializable
    data class Update(val versionCode: Int, val priority: Int)

}
