package com.ick.kalambury.startup

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.CustomUncaughtExceptionHandler
import com.ick.kalambury.di.InitializerEntryPoint
import com.ick.kalambury.settings.MainPreferenceStorage
import javax.inject.Inject

class CrashHandlingInitializer: Initializer<Unit> {

    @Inject
    lateinit var mainPreferences: MainPreferenceStorage

    override fun create(context: Context) {
        InitializerEntryPoint.resolve(context).inject(this)

        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(CustomUncaughtExceptionHandler(originalHandler, mainPreferences))
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(DependencyGraphInitializer::class.java)
    }

}