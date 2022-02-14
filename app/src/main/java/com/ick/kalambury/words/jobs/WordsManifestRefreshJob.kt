package com.ick.kalambury.words.jobs

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import androidx.work.rxjava3.RxWorker
import com.ick.kalambury.logging.Log
import com.ick.kalambury.util.logTag
import com.ick.kalambury.words.WordsManifest
import com.ick.kalambury.words.WordsManifestDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.rxjava3.core.Single
import javax.inject.Named


@HiltWorker
class WordsManifestRefreshJob @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    @Named("wordsManifestLocalDataSource") private val manifestLocalSource: WordsManifestDataSource,
    @Named("wordsManifestRemoteDataSource") private val manifestRemoteSource: WordsManifestDataSource,
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        return manifestRemoteSource.getWordsManifest()
            .flatMap {
                if (it.version == 0) {
                    Log.d(logTag(), "Remote words manifest not yet fetched. Retrying...")
                    Single.just(Result.retry())
                } else {
                    handleUpdate(it)
                }
            }
    }

    private fun handleUpdate(remoteManifest: WordsManifest): Single<Result> {
        return manifestLocalSource.getWordsManifest()
            .map { localManifest ->
                when {
                    localManifest.version == 0 -> {
                        val size = remoteManifest.sets.size
                        Log.d(logTag(), "Local words manifest version: 0! Updating all words sets ($size of $size)")

                        scheduleDownloadJobs(remoteManifest.key,
                            remoteManifest.iv,
                            remoteManifest.sets.map { it.id }.toSet())

                        Result.success()
                    }
                    localManifest.version == remoteManifest.version -> {
                        Log.d(logTag(), "Words sets already actual. Skipping.")
                        Result.success()
                    }
                    localManifest.version > remoteManifest.version -> {
                        Log.w(logTag(), "Local words manifest version higher than remote! " +
                                "Local version: ${localManifest.version}, remote version: ${remoteManifest.version}")
                        Result.failure()
                    }
                    else -> {
                        val oldSets = localManifest.sets.associateBy { it.id }
                        val setsToUpdate = remoteManifest.sets
                            .filter { new -> // get sets that are not present in local manifest or have lower version
                                val old = oldSets[new.id]
                                old == null || new.version > old.version
                            }
                            .map { it.id }
                            .toSet()

                        Log.d(logTag(), "Manifest versions: local: ${localManifest.version}, remote: ${remoteManifest.version}. " +
                                "Updating ${setsToUpdate.size} of ${remoteManifest.sets.size} words sets.")

                        scheduleDownloadJobs(remoteManifest.key, remoteManifest.iv, setsToUpdate)

                        Result.success()
                    }
                }
            }
    }

    private fun scheduleDownloadJobs(key: ByteArray, iv: ByteArray, setIds: Set<String>) {
        val downloadJobs: MutableList<OneTimeWorkRequest> = ArrayList(setIds.size)

        for (id in setIds) {
            val data = Data.Builder()
                .putString(WordsSetDownloadJob.KEY_WORDS_SET_ID, id)
                .putByteArray(WordsSetDownloadJob.KEY_SECRET_KEY, key)
                .putByteArray(WordsSetDownloadJob.KEY_SECRET_IV, iv)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            downloadJobs.add(OneTimeWorkRequest.Builder(WordsSetDownloadJob::class.java)
                .addTag(id)
                .setConstraints(constraints)
                .setInputData(data)
                .build())
        }

        val updateFinishJob = OneTimeWorkRequest.Builder(WordsSetsUpdateFinishJob::class.java)
            .setInputMerger(ArrayCreatingInputMerger::class.java)
            .build()

        WorkManager.getInstance(context)
            .beginWith(downloadJobs)
            .then(updateFinishJob)
            .enqueue()
    }

}