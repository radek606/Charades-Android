package com.ick.kalambury.service

import com.ick.kalambury.GameConfig
import com.ick.kalambury.net.connection.model.GameData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

interface GameHandler {

    enum class State {
        CREATED, CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED
    }

    var config: GameConfig

    /**
     * Indicates that we are ready to send/handle game events.
     * Meant to be called after game fragment is loaded and game event flowable is subscribed.
     */
    fun ready(): Completable

    /**
     * Initiate underlying connection to host/server.
     *
     * Returned Completable will complete as soon as connection process will be successfully initiated,
     * but NOT connected yet.
     *
     * Proper connection result will be emitted as GameEventData through Flowable returned by getGameEvents()
     *
     * Default endpoint parameter is meant to be used with 'host' handlers only
     */
    fun connect(endpoint: Endpoint = Endpoint.DEFAULT): Completable

    fun handleLocalGameData(gameData: GameData)
    fun getGameEvents(): Flowable<GameEvent>

    fun finish()

}