package com.ick.kalambury.words

import androidx.annotation.Keep
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.readValue
import com.ick.kalambury.util.ByteArrayDeserializer
import com.ick.kalambury.util.ByteArraySerializer
import com.ick.kalambury.util.JsonUtils

@Keep
@Suppress("ArrayInDataClass")
data class Secret(
    @JsonSerialize(using = ByteArraySerializer::class)
    @JsonDeserialize(using = ByteArrayDeserializer::class)
    val key: ByteArray,

    @JsonSerialize(using = ByteArraySerializer::class)
    @JsonDeserialize(using = ByteArrayDeserializer::class)
    val iv: ByteArray,
) {

    fun serialize(): String = JsonUtils.toJson(this)

    companion object {

        fun fromString(jsonString: String) = JsonUtils.objectMapper.readValue<Secret>(jsonString)

    }

}
