package com.ick.kalambury.words

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnore
import kotlin.random.Random

@Keep
data class WordsSet(val id: String, val name: String, val words: MutableList<Word> = mutableListOf()) {

    @get:JsonIgnore
    val hasWords: Boolean
        get() = words.isNotEmpty()

    fun getRandomWord(random: Random) =
        words.removeAt(random.nextInt(words.size)).apply { setName = name }

}
