package com.ick.kalambury.di

import android.content.Context
import com.ick.kalambury.words.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class WordsModule {

    @Singleton
    @Provides
    @Named("wordsManifestLocalDataSource")
    fun provideWordsManifestLocalDataSource(
        @ApplicationContext context: Context,
    ): WordsManifestDataSource {
        return WordsManifestLocalDataSource(context)
    }

    @Singleton
    @Provides
    @Named("wordsManifestRemoteDataSource")
    fun provideWordsManifestRemoteDataSource(): WordsManifestDataSource {
        return WordsManifestRemoteDataSource()
    }

    @Singleton
    @Provides
    fun provideWordsSetLocalDataSource(
        @ApplicationContext context: Context,
        secretProvider: WordsSecretProvider,
    ): WordsSetDataSource {
        return WordsSetLocalDataSource(context, secretProvider)
    }

    @Singleton
    @Provides
    fun provideWordsInstanceLocalDataSource(
        @ApplicationContext context: Context,
        secretProvider: WordsSecretProvider,
    ): WordsInstanceDataSource {
        return WordsInstanceLocalDataSource(context, secretProvider)
    }

}