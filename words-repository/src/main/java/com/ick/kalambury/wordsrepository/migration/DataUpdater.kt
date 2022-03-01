package com.ick.kalambury.wordsrepository.migration

import com.ick.kalambury.wordsrepository.datasource.WordsManifestDataSource
import com.ick.kalambury.wordsrepository.datasource.WordsSetDataSource
import com.ick.kalambury.wordsrepository.model.WordsManifest
import com.ick.kalambury.wordsrepository.model.WordsSet
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.SingleSubject
import java.io.File
import java.io.InputStream

class DataUpdater internal constructor(
    rootDirectory: () -> File,
    private val manifestDataSourceDelegate: WordsManifestDataSource,
    private val wordsSetDataSourceDelegate: WordsSetDataSource,
    private val migrationCompleteSignal: SingleSubject<Unit>,
    private val updateSignal: PublishSubject<Unit>,
) : CompositeDataSource(rootDirectory, manifestDataSourceDelegate, wordsSetDataSourceDelegate) {

    private var dirty: Boolean = false

    override fun getLocalWordsManifest(): Single<WordsManifest> {
        return migrationCompleteSignal.flatMap {
            manifestDataSourceDelegate.getLocalWordsManifest()
        }
    }

    override fun saveWordsManifest(wordsManifest: WordsManifest): Completable {
        dirty = true
        return migrationCompleteSignal.flatMapCompletable {
            manifestDataSourceDelegate.saveWordsManifest(wordsManifest)
        }
    }

    override fun getLocalWordsSet(setId: String): Single<WordsSet> {
        return migrationCompleteSignal.flatMap {
            wordsSetDataSourceDelegate.getLocalWordsSet(setId)
        }
    }

    override fun saveWordsSet(setId: String, input: InputStream, fileName: String): Completable {
        return migrationCompleteSignal.flatMapCompletable {
            wordsSetDataSourceDelegate.saveWordsSet(setId, input, fileName)
        }
    }

    fun finishUpdate() {
        if (dirty) {
            updateSignal.onNext(Unit)
            dirty = false
        }
    }

}