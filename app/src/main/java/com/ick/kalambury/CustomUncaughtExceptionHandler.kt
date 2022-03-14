package com.ick.kalambury

import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import java.lang.Thread.UncaughtExceptionHandler

class CustomUncaughtExceptionHandler(
        private val originalHandler: UncaughtExceptionHandler?,
        private val preferenceStorage: MainPreferenceStorage,
) : UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e(logTag, throwable = e)
        preferenceStorage.setAppCrashed(true)
        Log.blockUntilAllWritesFinished()
        originalHandler?.uncaughtException(t, e)
    }

}