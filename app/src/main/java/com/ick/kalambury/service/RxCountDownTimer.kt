package com.ick.kalambury.service

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.subscribers.DisposableSubscriber
import java.util.concurrent.TimeUnit

abstract class RxCountDownTimer(
    private val count: Long,
    private val period: Long,
    private val unit: TimeUnit,
    private val observeOn: Scheduler
) {

    private var subscriber: DisposableSubscriber<Long>? = null
    private var cancelled: Boolean = false

    abstract fun onTick(tick: Long)
    abstract fun onFinish()

    @Synchronized
    fun start(): RxCountDownTimer {
        cancelled = false
        if (count <= 0) {
            onFinish()
            return this
        }

        subscriber = Flowable.intervalRange(0, count + 1, 0, period, unit)
            .map { count - it }
            .onBackpressureDrop()
            //buffer size set to 1 along with onBackpressureDrop() allows skipping subsequent values
            //if processing of single one takes longer than emit period.
            .observeOn(observeOn, false, 1)
            .subscribeWith(object : DisposableSubscriber<Long>() {
                override fun onStart() {
                    request(1)
                }

                override fun onNext(t: Long) {
                    if (!cancelled) {
                        onTick(t)
                        request(1)
                    }
                }

                override fun onError(t: Throwable) {
                    throw t
                }

                override fun onComplete() {
                    onFinish()
                }
            })

        return this
    }

    @Synchronized
    fun cancel() {
        cancelled = true
        subscriber?.dispose()
    }

}