package com.ick.kalambury.service

import com.ick.kalambury.GameConfig
import com.ick.kalambury.entities.GameDataProtos
import com.ick.kalambury.entities.content
import com.ick.kalambury.entities.envelope
import com.ick.kalambury.logging.Log
import com.ick.kalambury.net.connection.model.GameData
import com.ick.kalambury.util.logTag
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

abstract class BaseGameHandler<T : RxConnection<out ConnectionEvent>> constructor(connection: T): GameHandler {

    private val gameStateEvents: PublishProcessor<GameEvent> = PublishProcessor.create()

    val handlerThreadScheduler: Scheduler =
        Schedulers.from(Executors.newSingleThreadExecutor { r ->
            Thread(r, "EventProcessingThread")
        })

    var state: GameHandler.State = GameHandler.State.CREATED
        set(value) {
            Log.d(logTag(), "State change: $field -> $value")
            field = value
        }

    override lateinit var config: GameConfig

    val disposables: CompositeDisposable = CompositeDisposable()
    var gameTimer: RxCountDownTimer? = null

    init {
        disposables += connection.messageEvent()
            .observeOn(handlerThreadScheduler)
            .subscribe { handleRemoteMessageEvent(it.sourceId, fromBytes(it.payload)) }

        Log.d(logTag(), "Handler created.")
    }

    override fun getGameEvents(): Flowable<GameEvent> {
        return gameStateEvents.onBackpressureBuffer()
    }

    abstract fun handleRemoteMessageEvent(endpointId: String?, message: GameData)

    open fun setGameTimer(seconds: Int) {
        gameTimer?.cancel()

        if (seconds <= 0) {
            Log.w(logTag(), "Tried starting game timer with value: $seconds! Ignoring...")
            return
        }

        Log.d(logTag(), "Starting game timer")

        gameTimer = object : RxCountDownTimer(seconds.toLong(), 1, TimeUnit.SECONDS, handlerThreadScheduler) {
            override fun onTick(tick: Long) {
                sendToUI(GameData.action(GameData.TIMER, tick.toString()))
            }

            override fun onFinish() { }
        }.start()
    }

    fun cancelGameTimer() {
        gameTimer?.let {
            Log.d(logTag(), "Cancelling game timer")
            it.cancel()
        }
    }

    fun notifyUI() {
        sendToUI(GameEvent(getStateForUi(state)))
    }

    fun sendToUI(state: GameEvent.State) {
        sendToUI(GameEvent(state))
    }

    fun sendToUI(data: GameConfig) {
        sendToUI(GameEvent(getStateForUi(state), config = data))
    }
    
    fun sendToUI(data: GameData) {
        sendToUI(GameEvent(getStateForUi(state), gameData = data))
    }

    fun sendToUI(data: GameEvent) {
        gameStateEvents.onNext(data)
    }

    private fun getStateForUi(state: GameHandler.State): GameEvent.State {
        return when (state) {
            GameHandler.State.CONNECTING -> GameEvent.State.CONNECTING
            GameHandler.State.CONNECTED -> GameEvent.State.CONNECTED
            GameHandler.State.DISCONNECTING,
            GameHandler.State.DISCONNECTED -> GameEvent.State.DISCONNECTED
            else -> throw IllegalArgumentException("Unknown state: $state")
        }
    }

    private fun fromBytes(bytes: ByteArray): GameData {
        return GameData.fromProto(
            GameDataProtos.Content.parseFrom(
            GameDataProtos.Envelope.parseFrom(bytes)
                .content)
            .data)
    }

    fun toBytes(message: GameData): ByteArray {
        return envelope {
            type = GameDataProtos.Envelope.Type.CONTENT
            content = content {
                data = message.toProto()
            }.toByteString()
        }.toByteArray()
    }

}