package com.ick.kalambury.words

import com.ick.kalambury.model.TestData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class MockWordsManifestDataSource : WordsManifestDataSource {

    override fun getWordsManifest(): Single<WordsManifest> {
        return Single.just(TestData.manifest)
    }

    override fun saveWordsManifest(wordsManifest: WordsManifest): Completable {
        return Completable.complete()
    }

}