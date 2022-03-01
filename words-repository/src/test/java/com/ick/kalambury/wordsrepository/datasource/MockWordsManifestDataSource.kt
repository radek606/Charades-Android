package com.ick.kalambury.wordsrepository.datasource

import com.ick.kalambury.wordsrepository.model.TestData
import com.ick.kalambury.wordsrepository.model.WordsManifest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class MockWordsManifestDataSource(
    private val manifest: WordsManifest = TestData.manifest,
) : WordsManifestDataSource {

    override fun getAssetsWordsManifest(): Single<WordsManifest> {
        return Single.just(manifest)
    }

    override fun getLocalWordsManifest(): Single<WordsManifest> {
        return Single.just(manifest)
    }

    override fun saveWordsManifest(wordsManifest: WordsManifest): Completable {
        return Completable.complete()
    }

}