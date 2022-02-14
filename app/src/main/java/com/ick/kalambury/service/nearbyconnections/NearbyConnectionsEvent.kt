package com.ick.kalambury.service.nearbyconnections

import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.ick.kalambury.service.ConnectionEvent

sealed class NearbyConnectionsEvent : ConnectionEvent() {

    data class EndpointFound(val endpointId: String, val endpointInfo: DiscoveredEndpointInfo) :
        NearbyConnectionsEvent()

    data class EndpointLost(val endpointId: String) : NearbyConnectionsEvent()
    data class ConnectionInitiated(val endpointId: String, val info: ConnectionInfo) :
        NearbyConnectionsEvent()

    data class ConnectionResult(val endpointId: String, val resolution: ConnectionResolution) :
        NearbyConnectionsEvent()

    data class Disconnected(val endpointId: String) : NearbyConnectionsEvent()
    data class TransferUpdate(
        val endpointId: String,
        val transferUpdate: PayloadTransferUpdate,
    ) : NearbyConnectionsEvent()

}