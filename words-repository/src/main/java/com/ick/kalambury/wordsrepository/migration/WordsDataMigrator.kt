package com.ick.kalambury.wordsrepository.migration

import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import com.ick.kalambury.wordsrepository.BuildConfig
import com.ick.kalambury.wordsrepository.properties.WordsPropertiesStorage
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

internal class WordsDataMigrator(
    private val dataSource: CompositeDataSource,
    private val wordsPropertiesStorage: WordsPropertiesStorage,
) {

    private var version: Int = 0

    fun performMigration(migrations: List<DataMigration>): Completable {
        return Completable.fromAction { prepareDirectory() }
            .andThen(wordsPropertiesStorage.wordsRepositoryVersion)
            .firstOrError()
            .doOnSuccess { version = it; logVersionInfo() }
            .flatMapObservable { Observable.fromIterable(migrations) }
            .filter { it.shouldMigrate(version, wordsPropertiesStorage).blockingGet() }
            .doOnNext { Log.d(logTag(), "Executing: ${it.javaClass.simpleName}")}
            .concatMapCompletable { it.migrate(dataSource) }
            .doOnComplete { Log.d(logTag(), "Words repository migration complete.") }
            .doOnError { handleError(it) }
            .onErrorComplete()
            .doFinally { wordsPropertiesStorage.setWordsRepositoryVersion(BuildConfig.WORDS_REPOSITORY_VERSION) }
    }

    private fun prepareDirectory() {
        val root = dataSource.rootDirectory.invoke()
        if (!root.exists()) {
            root.mkdirs()
        }
    }

    private fun logVersionInfo() {
        if (version == BuildConfig.WORDS_REPOSITORY_VERSION) {
            Log.d(logTag(), "Words repository version: $version - nothing to migrate.")
        } else {
            Log.d(logTag(), "Starting words repository migration from " +
                    "version $version to ${BuildConfig.WORDS_REPOSITORY_VERSION}.")
        }
    }

    private fun handleError(t: Throwable) {
        Log.w(logTag(), "Error during words repository migration. Clearing all local files...", t)

        dataSource.rootDirectory.invoke().listFiles()?.forEach {
            it.deleteRecursively()
        }
    }

}