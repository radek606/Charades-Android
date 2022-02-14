package com.ick.kalambury.service

import com.ick.kalambury.GameConfig
import com.ick.kalambury.GameMode
import com.ick.kalambury.di.gamehandler.GameHandlerKey
import com.ick.kalambury.di.gamehandler.GameHandlerKeyUtils
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

interface GameHandlerRepository {

    fun getHandler(): GameHandler
    fun createClientHandler(gameMode: GameMode): GameHandler
    fun createHostHandler(config: GameConfig): GameHandler

}

@Singleton
class GameHandlerRepositoryImpl @Inject constructor(
    private val gameHandlersMap: @JvmSuppressWildcards Map<GameHandlerKey, Provider<GameHandler>>,
) : GameHandlerRepository {

    private var gameHandler: GameHandler? = null

    override fun getHandler(): GameHandler {
        return gameHandler ?: throw IllegalStateException("No handler available")
    }

    @Synchronized
    override fun createClientHandler(gameMode: GameMode): GameHandler {
        return createHandlerInternal(GameHandlerKeyUtils.create(gameMode, false))
    }

    @Synchronized
    override fun createHostHandler(config: GameConfig): GameHandler {
        return createHandlerInternal(GameHandlerKeyUtils.create(config.gameMode, true))
            .apply { this.config = config }
    }

    @Synchronized
    private fun createHandlerInternal(key: GameHandlerKey): GameHandler {
        gameHandler?.finish()

        gameHandler = gameHandlersMap[key]?.get()
            ?: throw IllegalArgumentException("Unsupported game mode: $key")

        return gameHandler!!
    }

}