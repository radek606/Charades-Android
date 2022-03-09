package com.ick.kalambury.net.api.dto

import androidx.annotation.Keep
import com.ick.kalambury.GameConfig
import com.ick.kalambury.PlayerChooseMethod
import com.ick.kalambury.TableKind
import kotlinx.serialization.Serializable

@Serializable
data class TableConfigDto(
    val kind: TableKind,
    val playerChooseMethod: PlayerChooseMethod,
    val pointsLimit: Int,
    val roundTime: Int,
    val language: String,
    val categories: List<String>
) {

    companion object {

        fun fromGameConfig(config: GameConfig): TableConfigDto {
            return TableConfigDto(TableKind.PUBLIC,
                config.playerChooseMethod,
                config.pointsLimit,
                config.roundTime,
                config.language.toString(),
                config.categories
            )
        }

    }

}