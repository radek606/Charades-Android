package com.ick.kalambury.service

import io.reactivex.rxjava3.core.Completable

interface RxClientConnection<T> : RxConnection<T> {

    fun send(endpointId: String, message: ByteArray): Completable

}