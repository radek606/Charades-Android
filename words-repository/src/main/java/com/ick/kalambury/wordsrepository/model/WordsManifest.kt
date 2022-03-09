package com.ick.kalambury.wordsrepository.model

import androidx.annotation.VisibleForTesting
import com.ick.kalambury.util.ByteArrayBase64Serializer
import com.ick.kalambury.util.DateAsLongSerializer
import com.ick.kalambury.wordsrepository.Language
import com.ick.kalambury.wordsrepository.Usage
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Class representing internal model of words manifest, consisting of list of words set info.
 * Default manifest is bundled with app in assets and can be updated from remote service.
 * Updated manifest is stored as file in local storage.
 *
 * Note: default manifest and sets versions should be always 0,
 * so they can't be marked as 'new' or 'updated'. It exist only to have something to start
 * and not rely only on remote service. Bundled assets won't be updated in next app versions.
 */
@Serializable
class WordsManifest(
    @EncodeDefault val version: Int = 0,
    @Serializable(with = ByteArrayBase64Serializer::class)
    val key: ByteArray = ByteArray(0),
    @Serializable(with = ByteArrayBase64Serializer::class)
    val iv: ByteArray = ByteArray(0),
    val sets: List<Set> = listOf()
) {

    @Serializable
    class Set(val id: String,
              @EncodeDefault val version: Int = 0,
              val name: String,
              val description: String,
              val language: Language,
              @Serializable(with = DateAsLongSerializer::class)
              val createdTimestamp: Date,
              @Serializable(with = DateAsLongSerializer::class)
              val updatedTimestamp: Date,
              val usage: Map<Usage, Options>,
    ) {

        val isNew: Boolean
            get() = version == INITIAL_SET_VERSION && isBefore(createdTimestamp)

        val isUpdated: Boolean
            get() = version > INITIAL_SET_VERSION && isBefore(updatedTimestamp)

        private fun isBefore(date: Date): Boolean {
            return Date(System.currentTimeMillis() - MARK_AS_NEW_OR_UPDATED_PERIOD).before(date)
        }

        fun isEligible(type: Usage) = usage[type] != null

        fun isDefault(type: Usage) = usage.getOrDefault(type, Options(false)).isDefault

        companion object {
            //constant indicating first possible version value of word set coming from remote service
            private const val INITIAL_SET_VERSION = 1

            @VisibleForTesting
            val MARK_AS_NEW_OR_UPDATED_PERIOD = TimeUnit.DAYS.toMillis(3)
        }

        @Serializable
        class Options(@Required @SerialName(value = "default") val isDefault: Boolean = false)
    }

}

object WordsManifestSerializer : JsonTransformingSerializer<WordsManifest>(WordsManifest.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        return JsonObject(element.jsonObject.filterNot { (key, _) -> key == "key" || key == "iv" })
    }
}
