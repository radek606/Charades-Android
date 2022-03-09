package com.ick.kalambury.net.api.dto

import com.ick.kalambury.TableKind
import kotlinx.serialization.Serializable

@Serializable
data class TableDto(val id: String,
               val kind: TableKind,
               val name: String,
               val pointsLimit: Int,
               val maxPlayers: Int,
               val playersCount: Int,
               val roundTime: Int,
               val language: String,
               val operatorName: String?,
)