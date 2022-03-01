package com.ick.kalambury.wordsrepository

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.util.*

enum class Language(private val language: String) {

    PL("pl"),
    EN("en");

    companion object {
        private val languageMap: MutableMap<String, Language> = HashMap()

        init {
            languageMap["pl"] = PL
            languageMap["en"] = EN
        }

        @JsonCreator
        fun forLanguageName(value: String): Language {
            return languageMap.getOrDefault(value, EN)
        }

    }

    @JsonValue
    override fun toString() = language

    fun toLocale() = Locale(language)

}