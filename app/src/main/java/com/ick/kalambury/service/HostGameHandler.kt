package com.ick.kalambury.service

import com.ick.kalambury.net.connection.model.GameData
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy

abstract class HostGameHandler<T : RxHostConnection<out ConnectionEvent>> constructor(val connection: T) :
    BaseGameHandler<T>(connection) {

    fun broadcast(message: GameData, ids: List<String>) {
        disposables += broadcastCompletable(message, ids)
            .subscribeOn(handlerThreadScheduler)
            .subscribeBy(onError = { Log.w(logTag, "Send failed: $message", it) })
    }

    fun broadcastCompletable(message: GameData, ids: List<String>): Completable {
        if (ids.isEmpty()) return Completable.error(IllegalArgumentException("Empty ids list!"))

        return connection.broadcast(ids, toBytes(message))
    }

}