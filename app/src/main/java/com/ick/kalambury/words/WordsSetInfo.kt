package com.ick.kalambury.words

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.ick.kalambury.GameMode
import java.util.*
import java.util.concurrent.TimeUnit

@Keep
data class WordsSetInfo constructor(
    val id: String,
    val version: Int,
    val name: String,
    val description: String,
    val language: Language = Language.EN,
    val createdTimestamp: Date,
    val updatedTimestamp: Date,
    private val usage: Map<String, Options>
) {

    constructor(set: WordsManifest.Set) : this(
        set.id,
        set.version,
        set.name,
        set.description,
        Language.forLanguageName(set.language),
        set.createdTimestamp,
        set.updatedTimestamp,
        set.usage.map { it.key to Options(it.value.isDefault) }.toMap()
    )

    val isNew: Boolean
        get() = version == INITIAL_SET_VERSION && isBefore(createdTimestamp)

    val isUpdated: Boolean
        get() = version > INITIAL_SET_VERSION && isBefore(updatedTimestamp)

    private fun isBefore(date: Date): Boolean {
        return Date(System.currentTimeMillis() - MARK_AS_NEW_OR_UPDATED_PERIOD).before(date)
    }

    fun isEligible(mode: GameMode) = usage[mode.wordsSetType] != null

    fun isDefault(mode: GameMode) = usage.getOrDefault(mode.wordsSetType, Options(false)).isDefault

    companion object {
        private const val INITIAL_SET_VERSION = 1
        private val MARK_AS_NEW_OR_UPDATED_PERIOD = TimeUnit.DAYS.toMillis(3)
    }

    class Options(val isDefault: Boolean)

}
