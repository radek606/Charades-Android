package com.ick.kalambury.service

import com.ick.kalambury.logging.Log
import com.ick.kalambury.util.logTag
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableSubscriber
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit

abstract class RxCountDownTimer(
    private val count: Long,
    private val countInterval: Long = 1L,
    private val unit: TimeUnit = TimeUnit.SECONDS,
    private val observeOn: Scheduler = Schedulers.computation()
) {

    private var subscription: Subscription? = null

    private var cancelled: Boolean = false

    abstract fun onTick(tick: Long)
    abstract fun onFinish()

    fun start(): RxCountDownTimer {
        cancelled = false
        if (count <= 0) {
            onFinish()
            return this
        }

        Flowable.intervalRange(0, count + 1, 0, countInterval, unit)
            .map { count - it }
            .onBackpressureLatest()
            //buffer size set to 1 along with onBackpressureLatest() allows to skip subsequent values
            //if processing of single one takes longer than emit period.
            .observeOn(observeOn, false, 1)
            .subscribe(object : FlowableSubscriber<Long> {
                override fun onSubscribe(s: Subscription) {
                    subscription = s
                    s.request(1)
                }

                override fun onNext(t: Long) {
                    if (!cancelled) {
                        onTick(t)
                        subscription?.request(1)
                    }
                }

                override fun onError(t: Throwable) {
                    Log.w(logTag(), "Unexpected error during counting!", t)
                    throw t
                }

                override fun onComplete() {
                    onFinish()
                }
            })

        return this
    }

    fun cancel() {
        cancelled = true
        subscription?.cancel()
    }

}