package com.ick.kalambury.service.websocket

import com.ick.kalambury.service.RxClientConnection
import io.reactivex.rxjava3.core.Completable

interface RxWebSocket : RxClientConnection<WebSocketEvent> {

    fun connect(connectionUrl: String): Completable

    fun close(code: Int = 1000, reason: String = "Bye")

}