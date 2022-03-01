package com.ick.kalambury.startup

import android.content.Context
import androidx.startup.Initializer
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.di.InitializerEntryPoint
import com.ick.kalambury.logging.CrashlyticsLogger
import com.ick.kalambury.logging.LogFilesRepository
import com.ick.kalambury.logging.PersistentLogger
import com.ick.kalambury.util.SchedulerProvider
import com.ick.kalambury.util.log.AndroidLogger
import com.ick.kalambury.util.log.Log
import javax.inject.Inject

class LogInitializer: Initializer<Unit> {

    @Inject
    lateinit var logFilesRepository: LogFilesRepository

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    override fun create(context: Context) {
        InitializerEntryPoint.resolve(context).inject(this)

        Log.initialize(
            AndroidLogger(),
            CrashlyticsLogger(),
            PersistentLogger(logFilesRepository, BuildConfig.VERSION_NAME, schedulerProvider.single())
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(DependencyGraphInitializer::class.java)
    }

}