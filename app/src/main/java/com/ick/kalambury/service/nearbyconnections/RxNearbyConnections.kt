package com.ick.kalambury.service.nearbyconnections

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.service.MessageEvent
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

class RxNearbyConnections(context: Context) : RxClientNearbyConnections, RxHostNearbyConnections {

    private val client: ConnectionsClient by lazy { Nearby.getConnectionsClient(context) }

    private val connectionEventProcessor: PublishProcessor<NearbyConnectionsEvent> by lazy { PublishProcessor.create() }
    private val messageEventProcessor: PublishProcessor<MessageEvent> by lazy { PublishProcessor.create() }

    override fun connectionEvents(): Flowable<NearbyConnectionsEvent> {
        return connectionEventProcessor.onBackpressureBuffer()
    }

    override fun messageEvent(): Flowable<MessageEvent> {
        return messageEventProcessor.onBackpressureBuffer()
    }

    override fun startAdvertising(
        endpointData: ByteArray,
        serviceId: String,
        options: AdvertisingOptions,
    ): Completable {
        return Completable.create { emitter ->
            client.startAdvertising(endpointData, serviceId, connectionLifecycleCallback, options)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener(emitter::onError)
        }
    }

    override fun stopAdvertising() = client.stopAdvertising()

    override fun startDiscovery(serviceId: String, options: DiscoveryOptions): Completable {
        return Completable.create { emitter ->
            client.startDiscovery(serviceId, endpointDiscoveryCallback, options)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener(emitter::onError)
        }
    }

    override fun stopDiscovery() = client.stopDiscovery()

    override fun connect(endpointData: ByteArray, endpointId: String): Completable {
        return Completable.create { emitter ->
            client.requestConnection(endpointData, endpointId, connectionLifecycleCallback)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener(emitter::onError)
        }
    }

    override fun acceptConnection(endpointId: String): Completable {
        return Completable.create { emitter ->
            client.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener(emitter::onError)
        }
    }

    override fun rejectConnection(endpointId: String): Completable {
        return Completable.create { emitter ->
            client.rejectConnection(endpointId)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener(emitter::onError)
        }
    }

    override fun send(endpointId: String, message: ByteArray): Completable {
        return broadcast(listOf(endpointId), message)
    }

    override fun broadcast(endpointIds: List<String>, message: ByteArray): Completable {
        return Completable.create { emitter ->
            client.sendPayload(endpointIds, Payload.fromBytes(message))
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener(emitter::onError)
        }
    }

    override fun disconnect(endpointId: String) {
        clear()
        client.disconnectFromEndpoint(endpointId)
    }

    override fun disconnectAll() {
        clear()
        client.stopAllEndpoints()
    }

    private fun clear() {
        connectionEventProcessor.onComplete()
        messageEventProcessor.onComplete()
    }

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
                connectionEventProcessor.onNext(
                    NearbyConnectionsEvent.EndpointFound(endpointId,
                    endpointInfo))
            }

            override fun onEndpointLost(endpointId: String) {
                connectionEventProcessor.onNext(NearbyConnectionsEvent.EndpointLost(endpointId))
            }
        }

    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
                connectionEventProcessor.onNext(NearbyConnectionsEvent.ConnectionInitiated(endpointId, info))
            }

            override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
                connectionEventProcessor.onNext(
                    NearbyConnectionsEvent.ConnectionResult(endpointId,
                    resolution))
            }

            override fun onDisconnected(endpointId: String) {
                connectionEventProcessor.onNext(NearbyConnectionsEvent.Disconnected(endpointId))
            }
        }

    private val payloadCallback: PayloadCallback =
        object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                val bytes = payload.asBytes()
                if (bytes != null) {
                    messageEventProcessor.onNext(MessageEvent(endpointId, bytes))
                } else {
                    Log.w(logTag(), "Received payload of unsupported type, (other than byte array)!")
                }
            }

            override fun onPayloadTransferUpdate(
                endpointId: String,
                transferUpdate: PayloadTransferUpdate,
            ) {
                if (BuildConfig.DEBUG) {
                    connectionEventProcessor.onNext(
                        NearbyConnectionsEvent.TransferUpdate(endpointId,
                        transferUpdate))
                }
            }
        }

}