package com.ick.kalambury.wordsrepository.datasource

import com.ick.kalambury.wordsrepository.model.WordsInstance
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class MockWordsInstanceDataSource : WordsInstanceDataSource {

    private val instancesMap: MutableMap<String, WordsInstance> = mutableMapOf()

    override fun getWordsInstance(instance: String): Single<WordsInstance> {
        return Single.just(instancesMap[instance] ?: throw NullPointerException())
    }

    override fun saveWordsInstance(instance: WordsInstance): Completable {
        return Completable.fromAction { instancesMap[instance.id] = instance }
    }

}