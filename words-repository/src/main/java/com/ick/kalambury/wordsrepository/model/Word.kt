package com.ick.kalambury.wordsrepository.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable
data class Word internal constructor(
    @SerialName("text")
    @Serializable(with = WordDeserializer::class)
    val variants: List<String>,
    @Transient var setName: String? = null,
) {

    val wordString: String
        get() = variants[0]

}

object WordDeserializer : JsonTransformingSerializer<List<String>>(ListSerializer(String.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return if (element !is JsonArray) JsonArray(listOf(element)) else element
    }
}