package com.ick.kalambury.words

import android.content.Context
import com.ick.kalambury.logging.Log
import com.ick.kalambury.util.JsonUtils
import com.ick.kalambury.util.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class WordsInstanceLocalDataSource(
    private val context: Context,
    private val secretProvider: WordsSecretProvider,
) : WordsInstanceDataSource {

    override fun getWordsInstance(instance: String): Single<WordsInstance> {
        return Single.fromCallable { secretProvider.getOrCreateSecret() }
            .flatMap {
                Single.fromCallable {
                    AESCipherStreamsFactory.create(context.openFileInput(instance), it).use {
                        JsonUtils.fromJson(it, WordsInstance::class.java)
                    }
                }
            }

    }

    override fun saveWordsInstance(instance: WordsInstance): Completable {
        return Single.fromCallable { secretProvider.getOrCreateSecret() }
            .flatMapCompletable {
                Completable.fromCallable {
                    AESCipherStreamsFactory.create(context.openFileOutput(instance.id,
                        Context.MODE_PRIVATE), it).use {
                        JsonUtils.toJson(it, instance)
                    }
                }
            }
            .doOnComplete { Log.d(logTag(), "Saved instance: ${instance.id}") }
            .doOnError { Log.d(logTag(), "Failed saving instance: ${instance.id}") }
    }

}