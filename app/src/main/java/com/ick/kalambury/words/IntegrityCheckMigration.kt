package com.ick.kalambury.words

import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import com.ick.kalambury.wordsrepository.BuildConfig
import com.ick.kalambury.wordsrepository.properties.WordsPropertiesStorage
import com.ick.kalambury.wordsrepository.migration.CompositeDataSource
import com.ick.kalambury.wordsrepository.migration.DataMigration
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import java.io.File

class IntegrityCheckMigration : DataMigration {

    override fun shouldMigrate(version: Int, properties: WordsPropertiesStorage): Single<Boolean> {
        return Single.fromCallable { version < INTEGRITY_CHECK_VERSION }
    }

    override fun migrate(dataSource: CompositeDataSource): Completable {
        return Single.fromCallable {
            //check words root directory for manifest file.
            val manifest = File(dataSource.rootDirectory.invoke(), BuildConfig.WORDS_MANIFEST_FILE_NAME)
            manifest.exists()
        }.flatMapMaybe {
            //if has manifest, load it, finish otherwise
            if (it) {
                Maybe.fromSingle(dataSource.getLocalWordsManifest())
            } else {
                Log.d(logTag, "Local manifest file not found. Nothing to check.")
                Maybe.empty()
            }
        }.flatMapSingle {
            //load one of words sets to check if we can read it without error
            Log.d(logTag, "Found local manifest with version: ${it.version}.")
            dataSource.getLocalWordsSet(it.sets.first().id)
        }.flatMapCompletable {
            //if succeeded - complete, otherwise exception will be thrown
            //that will be handled by migrator and old files will be deleted
            Log.d(logTag, "Successfully loaded one of words sets. Integrity checked.")
            Completable.complete()
        }
    }

    internal companion object {
        private const val INTEGRITY_CHECK_VERSION = 2
    }

}