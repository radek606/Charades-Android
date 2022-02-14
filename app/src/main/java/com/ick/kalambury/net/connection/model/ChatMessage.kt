package com.ick.kalambury.net.connection.model

import com.ick.kalambury.entities.GameDataProtos
import com.ick.kalambury.entities.GameDataProtos.ChatMessage.Type
import com.ick.kalambury.entities.GameDataProtos.ChatMessage.newBuilder

class ChatMessage private constructor(
    val type: Type,
    val source: String? = null,
    val body: String? = null,
) {

    fun toProto(): GameDataProtos.ChatMessage {
        val builder = newBuilder()
        builder.type = type
        source?.let { builder.source = it }
        body?.let { builder.body = it }
        return builder.build()
    }

    companion object {

        fun playerJoin(playerId: String) =
            ChatMessage(Type.PLAYER_JOIN, playerId, null)

        fun playerDraw(playerId: String) =
            ChatMessage(Type.PLAYER_DRAW, playerId)

        fun playerGuess(playerId: String, word: String) =
            ChatMessage(Type.PLAYER_GUESS, playerId, word)

        fun playerAbandon(playerId: String) =
            ChatMessage(Type.PLAYER_ABANDON, playerId)

        fun playerInactive(playerId: String) =
            ChatMessage(Type.PLAYER_INACTIVE, playerId)

        fun playerLeft(playerId: String) =
            ChatMessage(Type.PLAYER_LEFT, playerId)

        fun playerAnswer(playerId: String, answer: String) =
            ChatMessage(Type.PLAYER_ANSWER, playerId, answer)

        fun playerWrite(playerId: String, text: String) =
            ChatMessage(Type.PLAYER_WRITE, playerId, text)

        fun playerWon(playerId: String) =
            ChatMessage(Type.PLAYER_WON, playerId)

        fun waiting() = ChatMessage(Type.WAITING)

        fun inactivityWarn() = ChatMessage(Type.INACTIVITY_WARN)

        fun littleTimeWarn() = ChatMessage(Type.LITTLE_TIME_WARN)

        fun timeIsUp() = ChatMessage(Type.TIME_IS_UP)

        fun closeEnoughAnswer(body: String) =
            ChatMessage(Type.CLOSE_ENOUGH_ANSWER, null, body)

        fun hint(body: String) = ChatMessage(Type.HINT, null, body)

        fun word(body: String) = ChatMessage(Type.PASSWORD, null, body)

        fun fromProto(message: GameDataProtos.ChatMessage) =
            ChatMessage(message.type, message.source, message.body)

    }

}
