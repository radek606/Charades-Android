package com.ick.kalambury.service.nearbyconnections

import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.ick.kalambury.service.RxHostConnection
import io.reactivex.rxjava3.core.Completable

interface RxHostNearbyConnections : RxHostConnection<NearbyConnectionsEvent> {

    fun startAdvertising(
        endpointData: ByteArray,
        serviceId: String,
        options: AdvertisingOptions,
    ): Completable
    fun stopAdvertising()

    fun acceptConnection(endpointId: String): Completable
    fun rejectConnection(endpointId: String): Completable

    fun disconnectAll()

}