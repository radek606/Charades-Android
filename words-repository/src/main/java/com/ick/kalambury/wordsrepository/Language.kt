package com.ick.kalambury.wordsrepository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
enum class Language(private val language: String) {

    @SerialName("pl")
    PL("pl"),
    @SerialName("en")
    EN("en");

    companion object {
        private val languageMap: MutableMap<String, Language> = HashMap()

        init {
            languageMap["pl"] = PL
            languageMap["en"] = EN
        }

        fun forLanguageName(value: String): Language {
            return languageMap.getOrDefault(value, EN)
        }

    }

    override fun toString() = language

    fun toLocale() = Locale(language)

}