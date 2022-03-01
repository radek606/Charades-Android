package com.ick.kalambury.wordsrepository

import android.content.Context
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import com.ick.kalambury.util.crypto.Secret
import com.ick.kalambury.util.crypto.SecretDataSource
import com.ick.kalambury.util.crypto.SecretProvider
import com.ick.kalambury.util.crypto.SecretProviderImpl
import com.ick.kalambury.util.settings.DataStoreWrapper
import com.ick.kalambury.wordsrepository.datasource.WordsInstanceLocalDataSource
import com.ick.kalambury.wordsrepository.datasource.WordsManifestLocalDataSource
import com.ick.kalambury.wordsrepository.datasource.WordsSetLocalDataSource
import com.ick.kalambury.wordsrepository.migration.DataMigration
import com.ick.kalambury.wordsrepository.properties.WordsPropertiesStorageImpl
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

class WordsRepositoryBuilder(
    private val context: Context,
    private var produceDirectory: (() -> File) = { context.filesDir },
) {

    private var secretDataSource: SecretDataSource? = null
    private var scheduler: Scheduler = Schedulers.io()

    private val migrations: MutableList<DataMigration> = mutableListOf()

    fun setScheduler(scheduler: Scheduler): WordsRepositoryBuilder {
        return apply { this.scheduler = scheduler }
    }

    fun setEncryptionSecretDataSource(source: SecretDataSource): WordsRepositoryBuilder {
        return apply { secretDataSource = source }
    }

    fun addMigration(dataMigration: DataMigration): WordsRepositoryBuilder {
        return apply { migrations.add(dataMigration) }
    }

    fun build(): WordsRepository {
        val dataStore = RxPreferenceDataStoreBuilder(context, "words_prefs").build()

        val secretProvider =
            secretDataSource?.let { SecretProviderImpl(it) } ?: NoopSecretProvider()

        return WordsRepositoryImpl(
            produceDirectory,
            manifestDataSource = WordsManifestLocalDataSource(context, produceDirectory),
            wordsSetDataSource = WordsSetLocalDataSource(context, secretProvider, produceDirectory),
            wordsInstanceDataSource = WordsInstanceLocalDataSource(context, secretProvider),
            wordsPropertiesStorage = WordsPropertiesStorageImpl(DataStoreWrapper(dataStore)),
            scheduler = scheduler,
            migrations = migrations.toList()
        )
    }

    internal class NoopSecretProvider : SecretProvider {
        override fun getOrCreateSecret(): Maybe<Secret> = Maybe.empty()
    }

}