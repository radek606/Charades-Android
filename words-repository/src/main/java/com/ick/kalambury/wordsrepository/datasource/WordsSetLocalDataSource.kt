package com.ick.kalambury.wordsrepository.datasource

import android.content.Context
import com.ick.kalambury.util.JsonUtils
import com.ick.kalambury.util.crypto.AESCipherStreamsFactory
import com.ick.kalambury.util.crypto.SecretProvider
import com.ick.kalambury.wordsrepository.BuildConfig
import com.ick.kalambury.wordsrepository.model.WordsSet
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import java.io.*

class WordsSetLocalDataSource (
    private val context: Context,
    private val secretProvider: SecretProvider,
    private val rootDirectory: () -> File
) : WordsSetDataSource {

    override fun getAssetsWordsSet(setId: String): Single<WordsSet> {
        return Single.fromCallable { context.assets.open("words/$setId.json") }
            .map { stream -> stream.use { JsonUtils.fromJson(it, WordsSet::class.java) } }
    }

    override fun getLocalWordsSet(setId: String): Single<WordsSet> {
        return secretProvider.getOrCreateSecret()
            .flatMap(
                { Maybe.just(AESCipherStreamsFactory.create(getFileInputStream(setId), it)) },
                { Maybe.error(it) },
                { Maybe.just(getFileInputStream(setId)) }
            )
            .map { stream -> stream.use { JsonUtils.fromJson(it, WordsSet::class.java) } }
            .toSingle()
    }

    private fun getFileInputStream(setId: String): InputStream {
        return FileInputStream(File(getLocalWordsSetDirectory(setId), BuildConfig.WORDS_SET_FILE_NAME))
    }

    override fun saveWordsSet(setId: String, input: InputStream, fileName: String): Completable {
        return secretProvider.getOrCreateSecret()
            .flatMap(
                { Maybe.just(AESCipherStreamsFactory.create(getFileOutputStream(setId, fileName), it)) },
                { Maybe.error(it) },
                { Maybe.just(getFileOutputStream(setId, fileName)) }
            )
            .flatMapCompletable { output ->
                Completable.fromAction { output.use { input.copyTo(it) } }
                    .doFinally { input.close() }
                    .onErrorComplete()
            }
    }

    private fun getFileOutputStream(setId: String, fileName: String): OutputStream {
        val directory = getLocalWordsSetDirectory(setId)
        if (!directory.exists() ) {
            directory.mkdirs()
        }
        return FileOutputStream(File(directory, fileName))
    }

    override fun getLocalWordsSetDirectory(setId: String) = File(rootDirectory.invoke(), setId)

}