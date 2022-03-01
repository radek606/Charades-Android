package com.ick.kalambury.words

import com.ick.kalambury.wordsrepository.datasource.WordsSetDataSource
import com.ick.kalambury.wordsrepository.model.WordsSet
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.InputStream

class MockWordsSetDataSource : WordsSetDataSource {

    override fun getAssetsWordsSet(setId: String): Single<WordsSet> {
        return Single.never()
    }

    override fun getLocalWordsSet(setId: String): Single<WordsSet> {
        return Single.never()
    }

    override fun getLocalWordsSetDirectory(setId: String): File {
        TODO("Not yet implemented")
    }

    override fun saveWordsSet(setId: String, input: InputStream, fileName: String): Completable {
        return Completable.complete()
    }

}