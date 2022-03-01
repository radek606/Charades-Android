package com.ick.kalambury.wordsrepository.datasource

import com.ick.kalambury.wordsrepository.model.WordsManifest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface WordsManifestDataSource {

    fun getAssetsWordsManifest(): Single<WordsManifest>
    fun getLocalWordsManifest(): Single<WordsManifest>

    fun saveWordsManifest(wordsManifest: WordsManifest): Completable

}