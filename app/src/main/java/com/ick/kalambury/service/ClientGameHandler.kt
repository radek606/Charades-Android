package com.ick.kalambury.service

import com.ick.kalambury.logging.Log
import com.ick.kalambury.net.connection.User
import com.ick.kalambury.net.connection.model.GameData
import com.ick.kalambury.util.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy

abstract class ClientGameHandler<T : RxClientConnection<out ConnectionEvent>> constructor(val connection: T) :
    BaseGameHandler<T>(connection) {

    abstract var localUser: User
    abstract var hostEndpoint: Endpoint

    abstract fun startDiscovery(duration: Long)
    abstract fun stopDiscovery()

    override fun handleRemoteMessageEvent(endpointId: String?, message: GameData) {
        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(logTag(), "handleRemoteMessageEvent() - Already disconnecting or disconnected. Ignoring...")
            return
        }

        Log.d(logTag(), "handleRemoteMessageEvent(): $message")

        if (message.hasAction(GameData.INITIAL_DATA)) {
            state = GameHandler.State.CONNECTED
            return sendToUI(message.config ?: error("Missing game config in initial data!"))
        }
        if (message.hasAction(GameData.QUIT_GAME)) {
            state = GameHandler.State.DISCONNECTING
            return sendToUI(GameEvent.State.HOST_FINISHED)
        }
        if (message.hasAction(GameData.GAME_STATE_CHANGE)) {
            val gameStateData = message.gameStateData
            if (localUser.uuid == gameStateData?.drawingPlayerId) {
                setGameTimer(gameStateData.timeLeft)
            } else {
                cancelGameTimer()
            }
        }

        sendToUI(message)
    }

    override fun handleLocalGameData(gameData: GameData) {
        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag(),
                "handleLocalGameData() - Already disconnecting or disconnected! Ignoring..."
            )
            return
        }

        Log.d(logTag(), "handleLocalGameData(): $gameData")

        send(gameData)
    }

    fun send(message: GameData) {
        disposables += sendCompletable(message)
            .subscribeBy(onError = { Log.w(logTag(), "Send failed: $message") })
    }

    fun sendCompletable(message: GameData): Completable {
        return connection.send(hostEndpoint.id, message = toBytes(message))
    }

}