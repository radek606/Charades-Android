package com.ick.kalambury.drawing

import android.content.Context
import com.ick.kalambury.R
import com.ick.kalambury.entities.GameDataProtos.ChatMessage.Type.*
import com.ick.kalambury.net.connection.model.ChatMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MessageFormatter @Inject constructor(@ApplicationContext private val context: Context) {

    fun format(message: ChatMessage): String {
        return when (message.type) {
            PLAYER_JOIN -> context.getString(R.string.chat_msg_player_join, message.source)
            PLAYER_LEFT -> context.getString(R.string.chat_msg_player_left, message.source)
            PLAYER_KICKED -> context.getString(R.string.chat_msg_player_kicked, message.source)
            PLAYER_DRAW -> context.getString(R.string.chat_msg_player_draw, message.source)
            PLAYER_GUESS -> context.getString(R.string.chat_msg_player_guess, message.source, message.body)
            PLAYER_WRITE,
            PLAYER_ANSWER -> context.getString(R.string.chat_msg_player_answer, message.source, message.body)
            PLAYER_ABANDON -> context.getString(R.string.chat_msg_player_abandon, message.source)
            PLAYER_INACTIVE -> context.getString(R.string.chat_msg_player_inactive, message.source)
            PLAYER_WON -> context.getString(R.string.chat_msg_player_won, message.source)
            CLOSE_ENOUGH_ANSWER -> context.getString(R.string.chat_msg_close_answer, message.body)
            PASSWORD -> context.getString(R.string.chat_msg_password, message.body)
            WAITING -> context.getString(R.string.chat_msg_waiting)
            HINT -> context.getString(R.string.chat_msg_hint, message.body)
            TIME_IS_UP -> context.getString(R.string.chat_msg_time_up)
            INACTIVITY_WARN -> context.getString(R.string.chat_msg_inactivity_warn)
            LITTLE_TIME_WARN -> context.getString(R.string.chat_msg_little_time_warn)
            NEW_OPERATOR -> context.getString(R.string.chat_msg_new_operator, message.source)
            else -> context.getString(R.string.chat_msg_plain, message.body)
        }
    }

}