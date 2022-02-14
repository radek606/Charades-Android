package com.ick.kalambury.words

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnore
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlin.random.Random

@Keep
data class WordsInstance(
    val id: String,
    val wordsSets: MutableList<WordsSet> = mutableListOf(),
    var selectedSets: List<String> = mutableListOf(),
) {

    @JsonIgnore
    private var _wordsObservable: Observable<Word> = createWordsObservable()

    @get:JsonIgnore
    val wordsObservable = _wordsObservable

    @get:JsonIgnore
    val hasWords: Boolean
        get() = wordsSets.isNotEmpty()

    fun reset() {
        _wordsObservable = createWordsObservable()
    }

    fun getNextWord(): Single<Word> = Single.create {
        val word = getWord()
        if (word != null) {
            it.onSuccess(word)
        } else {
            it.onError(EmptyInstanceException())
        }
    }

    private fun createWordsObservable(): Observable<Word> {
        return Observable.create {
            val word = getWord()
            if (word != null) {
                it.onNext(word)
            } else {
                it.onError(EmptyInstanceException())
            }
        }
    }

    private fun getWord(): Word? {
        if (wordsSets.isNotEmpty()) {
            val index = Random.nextInt(wordsSets.size)

            val set = wordsSets[index]
            val word = set.getRandomWord(Random)

            if (!set.hasWords) {
                wordsSets.removeAt(index)
            }

            return word
        }
        return null
    }

}

class EmptyInstanceException : Exception()
