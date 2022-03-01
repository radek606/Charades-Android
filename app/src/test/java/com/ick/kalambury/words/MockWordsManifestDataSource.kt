package com.ick.kalambury.words

import com.ick.kalambury.wordsrepository.datasource.WordsManifestDataSource
import com.ick.kalambury.wordsrepository.model.WordsManifest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.FileNotFoundException

class MockWordsManifestDataSource(
    private val manifest: WordsManifest,
) : WordsManifestDataSource {

    override fun getAssetsWordsManifest(): Single<WordsManifest> {
        return Single.just(manifest)
    }

    override fun getLocalWordsManifest(): Single<WordsManifest> {
        return Single.error(FileNotFoundException())
    }

    override fun saveWordsManifest(wordsManifest: WordsManifest): Completable {
        return Completable.complete()
    }

}