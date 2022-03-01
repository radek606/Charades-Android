package com.ick.kalambury.wordsrepository.datasource

import com.ick.kalambury.wordsrepository.BuildConfig
import com.ick.kalambury.wordsrepository.model.WordsSet
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.InputStream

interface WordsSetDataSource {

    fun getAssetsWordsSet(setId: String): Single<WordsSet>
    fun getLocalWordsSet(setId: String): Single<WordsSet>

    fun getLocalWordsSetDirectory(setId: String): File

    fun saveWordsSet(
        setId: String,
        input: InputStream,
        fileName: String = BuildConfig.WORDS_SET_FILE_NAME
    ): Completable

}