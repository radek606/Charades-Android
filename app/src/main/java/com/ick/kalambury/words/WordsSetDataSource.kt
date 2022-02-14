package com.ick.kalambury.words

import com.ick.kalambury.BuildConfig
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.InputStream

interface WordsSetDataSource {

    fun getWordsSet(setId: String): Single<WordsSet>
    fun getWordsSetDirectory(setId: String, createIfNecessary: Boolean = true): File
    fun saveWordsSet(
        setId: String,
        input: InputStream,
        fileName: String = BuildConfig.LOCAL_WORDS_SET_FILE_NAME
    ): Completable

}