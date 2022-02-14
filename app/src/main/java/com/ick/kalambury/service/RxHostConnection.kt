package com.ick.kalambury.service

import io.reactivex.rxjava3.core.Completable

interface RxHostConnection<T> : RxConnection<T> {

    fun broadcast(endpointIds: List<String>, message: ByteArray): Completable

}