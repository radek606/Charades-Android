package com.ick.kalambury.service

import android.util.Base64
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.entities.connectionData
import com.ick.kalambury.net.connection.SupportedVersionInfo
import com.ick.kalambury.net.connection.User
import com.ick.kalambury.net.connection.model.GameData
import com.ick.kalambury.service.websocket.RxWebSocket
import com.ick.kalambury.service.websocket.WebSocketEvent
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import com.ick.kalambury.util.toBase64
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException

class OnlineGameClientHandler(
    socket: RxWebSocket,
) : ClientGameHandler<RxWebSocket>(socket) {

    override lateinit var localUser: User
    override lateinit var hostEndpoint: Endpoint

    init {
        disposables += connection.connectionEvents()
            .observeOn(handlerThreadScheduler)
            .subscribe(::handleWebSocketEvents)
    }

    override fun connect(localUser: User, endpoint: Endpoint): Completable {
        if (state >= GameHandler.State.CONNECTING) {
            Log.w(logTag, "connect() - Already connecting or connected! Ignoring...")
            return Completable.complete()
        }

        Log.d(logTag, "connect()")

        this.localUser = localUser
        hostEndpoint = endpoint

        state = GameHandler.State.CONNECTING
        notifyUI()

        val connectionData = connectionData {
            this.endpoint = endpoint.id
            nickname = localUser.nickname
            uuid = localUser.uuid
            version = BuildConfig.VERSION_CODE
        }.toBase64(Base64.NO_WRAP or Base64.URL_SAFE)

        val connectionUrl = BuildConfig.SERVICE_URL
            .replace("https://", "wss://")
            .replace("http://", "ws://")
            .plus(String.format(GAME_SOCKET_PATH, connectionData))

        return connection.connect(connectionUrl)
            .subscribeOn(handlerThreadScheduler)
    }

    override fun ready(): Completable {
        return sendCompletable(GameData.action(GameData.PLAYER_READY))
            .subscribeOn(handlerThreadScheduler)
    }

    override fun finish() {
        if (state >= GameHandler.State.DISCONNECTED) {
            Log.w(logTag, "finish() - Already disconnected! Ignoring...")
            return
        }

        Log.d(logTag, "finish()")

        disposables.dispose()

        cancelGameTimer()

        if (state == GameHandler.State.CONNECTED) {
            state = GameHandler.State.DISCONNECTING

            sendCompletable(GameData.action(GameData.QUIT_GAME))
                .subscribeOn(handlerThreadScheduler)
                .onErrorComplete()
                .andThen {
                    connection.close()
                    state = GameHandler.State.DISCONNECTED
                    it.onComplete()
                }
                .doFinally { handlerThreadScheduler.shutdown() }
                .subscribeBy(onError = { Log.w(logTag, "Failed finishing game handler.", it) })
        } else {
            handlerThreadScheduler.shutdown()
            state = GameHandler.State.DISCONNECTED
        }
    }

    private fun handleWebSocketEvents(event: WebSocketEvent) {
        when (event) {
            is WebSocketEvent.Opened -> handleOpenedEvent()
            is WebSocketEvent.Closing -> handleClosingEvent(event)
            is WebSocketEvent.Closed -> handleClosedEvent(event)
            is WebSocketEvent.Failure -> handleFailureEvent(event)
        }
    }

    private fun handleOpenedEvent() {
        Log.d(logTag, "handleOpenedEvent() - Connected to remote.")
    }

    private fun handleClosingEvent(closingEvent: WebSocketEvent.Closing) {
        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag,
                "handleClosingEvent() - Already disconnecting or disconnected. Ignoring..."
            )
            return
        }

        Log.d(logTag, "handleClosingEvent()")

        connection.close()
        state = GameHandler.State.DISCONNECTED
        handleRemoteDisconnected(closingEvent.code, closingEvent.reason)
    }

    private fun handleClosedEvent(closedEvent: WebSocketEvent.Closed) {
        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag,
                "handleClosedEvent() - Already disconnecting or disconnected. Ignoring..."
            )
            return
        }

        Log.d(logTag, "handleClosedEvent()")

        state = GameHandler.State.DISCONNECTED
        handleRemoteDisconnected(closedEvent.code, closedEvent.reason)
    }

    private fun handleFailureEvent(failureEvent: WebSocketEvent.Failure) {
        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag,
                "handleFailureEvent() - Already disconnecting or disconnected. Ignoring..."
            )
            return
        }

        Log.w(logTag, "handleFailureEvent()", failureEvent.throwable)

        state = GameHandler.State.DISCONNECTED
        sendToUI(GameEvent.State.NETWORK_FAILURE)
    }

    private fun handleRemoteDisconnected(code: Int, reason: String) {
        val data = when (code) {
            1003 -> {
                // FIXME find better way to handle close reasons
                try {
                    val info = Json.decodeFromString<SupportedVersionInfo>(reason)
                    GameEvent(
                        GameEvent.State.UNSUPPORTED_VERSION,
                        supportedVersionInfo = info
                    )
                } catch (e: SerializationException) {
                    when (reason) {
                        "102" -> GameEvent(GameEvent.State.PLAYER_LIMIT_EXCEEDED)
                        "103" -> GameEvent(GameEvent.State.TABLE_NOT_FOUND)
                        else -> GameEvent(GameEvent.State.GENERIC_FAILURE)
                    }
                }
            }
            1008 -> GameEvent(GameEvent.State.KICKED)
            else -> GameEvent(GameEvent.State.NETWORK_FAILURE)
        }
        sendToUI(data)
    }

    override fun startDiscovery(duration: Long) { /* no-op */ }
    override fun stopDiscovery() { /* no-op */ }

    companion object {
        private const val GAME_SOCKET_PATH = "/v2/game/websocket?data=%s"
    }

}