package com.ick.kalambury.service

import com.google.android.gms.nearby.connection.ConnectionsStatusCodes.*
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.Strategy
import com.google.protobuf.InvalidProtocolBufferException
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.entities.connectionData
import com.ick.kalambury.list.model.EndpointData
import com.ick.kalambury.net.connection.User
import com.ick.kalambury.net.connection.model.GameData
import com.ick.kalambury.service.nearbyconnections.NearbyConnectionsEvent
import com.ick.kalambury.service.nearbyconnections.RxClientNearbyConnections
import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.concurrent.TimeUnit
import com.ick.kalambury.entities.EndpointData as EndpointDataProto

class LocalGameClientHandler(
    conn: RxClientNearbyConnections,
    private val preferenceStorage: MainPreferenceStorage,
) : ClientGameHandler<RxClientNearbyConnections>(conn) {

    override lateinit var localUser: User
    override lateinit var hostEndpoint: Endpoint

    private var discoveryTimerDisposable: Disposable? = null

    private val discoveredEndpoints: MutableMap<String, EndpointData> = mutableMapOf()
    private var isDiscovering = false

    init {
        disposables += connection.connectionEvents()
            .observeOn(handlerThreadScheduler)
            .subscribe(::handleNearbyConnectionsEvents)
    }

    override fun ready(): Completable = sendCompletable(GameData.action(GameData.PLAYER_READY))

    override fun startDiscovery(duration: Long) {
        if (state >= GameHandler.State.CONNECTED) {
            Log.w(logTag, "startDiscovery() - Already in $state state. Ignoring...")
            return
        }
        if (isDiscovering) {
            Log.w(logTag, "startDiscovery() - Already discovering. Ignoring...")
            return
        }

        Log.d(logTag, "startDiscovery()")

        disposables += connection.startDiscovery(
            BuildConfig.LOCAL_GAME_SERVICE_ID,
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        )
            .subscribe(
                {
                    isDiscovering = true
                    discoveryTimerDisposable = Completable.timer(duration, TimeUnit.MILLISECONDS)
                        .subscribe {
                            isDiscovering = false
                            connection.stopDiscovery()

                            if (state >= GameHandler.State.DISCONNECTING) {
                                Log.w(
                                    logTag,
                                    "discoveryTimer.onComplete() - Already in $state state. Ignoring..."
                                )
                            } else {
                                Log.d(logTag, "Finished discovering endpoints.")
                                sendEndpointsToUi(GameEvent.State.DISCOVERY_FINISHED)
                            }
                        }
                    Log.d(logTag, "Started discovering endpoints...")
                },
                {
                    Log.w(logTag, "Endpoints discovery failed.", it)
                    sendToUI(GameEvent.State.DISCOVERY_FAILURE)
                }
            )
    }

    override fun stopDiscovery() {
        Log.d(logTag, "stopDiscovery()")

        connection.stopDiscovery()
        discoveryTimerDisposable?.dispose()
        isDiscovering = false
    }

    override fun connect(endpoint: Endpoint): Completable {
        if (state >= GameHandler.State.CONNECTING) {
            Log.w(logTag, "connect() - Already connecting or connected! Ignoring...")
            return Completable.complete()
        }

        Log.d(logTag, "connect()")

        hostEndpoint = endpoint

        if (isDiscovering) {
            stopDiscovery()
        }

        state = GameHandler.State.CONNECTING
        notifyUI()

        return preferenceStorage.localUserData
            .firstOrError()
            .doOnSuccess { localUser = it }
            .map {
                connectionData {
                    this.endpoint = endpoint.id
                    nickname = it.nickname
                    uuid = it.uuid
                    version = BuildConfig.VERSION_CODE
                }.toByteArray()
            }
            .observeOn(handlerThreadScheduler)
            .flatMapCompletable { connection.connect(it, endpoint.id) }
    }

    override fun finish() {
        if (state >= GameHandler.State.DISCONNECTED) {
            Log.w(logTag, "finish() - Already disconnected! Ignoring...")
            return
        }

        Log.d(logTag, "finish()")

        disposables.dispose()
        discoveryTimerDisposable?.dispose()

        cancelGameTimer()
        connection.stopDiscovery()

        if (state == GameHandler.State.CONNECTED) {
            state = GameHandler.State.DISCONNECTING

            sendCompletable(GameData.Builder(GameData.QUIT_GAME).build())
                .onErrorComplete()
                .andThen {
                    connection.disconnect(hostEndpoint.id)
                    state = GameHandler.State.DISCONNECTED
                }
                .observeOn(handlerThreadScheduler)
                .subscribe(
                    { handlerThreadScheduler.shutdown() },
                    { Log.w(logTag, "Failed finishing game handler.", it) }
                )
        } else {
            handlerThreadScheduler.shutdown()
            state = GameHandler.State.DISCONNECTED
        }
    }

    private fun handleNearbyConnectionsEvents(event: NearbyConnectionsEvent) {
        when (event) {
            is NearbyConnectionsEvent.EndpointFound -> handleEndpointFoundEvent(event)
            is NearbyConnectionsEvent.EndpointLost -> handleEndpointLostEvent(event)
            is NearbyConnectionsEvent.ConnectionInitiated -> handleConnectionInitiatedEvent(event)
            is NearbyConnectionsEvent.ConnectionResult -> handleConnectionResultEvent(event)
            is NearbyConnectionsEvent.TransferUpdate -> handleMessageTransferUpdateEvent(event)
            is NearbyConnectionsEvent.Disconnected -> handleDisconnectedEvent(event)
        }
    }

    private fun handleEndpointFoundEvent(event: NearbyConnectionsEvent.EndpointFound) {
        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag,
                "handleEndpointFoundEvent() - Already in $state state. Ignoring..."
            )
            return
        }

        Log.d(logTag, "handleEndpointFoundEvent()")

        val (endpointId, endpointInfo) = event

        if (endpointInfo.serviceId == BuildConfig.LOCAL_GAME_SERVICE_ID) {
            try {
                discoveredEndpoints[endpointId] = EndpointData(EndpointDataProto.parseFrom(endpointInfo.endpointInfo)
                    .toBuilder()
                    .setId(endpointId)
                    .build()
                )
                sendEndpointsToUi()
            } catch (e: InvalidProtocolBufferException) {
                Log.w(logTag, "handleEndpointFoundEvent() - Endpoint with incorrect data. Ignoring...")
                sendToUI(GameEvent.State.UNSUPPORTED_VERSION)
            }
        } else {
            Log.w(
                logTag,
                "handleEndpointFoundEvent() - Endpoint with unsupported serviceId ${endpointInfo.serviceId}. Ignoring..."
            )
        }
    }

    private fun handleEndpointLostEvent(event: NearbyConnectionsEvent.EndpointLost) {
        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag,
                "handleEndpointLostEvent() - Already in $state state. Ignoring..."
            )
            return
        }

        Log.d(logTag, "handleEndpointLostEvent()")

        discoveredEndpoints.remove(event.endpointId)
        sendEndpointsToUi()
    }

    private fun handleConnectionInitiatedEvent(event: NearbyConnectionsEvent.ConnectionInitiated) {
        val (endpointId, connectionInfo) = event

        if (connectionInfo.isIncomingConnection) {
            Log.w(
                logTag,
                "handleConnectionInitiatedEvent() - Incoming request when in client mode. Rejecting..."
            )
            connection.rejectConnection(endpointId).subscribe()
            return
        }

        Log.d(logTag, "handleConnectionInitiatedEvent()")

        if (endpointId == hostEndpoint.id) {
            connection.acceptConnection(endpointId).subscribe()
        } else {
            Log.w(
                logTag,
                "handleConnectionInitiatedEvent() - Connection with unknown host. Rejecting..."
            )
            connection.rejectConnection(endpointId).subscribe()
        }
    }

    private fun handleConnectionResultEvent(event: NearbyConnectionsEvent.ConnectionResult) {
        val result = event.resolution

        Log.d(logTag, "handleConnectionResultEvent() result: + ${result.status}")

        when (result.status.statusCode) {
            STATUS_OK,
            STATUS_ALREADY_CONNECTED_TO_ENDPOINT -> { /* no-op */ }
            STATUS_CONNECTION_REJECTED -> sendToUI(GameEvent.State.UNSUPPORTED_VERSION)
            else -> sendToUI(GameEvent.State.NETWORK_FAILURE)
        }
    }

    private fun handleMessageTransferUpdateEvent(event: NearbyConnectionsEvent.TransferUpdate) {
        val (endpointId, update) = event

        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag,
                "handleMessageTransferUpdateEvent() - Already in $state state. Ignoring..."
            )
            return
        }

        if (endpointId == hostEndpoint.id) {
            Log.v(
                logTag, "handleMessageTransferUpdateEvent() from user: " +
                        "${hostEndpoint.id}, msgId: ${update.payloadId}, " +
                        "status: ${update.status}, bytes: ${update.bytesTransferred}"
            )
        } else {
            Log.w(
                logTag, "handleMessageTransferUpdateEvent() from unknown endpoint: " +
                        "$endpointId, msgId: ${update.payloadId}, " +
                        "status: ${update.status}, bytes: ${update.bytesTransferred}"
            )
        }
    }

    private fun handleDisconnectedEvent(event: NearbyConnectionsEvent.Disconnected) {
        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag,
                "handleDisconnectedEvent() - Already in $state state. Ignoring..."
            )
            return
        }

        Log.d(logTag, "handleDisconnectedEvent()")

        if (event.endpointId == hostEndpoint.id) {
            sendToUI(GameEvent.State.NETWORK_FAILURE)
        } else {
            Log.w(logTag, "handleDisconnectedEvent() callback with unknown host!")
        }
    }

    private fun sendEndpointsToUi(state: GameEvent.State = GameEvent.State.DISCOVERING) {
        sendToUI(GameEvent(state, hostEndpoints = discoveredEndpoints.values.toList()))
    }

}