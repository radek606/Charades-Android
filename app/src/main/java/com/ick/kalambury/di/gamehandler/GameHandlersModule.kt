package com.ick.kalambury.di.gamehandler

import com.ick.kalambury.GameMode
import com.ick.kalambury.net.api.RestApiManager
import com.ick.kalambury.service.*
import com.ick.kalambury.service.nearbyconnections.RxClientNearbyConnections
import com.ick.kalambury.service.nearbyconnections.RxHostNearbyConnections
import com.ick.kalambury.service.websocket.RxWebSocket
import com.ick.kalambury.wordsrepository.WordsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@InstallIn(SingletonComponent::class)
@Module
class GameHandlersModule {

    @Provides
    @IntoMap
    @GameHandlerKey(mode = GameMode.DRAWING_LOCAL, host = true)
    fun provideLocalHostHandler(
        connection: RxHostNearbyConnections,
        wordsRepository: WordsRepository,
    ): GameHandler {
        return LocalGameHostHandler(connection, wordsRepository)
    }

    @Provides
    @IntoMap
    @GameHandlerKey(mode = GameMode.DRAWING_LOCAL, host = false)
    fun provideLocalClientHandler(connection: RxClientNearbyConnections): GameHandler {
        return LocalGameClientHandler(connection)
    }

    @Provides
    @IntoMap
    @GameHandlerKey(mode = GameMode.DRAWING_ONLINE, host = true)
    fun provideOnlineHostHandler(apiManager: RestApiManager, connection: RxWebSocket): GameHandler {
        return OnlineGameHostHandler(apiManager, OnlineGameClientHandler(connection))
    }

    @Provides
    @IntoMap
    @GameHandlerKey(mode = GameMode.DRAWING_ONLINE, host = false)
    fun provideOnlineClientHandler(connection: RxWebSocket): GameHandler {
        return OnlineGameClientHandler(connection)
    }

}

@InstallIn(SingletonComponent::class)
@Module
abstract class GameHandlerRepositoryModule {

    @Binds
    abstract fun bindGameHandlerRepository(repository: GameHandlerRepositoryImpl): GameHandlerRepository

}

