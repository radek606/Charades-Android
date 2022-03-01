package com.ick.kalambury.wordsrepository.datasource

import com.ick.kalambury.wordsrepository.model.WordsInstance
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface WordsInstanceDataSource {

    fun getWordsInstance(instance: String): Single<WordsInstance>
    fun saveWordsInstance(instance: WordsInstance): Completable

}