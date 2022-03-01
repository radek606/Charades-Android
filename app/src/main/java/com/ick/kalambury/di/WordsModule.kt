package com.ick.kalambury.di

import android.content.Context
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.settings.EncryptionKeysStorage
import com.ick.kalambury.util.SchedulerProvider
import com.ick.kalambury.words.IntegrityCheckMigration
import com.ick.kalambury.words.WordsSecretDataSource
import com.ick.kalambury.wordsrepository.WordsRepository
import com.ick.kalambury.wordsrepository.WordsRepositoryBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WordsModule {

    @Singleton
    @Provides
    fun provideWordsRepository(
        @ApplicationContext context: Context,
        keysStorage: EncryptionKeysStorage,
        provider: SchedulerProvider,
    ): WordsRepository {
        return WordsRepositoryBuilder(context) { File(context.filesDir, BuildConfig.WORDS_ROOT_DIR) }
            .setScheduler(provider.io())
            .setEncryptionSecretDataSource(WordsSecretDataSource(keysStorage))
            .addMigration(IntegrityCheckMigration())
            .build()
    }

}