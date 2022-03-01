package com.ick.kalambury.wordsrepository.datasource

import com.ick.kalambury.wordsrepository.model.TestData
import com.ick.kalambury.wordsrepository.model.WordsSet
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.InputStream

class MockWordsSetDataSource : WordsSetDataSource {

    override fun getAssetsWordsSet(setId: String): Single<WordsSet> {
        return Single.just(TestData.wordsSetsMap[setId]!!)
    }

    override fun getLocalWordsSet(setId: String): Single<WordsSet> {
        return Single.just(TestData.wordsSetsMap[setId]!!)
    }

    override fun getLocalWordsSetDirectory(setId: String): File {
        TODO("Not yet implemented")
    }

    override fun saveWordsSet(setId: String, input: InputStream, fileName: String): Completable {
        return Completable.complete()
    }

}