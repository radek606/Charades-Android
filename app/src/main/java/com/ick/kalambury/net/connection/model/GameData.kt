package com.ick.kalambury.net.connection.model

import com.ick.kalambury.GameConfig
import com.ick.kalambury.entities.GameDataProtos
import com.ick.kalambury.list.model.Player

class GameData (
        private val originalMessage: GameDataProtos.GameData?,
        private val actions: Int,
        val actionData: String?,
        val config: GameConfig?,
        val gameStateData: GameStateData?,
        val players: Map<String, Player>,
        val messages: List<ChatMessage>,
        val drawables: List<DrawableData>,
) {

    fun hasAction(action: Int) = actions and action == action

    fun getMessage(type: GameDataProtos.ChatMessage.Type): ChatMessage? {
        return messages.filter { m: ChatMessage -> m.type == type }.getOrNull(0)
    }

    fun toProto(): GameDataProtos.GameData {
        val builder: GameDataProtos.GameData.Builder =
            originalMessage?.toBuilder() ?: GameDataProtos.GameData.newBuilder()
        builder.action = actions
        actionData?.let { builder.actionData = it }
        config?.let { builder.config = it.toProto() }
        gameStateData?.let { builder.gameState = it.toProto() }
        players.forEach { (k, v) -> builder.putPlayers(k, v.toProto()) }
        messages.forEach { m -> builder.addMessages(m.toProto()) }
        drawables.forEach { d -> builder.addDrawables(d.toProto()) }
        return builder.build()
    }

    override fun toString() = buildString {
        append("GameData{actions=[")
        when {
            hasAction(INITIAL_DATA)       -> append("INITIAL_DATA,")
            hasAction(PLAYER_UPDATE)      -> append("PLAYER_UPDATE,")
            hasAction(INITIAL_DATA)       -> append("INITIAL_DATA,")
            hasAction(PLAYER_UPDATE)      -> append("PLAYER_UPDATE,")
            hasAction(GAME_STATE_CHANGE)  -> append("GAME_STATE_CHANGE,")
            hasAction(GAME_FINISH)        -> append("GAME_FINISH,")
            hasAction(CHAT_MESSAGE)       -> append("CHAT_MESSAGE,")
            hasAction(PLAYER_READY)       -> append("PLAYER_READY,")
            hasAction(CLEAR_SCREEN)       -> append("CLEAR_SCREEN,")
            hasAction(ADD_NEW_OBJECT)     -> append("ADD_NEW_OBJECT,")
            hasAction(DELETE_LAST_OBJECT) -> append("DELETE_LAST_OBJECT,")
            hasAction(ABANDON_DRAWING)    -> append("ABANDON_DRAWING,")
            hasAction(TIMER)              -> append("TIMER,")
            hasAction(CONTINUE)           -> append("AGREE_CONTINUE,")
            hasAction(QUIT_GAME)          -> append("QUIT_GAME,")
        }
        deleteCharAt(length - 1)
        append("]}")
    }

    class Builder {

        private var originalMessage: GameDataProtos.GameData? = null
        private var actions: Int = 0
        private var actionData: String? = null
        private var config: GameConfig? = null
        private var gameStateData: GameStateData? = null
        private var players: MutableMap<String, Player> = mutableMapOf()
        private var messages: MutableList<ChatMessage> = mutableListOf()
        private var drawables: MutableList<DrawableData> = mutableListOf()

        constructor()

        constructor(action: Int) {
            actions = action
        }

        constructor(gameData: GameDataProtos.GameData) {
            originalMessage = gameData
            actions = gameData.action
        }

        fun addAction(action: Int): Builder {
            actions = actions or action
            return this
        }

        fun withActionData(data: String): Builder {
            actionData = data
            return this
        }

        fun withConfig(config: GameConfig): Builder {
            this.actions = actions or INITIAL_DATA
            this.config = config
            return this
        }

        fun withGameState(gameStateData: GameStateData): Builder {
            this.actions = actions or GAME_STATE_CHANGE
            this.gameStateData = gameStateData
            return this
        }

        fun withPlayers(players: Map<String, Player>): Builder {
            this.actions = actions or PLAYER_UPDATE
            this.players.putAll(players)
            return this
        }

        fun putPlayer(player: Player): Builder {
            this.actions = actions or PLAYER_UPDATE
            this.players[player.id] = player
            return this
        }

        fun withChatMessages(vararg messages: ChatMessage): Builder {
            this.actions = actions or CHAT_MESSAGE
            this.messages.addAll(messages)
            return this
        }

        fun addChatMessage(message: ChatMessage): Builder {
            this.actions = actions or CHAT_MESSAGE
            this.messages.add(message)
            return this
        }

        fun withDrawables(drawables: List<DrawableData>): Builder {
            this.actions = actions or ADD_NEW_OBJECT
            this.drawables.addAll(drawables)
            return this
        }

        fun addDrawable(drawable: DrawableData): Builder {
            this.actions = actions or ADD_NEW_OBJECT
            this.drawables.add(drawable)
            return this
        }

        fun build(): GameData {
            return GameData(originalMessage, actions, actionData, config, gameStateData, players, messages, drawables)
        }
    }

    companion object {

        //server/host only generated actions
        const val INITIAL_DATA = 1 shl 1
        const val PLAYER_UPDATE = 1 shl 2
        const val GAME_STATE_CHANGE = 1 shl 3
        const val GAME_FINISH = 1 shl 4
        const val TIMER = 1 shl 15

        //client or server/host generated actions
        const val CHAT_MESSAGE = 1 shl 7

        //client only generated actions
        const val PLAYER_READY = 1 shl 6
        const val CLEAR_SCREEN = 1 shl 8
        const val ADD_NEW_OBJECT = 1 shl 9
        const val DELETE_LAST_OBJECT = 1 shl 10
        const val ABANDON_DRAWING = 1 shl 11
        const val CONTINUE = 1 shl 12
        const val QUIT_GAME = 1 shl 13

        //table operator dedicated actions
        const val KICK_PLAYER = 1 shl 14

        fun action(action: Int, actionData: String? = null): GameData {
            return Builder(action).apply {
                actionData?.let { withActionData(it) }
            }.build()
        }

        fun drawable(drawable: DrawableData) = Builder().addDrawable(drawable).build()

        fun message(message: ChatMessage) = Builder().addChatMessage(message).build()

        fun fromProto(gameData: GameDataProtos.GameData): GameData {
            val builder = Builder(gameData)

            if (gameData.hasActionData()) {
                builder.withActionData(gameData.actionData)
            }
            if (gameData.hasConfig()) {
                builder.withConfig(GameConfig.fromProto(gameData.config))
            }
            if (gameData.hasGameState()) {
                builder.withGameState(GameStateData.fromProto(gameData.gameState))
            }

            gameData.playersMap.forEach { (_, v) -> builder.putPlayer(Player.fromProto(v)) }
            gameData.messagesList.forEach { m -> builder.addChatMessage(ChatMessage.fromProto(m)) }
            gameData.drawablesList.forEach { d -> builder.addDrawable(DrawableData.fromProto(d)) }

            return builder.build()
        }

    }
}