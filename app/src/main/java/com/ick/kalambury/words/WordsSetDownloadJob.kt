package com.ick.kalambury.words

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import com.ick.kalambury.net.api.RestApiManager
import com.ick.kalambury.net.api.exceptions.NetworkFailureException
import com.ick.kalambury.util.crypto.AESCipherStreamsFactory
import com.ick.kalambury.util.crypto.Secret
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import com.ick.kalambury.wordsrepository.WordsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.rxjava3.core.Single
import java.io.ByteArrayInputStream

@HiltWorker
class WordsSetDownloadJob @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val wordsRepository: WordsRepository,
    private val restApiManager: RestApiManager,
) : RxWorker(context, workerParams) {

    companion object {
        const val KEY_WORDS_SET_ID = "words_set_id"
        const val KEY_SECRET_KEY = "secret_key"
        const val KEY_SECRET_IV = "secret_iv"

        const val KEY_SUCCESS = "success"
        const val KEY_FAILURE = "failure"
    }

    override fun createWork(): Single<Result> {
        val setId = inputData.getString(KEY_WORDS_SET_ID) ?: return Single.just(Result.failure())
        val key = inputData.getByteArray(KEY_SECRET_KEY) ?: return Single.just(Result.failure())
        val iv = inputData.getByteArray(KEY_SECRET_IV) ?: return Single.just(Result.failure())

        return restApiManager.getWordsSet(setId)
            .flatMap { result ->
                result.fold({
                    val stream = AESCipherStreamsFactory.create(ByteArrayInputStream(it), Secret(key, iv), "AES/CBC/PKCS5Padding")
                    wordsRepository.updater.saveWordsSet(setId, stream, "new")
                        .toSingle { success(setId) }
                        .onErrorReturn { failure(setId) }
                }, {
                    when(it) {
                        is NetworkFailureException -> {
                            Log.w(logTag(), "Network fail during words set download. Retrying...", it)
                            Single.just(Result.retry())
                        }
                        else -> {
                            Log.w(logTag(), "Error during words set download. Aborting...", it)
                            Single.just(failure(setId))
                        }
                    }
                })
            }
    }

    private fun success(setId: String): Result {
        return Result.success(Data.Builder().putString(KEY_SUCCESS, setId).build())
    }

    private fun failure(setId: String): Result {
        return Result.success(Data.Builder().putString(KEY_FAILURE, setId).build())
    }

}