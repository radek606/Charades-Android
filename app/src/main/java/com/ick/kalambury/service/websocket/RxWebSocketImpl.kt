package com.ick.kalambury.service.websocket

import com.ick.kalambury.net.connection.exceptions.ConnectionException
import com.ick.kalambury.net.connection.exceptions.MessageSendFailedException
import com.ick.kalambury.service.MessageEvent
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import javax.inject.Inject

class RxWebSocketImpl @Inject constructor(private val httpClient: OkHttpClient) : RxWebSocket {

    private var webSocket: WebSocket? = null

    private val connectionEventProcessor: PublishProcessor<WebSocketEvent> by lazy { PublishProcessor.create() }
    private val messageEventProcessor: PublishProcessor<MessageEvent> by lazy { PublishProcessor.create() }

    override fun connectionEvents(): Flowable<WebSocketEvent> {
        return connectionEventProcessor.onBackpressureBuffer()
    }

    override fun messageEvent(): Flowable<MessageEvent> {
        return messageEventProcessor.onBackpressureBuffer()
    }

    @Synchronized
    override fun connect(connectionUrl: String): Completable {
        val request = Request.Builder()
            .url(connectionUrl)
            .build()

        Log.d(logTag(), request.toString())

        return Completable.fromAction { webSocket = httpClient.newWebSocket(request, webSocketListener) }
    }

    override fun send(endpointId: String, message: ByteArray): Completable {
        return Completable.create {
            webSocket?.apply {
                if (send(message.toByteString())) {
                    it.onComplete()
                } else {
                    it.onError(MessageSendFailedException())
                }
            } ?: it.onError(IllegalStateException("WebSocket not connected!"))
        }
    }

    override fun close(code: Int, reason: String) {
        connectionEventProcessor.onComplete()
        messageEventProcessor.onComplete()
        webSocket?.close(code, reason)
        webSocket = null
    }

    private val webSocketListener: WebSocketListener =
        object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                connectionEventProcessor.onNext(WebSocketEvent.Opened)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                messageEventProcessor.onNext(MessageEvent(payload = bytes.toByteArray()))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                connectionEventProcessor.onNext(WebSocketEvent.Closing(code, reason))
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                connectionEventProcessor.onNext(WebSocketEvent.Closed(code, reason))
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                connectionEventProcessor.onNext(WebSocketEvent.Failure(ConnectionException(t)))
            }
        }

}