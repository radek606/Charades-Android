package com.ick.kalambury

import android.os.Parcelable
import androidx.annotation.Keep
import com.ick.kalambury.entities.GameDataProtos
import com.ick.kalambury.wordsrepository.Language
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class GameConfig(
    val gameMode: GameMode = GameMode.NONE,
    val isHost: Boolean = false,
    val roundTime: Int = 0,
    val pointsLimit: Int = 0,
    val playerChooseMethod: PlayerChooseMethod = PlayerChooseMethod.GUESSING_PLAYER,
    val language: Language = Language.EN,
    val categories: List<String> = listOf(),
    var name: String? = null,
) : Parcelable {

    fun toProto(): GameDataProtos.GameConfig {
        return GameDataProtos.GameConfig.newBuilder()
            .setPointsLimit(pointsLimit)
            .setRoundTime(roundTime)
            .setLanguage(language.toString())
            .setChooseMethod(GameDataProtos.GameConfig.PlayerChooseMethod.valueOf(playerChooseMethod.name))
            .apply { this@GameConfig.name?.let { name = it } }
            .build()
    }

    companion object {

        fun fromProto(config: GameDataProtos.GameConfig): GameConfig {
            return GameConfig(
                    roundTime = config.roundTime,
                    pointsLimit = config.pointsLimit,
                    language = Language.forLanguageName(config.language),
                    playerChooseMethod = PlayerChooseMethod.valueOf(config.chooseMethod.name),
                    name = config.name
            )
        }

    }

}