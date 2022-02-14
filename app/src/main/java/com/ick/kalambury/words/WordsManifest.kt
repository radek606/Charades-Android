package com.ick.kalambury.words

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.ick.kalambury.util.ByteArrayDeserializer
import com.ick.kalambury.util.JsonUtils
import java.util.*

@Keep
class WordsManifest(
    @JsonProperty
    val version: Int,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonDeserialize(using = ByteArrayDeserializer::class)
    val key: ByteArray = ByteArray(0),

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonDeserialize(using = ByteArrayDeserializer::class)
    val iv: ByteArray = ByteArray(0),

    @JsonProperty
    val sets: List<Set> = listOf()
) {

    companion object {

        @JvmStatic
        fun fromString(jsonString: String): WordsManifest {
            return JsonUtils.fromJson(jsonString, WordsManifest::class.java)
        }

    }

    @Keep
    class Set(val id: String,
              val version: Int = 0,
              val name: String,
              val description: String,
              val language: String,
              val createdTimestamp: Date = Date(0),
              val updatedTimestamp: Date = Date(0),
              val usage: Map<String, Options>,
    ) {

        @Keep
        class Options(@JsonProperty(value = "default")
                      val isDefault: Boolean = false)
    }

}
