package com.ick.kalambury.list.model

import com.ick.kalambury.net.api.dto.TableDto

data class TableData(
        override val id: String,
        override val text: String,
        val players: Int,
        val playersLimit: Int,
        val points: Int,
        val roundTime: Int,
        val lang: String,
        val operator: String?,
        override val selected: Boolean = false,
) : ListableData, Connectable(), Comparable<TableData> {

    constructor(dto: TableDto) : this(
        dto.id,
        dto.name,
        dto.playersCount,
        dto.maxPlayers,
        dto.pointsLimit,
        dto.roundTime,
        dto.language,
        dto.operatorName,
        false
    )

    override fun compareTo(other: TableData): Int {
        val playersCountCompare = other.players.compareTo(players)
        return if (playersCountCompare == 0) {
            text.compareTo(other.text)
        } else {
            playersCountCompare
        }
    }

}
