package com.ick.kalambury.words

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface WordsInstanceDataSource {

    fun getWordsInstance(instance: String): Single<WordsInstance>
    fun saveWordsInstance(instance: WordsInstance): Completable

}