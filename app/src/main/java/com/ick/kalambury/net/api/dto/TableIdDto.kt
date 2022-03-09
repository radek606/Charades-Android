package com.ick.kalambury.net.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class TableIdDto(val tableId: String, val tableName: String)