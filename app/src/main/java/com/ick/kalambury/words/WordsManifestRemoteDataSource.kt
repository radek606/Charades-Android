package com.ick.kalambury.words

import com.ick.kalambury.remoteconfig.RemoteConfigHelper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class WordsManifestRemoteDataSource : WordsManifestDataSource {

    override fun getWordsManifest(): Single<WordsManifest> {
        return Single.fromCallable { WordsManifest.fromString(RemoteConfigHelper.remoteWordsManifest) }
    }

    override fun saveWordsManifest(wordsManifest: WordsManifest): Completable =
        Completable.complete()

}