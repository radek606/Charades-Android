package com.ick.kalambury.wordsrepository.model

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class WordsSet(val id: String, val name: String, val words: MutableList<Word>) {

    val hasWords: Boolean
        get() = words.isNotEmpty()

    fun getRandomWord(random: Random) =
        words.removeAt(random.nextInt(words.size)).apply { setName = name }

}
