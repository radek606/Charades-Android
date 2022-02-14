package com.ick.kalambury.net.connection

data class User(
    val id: String,
    val uuid: String,
    val nickname: String,
    var connectionState: ConnectionState = ConnectionState.CONNECTING
)
