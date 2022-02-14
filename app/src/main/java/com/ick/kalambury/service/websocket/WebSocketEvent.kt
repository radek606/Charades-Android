package com.ick.kalambury.service.websocket

import com.ick.kalambury.service.ConnectionEvent

sealed class WebSocketEvent : ConnectionEvent() {

    object Opened : WebSocketEvent()
    class Closing(val code: Int, val reason: String) : WebSocketEvent()
    class Closed(val code: Int, val reason: String) : WebSocketEvent()
    class Failure(val throwable: Throwable) : WebSocketEvent()

}


