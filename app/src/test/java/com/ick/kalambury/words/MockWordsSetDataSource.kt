package com.ick.kalambury.words

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.InputStream
import com.ick.kalambury.model.TestData
import java.io.File

class MockWordsSetDataSource : WordsSetDataSource {

    override fun getWordsSet(setId: String): Single<WordsSet> {
        return Single.just(TestData.wordsSetsMap[setId]!!)
    }

    override fun getWordsSetDirectory(setId: String, createIfNecessary: Boolean): File {
        TODO("Not yet implemented")
    }

    override fun saveWordsSet(setId: String, input: InputStream, fileName: String): Completable {
        return Completable.complete()
    }

}