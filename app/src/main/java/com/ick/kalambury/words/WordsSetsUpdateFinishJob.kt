package com.ick.kalambury.words

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import com.ick.kalambury.remoteconfig.RemoteConfigHelper
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import com.ick.kalambury.wordsrepository.BuildConfig
import com.ick.kalambury.wordsrepository.WordsRepository
import com.ick.kalambury.wordsrepository.model.WordsManifest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.rxjava3.core.Single
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.moveTo

@HiltWorker
class WordsSetsUpdateFinishJob @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val wordsRepository: WordsRepository,
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        return Single.fromCallable {
            val manifest = WordsManifest.fromString(RemoteConfigHelper.remoteWordsManifest)

            val failedSets = inputData.getStringArray(WordsSetDownloadJob.KEY_FAILURE)
            if (failedSets != null) {
                Log.w(
                    logTag(),
                    "${failedSets.size} download jobs failed. Removing all new files..."
                )

                manifest.sets.forEach { set ->
                    val dir = wordsRepository.updater.getLocalWordsSetDirectory(set.id)
                    File(dir, "new").delete()
                    //if this set was new, so no previous version existed, delete its directory
                    if (dir.list().isNullOrEmpty()) {
                        dir.delete()
                    }
                }
                throw IllegalStateException("Failed download job!")
            }

            val succeededSets = inputData.getStringArray(WordsSetDownloadJob.KEY_SUCCESS)
            if (succeededSets == null) {
                Log.w(logTag(), "No successful download jobs!")
                throw IllegalStateException("Empty succeeded sets array!")
            }

            Log.d(
                logTag(),
                "All of ${succeededSets.size} download jobs succeeded. Updating words sets..."
            )

            succeededSets.forEach { setId ->
                val dir = wordsRepository.updater.getLocalWordsSetDirectory(setId)
                val new = File(dir, "new")
                val current = File(dir, BuildConfig.WORDS_SET_FILE_NAME)
                Path(new.path).moveTo(Path(current.path), true)
            }

            manifest
        }
            .flatMapCompletable {
                Log.d(logTag(), "Finished updating words sets. Saving new manifest...")

                wordsRepository.updater.saveWordsManifest(it)
            }
            .doOnComplete { wordsRepository.updater.finishUpdate() }
            .toSingle(Result::success)
            .onErrorReturnItem(Result.failure())
    }

}