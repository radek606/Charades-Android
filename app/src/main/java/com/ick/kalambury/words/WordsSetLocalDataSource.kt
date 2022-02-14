package com.ick.kalambury.words

import android.content.Context
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.util.JsonUtils
import com.ick.kalambury.util.closeSilently
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream


class WordsSetLocalDataSource (
    private val context: Context,
    private val secretProvider: WordsSecretProvider,
) : WordsSetDataSource {

    override fun getWordsSet(setId: String): Single<WordsSet> {
        return getLocalFileStream(setId)
            .onErrorResumeNext { getAssetsFileStream(setId) }
            .map { stream -> stream.use { JsonUtils.fromJson(it, WordsSet::class.java) } }
    }

    private fun getAssetsFileStream(setId: String): Single<InputStream> {
        return Single.fromCallable {
            context.assets.open("${BuildConfig.LOCAL_WORDS_ROOT_DIR}/$setId.json")
        }
    }

    private fun getLocalFileStream(setId: String): Single<InputStream> {
        return Single.fromCallable { secretProvider.getOrCreateSecret() }
            .flatMap {
                Single.fromCallable {
                    val inputFile = File(getWordsSetDirectory(setId), BuildConfig.LOCAL_WORDS_SET_FILE_NAME)
                    AESCipherStreamsFactory.create(FileInputStream(inputFile), it)
                }
            }
    }

    override fun saveWordsSet(setId: String, input: InputStream, fileName: String): Completable {
        return Single.fromCallable { secretProvider.getOrCreateSecret() }
            .flatMapCompletable {
                Completable.fromAction {
                    val outputFile = File(getWordsSetDirectory(setId), fileName)
                    AESCipherStreamsFactory.create(FileOutputStream(outputFile), it).use {
                        input.copyTo(it)
                        input.closeSilently()
                    }
                }
            }
    }

    override fun getWordsSetDirectory(setId: String, createIfNecessary: Boolean): File {
        val directory = File(context.filesDir, "${BuildConfig.LOCAL_WORDS_ROOT_DIR}/$setId")
        if (!directory.exists() || createIfNecessary) {
            directory.mkdirs()
        }
        return directory
    }

}