package com.ick.kalambury.util.crypto

import com.ick.kalambury.util.ByteArrayBase64Serializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("ArrayInDataClass")
@Serializable
data class Secret(
    @Serializable(with = ByteArrayBase64Serializer::class)
    val key: ByteArray,
    @Serializable(with = ByteArrayBase64Serializer::class)
    val iv: ByteArray,
) {

    fun serialize() = Json.encodeToString(this)

    companion object {

        fun fromJson(jsonString: String): Secret = Json.decodeFromString(jsonString)

    }

}
