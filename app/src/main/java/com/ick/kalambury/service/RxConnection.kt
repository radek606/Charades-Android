package com.ick.kalambury.service

import io.reactivex.rxjava3.core.Flowable

interface RxConnection<T> {

    fun connectionEvents(): Flowable<T>
    fun messageEvent(): Flowable<MessageEvent>

}