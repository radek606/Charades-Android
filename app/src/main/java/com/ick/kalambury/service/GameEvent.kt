package com.ick.kalambury.service

import com.ick.kalambury.GameConfig
import com.ick.kalambury.list.model.EndpointData
import com.ick.kalambury.list.model.Player
import com.ick.kalambury.net.connection.SupportedVersionInfo
import com.ick.kalambury.net.connection.model.GameData

data class GameEvent(
    val state: State,
    val gameData: GameData? = null,
    val config: GameConfig? = null,
    val players: Map<String, Player> = mapOf(),
    val hostEndpoints: List<EndpointData> = listOf(),
    val supportedVersionInfo: SupportedVersionInfo? = null,
) {

    enum class State {
        // Normal states
        INITIALIZING,
        INITIALIZED,
        DISCOVERING,
        DISCOVERY_FINISHED,
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        HOST_FINISHED,

        // Error states
        ADVERTISING_FAILURE,
        DISCOVERY_FAILURE,
        UNSUPPORTED_VERSION,
        KICKED,
        PLAYER_LIMIT_EXCEEDED,
        TABLE_NOT_FOUND,
        NETWORK_FAILURE,
        GENERIC_FAILURE,
    }

}