package com.ick.kalambury.service.nearbyconnections

import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.ick.kalambury.service.RxClientConnection
import io.reactivex.rxjava3.core.Completable

interface RxClientNearbyConnections : RxClientConnection<NearbyConnectionsEvent> {

    fun startDiscovery(serviceId: String, options: DiscoveryOptions): Completable
    fun stopDiscovery()

    fun connect(endpointData: ByteArray, endpointId: String): Completable
    fun disconnect(endpointId: String)

    fun acceptConnection(endpointId: String): Completable
    fun rejectConnection(endpointId: String): Completable

}