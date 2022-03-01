package com.ick.kalambury.wordsrepository.datasource

import android.content.Context
import com.ick.kalambury.util.JsonUtils
import com.ick.kalambury.util.crypto.AESCipherStreamsFactory
import com.ick.kalambury.util.crypto.SecretProvider
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import com.ick.kalambury.wordsrepository.model.WordsInstance
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import java.io.InputStream
import java.io.OutputStream

class WordsInstanceLocalDataSource(
    private val context: Context,
    private val secretProvider: SecretProvider,
) : WordsInstanceDataSource {

    override fun getWordsInstance(instance: String): Single<WordsInstance> {
        return secretProvider.getOrCreateSecret()
            .flatMap(
                { Maybe.just(AESCipherStreamsFactory.create(getInputStream(instance), it)) },
                { Maybe.error(it) },
                { Maybe.just(getInputStream(instance)) }
            )
            .toSingle()
            .flatMap {
                Single.fromCallable { it.use { JsonUtils.fromJson(it, WordsInstance::class.java) } }
            }
    }

    private fun getInputStream(instance: String): InputStream {
        return context.openFileInput(instance)
    }

    override fun saveWordsInstance(instance: WordsInstance): Completable {
        return secretProvider.getOrCreateSecret()
            .flatMap(
                { Maybe.just(AESCipherStreamsFactory.create(getOutputStream(instance.id), it)) },
                { Maybe.error(it) },
                { Maybe.just(getOutputStream(instance.id)) }
            )
            .flatMapCompletable {
                Completable.fromCallable { it.use { JsonUtils.toJson(it, instance) } }
            }
            .doOnComplete { Log.d(logTag(), "Saved instance: ${instance.id}") }
            .doOnError {
                Log.w(logTag(), "Failed saving instance: ${instance.id}. Deleting...")
                context.deleteFile(instance.id)
            }
            .onErrorComplete()
    }

    private fun getOutputStream(instance: String): OutputStream {
        return context.openFileOutput(instance, Context.MODE_PRIVATE)
    }

}