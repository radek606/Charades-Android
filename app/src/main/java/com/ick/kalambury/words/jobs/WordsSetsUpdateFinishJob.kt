package com.ick.kalambury.words.jobs

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.logging.Log
import com.ick.kalambury.util.logTag
import com.ick.kalambury.words.WordsManifestDataSource
import com.ick.kalambury.words.WordsRepository
import com.ick.kalambury.words.WordsSetDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.rxjava3.core.Single
import java.io.File
import javax.inject.Named
import kotlin.io.path.Path
import kotlin.io.path.moveTo

@HiltWorker
class WordsSetsUpdateFinishJob @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    @Named("wordsManifestRemoteDataSource") private val manifestRemoteDataSource: WordsManifestDataSource,
    private val wordsSetLocalDataSource: WordsSetDataSource,
    private val wordsRepository: WordsRepository,
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        return manifestRemoteDataSource.getWordsManifest()
            .map { manifest ->
                val failedSets = inputData.getStringArray(WordsSetDownloadJob.KEY_FAILURE)
                if (failedSets != null) {
                    Log.w(
                        logTag(),
                        "${failedSets.size} download jobs failed. Removing all new files..."
                    )

                    manifest.sets.forEach { set ->
                        val dir = wordsSetLocalDataSource.getWordsSetDirectory(set.id, false)
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
                    val dir = wordsSetLocalDataSource.getWordsSetDirectory(setId)
                    val new = File(dir, "new")
                    val current = File(dir, BuildConfig.LOCAL_WORDS_SET_FILE_NAME)
                    Path(new.path).moveTo(Path(current.path), true)
                }

                manifest
            }
            .flatMapCompletable {
                Log.d(logTag(), "Finished updating words sets. Saving new manifest...")

                wordsRepository.updateManifest(it)
            }
            .toSingle(Result::success)
            .onErrorReturnItem(Result.failure())
    }

}