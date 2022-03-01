package com.ick.kalambury.wordsrepository.model

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.ick.kalambury.util.ByteArrayDeserializer
import com.ick.kalambury.util.JsonUtils
import com.ick.kalambury.wordsrepository.Language
import com.ick.kalambury.wordsrepository.Usage
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
@Keep
class WordsManifest(
    @JsonProperty
    val version: Int = 0,

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
              val language: Language,
              val createdTimestamp: Date = Date(0),
              val updatedTimestamp: Date = Date(0),
              val usage: Map<Usage, Options>,
    ) {

        @get:JsonIgnore
        val isNew: Boolean
            get() = version == INITIAL_SET_VERSION && isBefore(createdTimestamp)

        @get:JsonIgnore
        val isUpdated: Boolean
            get() = version > INITIAL_SET_VERSION && isBefore(updatedTimestamp)

        @JsonIgnore
        private fun isBefore(date: Date): Boolean {
            return Date(System.currentTimeMillis() - MARK_AS_NEW_OR_UPDATED_PERIOD).before(date)
        }

        @JsonIgnore
        fun isEligible(type: Usage) = usage[type] != null

        @JsonIgnore
        fun isDefault(type: Usage) = usage.getOrDefault(type, Options(false)).isDefault

        companion object {
            //constant indicating first possible version value of word set coming from remote service
            private const val INITIAL_SET_VERSION = 1

            @VisibleForTesting
            val MARK_AS_NEW_OR_UPDATED_PERIOD = TimeUnit.DAYS.toMillis(3)
        }

        @Keep
        class Options(@JsonProperty(value = "default")
                      val isDefault: Boolean = false)
    }

}
