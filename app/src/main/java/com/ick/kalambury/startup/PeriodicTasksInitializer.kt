package com.ick.kalambury.startup

import android.content.Context
import androidx.startup.Initializer
import androidx.work.*
import com.ick.kalambury.words.WordsManifestRefreshJob
import java.util.concurrent.TimeUnit

class PeriodicTasksInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("refresh_words_manifest", ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequest.Builder(WordsManifestRefreshJob::class.java, 1, TimeUnit.DAYS)
                    .setInitialDelay(10, TimeUnit.SECONDS)
                    .setConstraints(
                        Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                    .build())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(WorkManagerInitializer::class.java)
    }

}