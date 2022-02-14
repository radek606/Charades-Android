package com.ick.kalambury.remoteconfig

data class AppUpdateData(val updates: List<Update> = listOf()) {

    data class Update(val versionCode: Int, val priority: Int)

}
