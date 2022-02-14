package com.ick.kalambury.words

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface WordsManifestDataSource {

    fun getWordsManifest(): Single<WordsManifest>
    fun saveWordsManifest(wordsManifest: WordsManifest): Completable

}