package com.ick.kalambury.net.connection.model

import com.ick.kalambury.entities.GameDataProtos
import com.ick.kalambury.service.GameState

class GameStateData(
    val state: GameState,
    val operatorPlayerId: String?,
    val drawingPlayerId: String?,
    val winnerPlayerId: String?,
    val wordToGuess: String?,
    val category: String?,
    val timeLeft: Int,
) {

    fun toProto(): GameDataProtos.GameState {
        val builder = GameDataProtos.GameState.newBuilder()
        builder.state = translateState(state)
        builder.timeLeft = timeLeft
        operatorPlayerId?.let { builder.operatorPlayerId = it }
        drawingPlayerId?.let { builder.drawingPlayerId = it }
        winnerPlayerId?.let { builder.winnerPlayerId = it }
        wordToGuess?.let { builder.wordToGuess = it }
        category?.let { builder.category = it }
        operatorPlayerId?.let { builder.operatorPlayerId = it }
        return builder.build()
    }

    private fun translateState(state: GameState?): GameDataProtos.GameState.State {
        return when (state) {
            GameState.NO_PLAYERS -> GameDataProtos.GameState.State.NO_PLAYERS
            GameState.WAITING -> GameDataProtos.GameState.State.WAITING
            GameState.IN_GAME -> GameDataProtos.GameState.State.IN_GAME
            GameState.FINISHED -> GameDataProtos.GameState.State.FINISHED
            else -> throw IllegalArgumentException("Unknown state: $state")
        }
    }

    class Builder(private val state: GameState) {

        private var operatorPlayerId: String? = null
        private var drawingPlayerId: String? = null
        private var winnerPlayerId: String? = null
        private var wordToGuess: String? = null
        private var category: String? = null
        private var timeLeft = 0

        fun setOperatorPlayerId(playerId: String?): Builder {
            this.operatorPlayerId = playerId
            return this
        }

        fun setDrawingPlayerId(playerId: String?): Builder {
            this.drawingPlayerId = playerId
            return this
        }

        fun setWinnerPlayerId(playerId: String?): Builder {
            this.winnerPlayerId = playerId
            return this
        }

        fun setWordToGuess(wordToGuess: String?): Builder {
            this.wordToGuess = wordToGuess
            return this
        }

        fun setCategory(category: String?): Builder {
            this.category = category
            return this
        }

        fun setTimeLeft(timeLeft: Int): Builder {
            this.timeLeft = timeLeft
            return this
        }

        fun build() = GameStateData(state, operatorPlayerId, drawingPlayerId, winnerPlayerId,
            wordToGuess, category, timeLeft)
    }

    companion object {

        fun newBuilder(state: GameState) = Builder(state)

        fun fromProto(gameState: GameDataProtos.GameState): GameStateData {
            return newBuilder(translateState(gameState.state))
                .setOperatorPlayerId(gameState.operatorPlayerId)
                .setDrawingPlayerId(gameState.drawingPlayerId)
                .setWinnerPlayerId(gameState.winnerPlayerId)
                .setWordToGuess(gameState.wordToGuess)
                .setCategory(gameState.category)
                .setTimeLeft(gameState.timeLeft)
                .build()
        }

        private fun translateState(state: GameDataProtos.GameState.State): GameState {
            return when (state) {
                GameDataProtos.GameState.State.NO_PLAYERS -> GameState.NO_PLAYERS
                GameDataProtos.GameState.State.WAITING -> GameState.WAITING
                GameDataProtos.GameState.State.IN_GAME -> GameState.IN_GAME
                GameDataProtos.GameState.State.FINISHED -> GameState.FINISHED
            }
        }

    }
}