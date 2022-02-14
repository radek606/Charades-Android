package com.ick.kalambury.logging

import com.google.firebase.crashlytics.BuildConfig
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

class CrashlyticsLogger : Logger() {

    private val cachedThreadString: ThreadLocal<String> = ThreadLocal()

    override fun isEnabled(priority: Log.Level, tag: String?): Boolean {
        return priority >= Log.Level.WARNING && !BuildConfig.DEBUG
    }

    override fun log(priority: Log.Level, tag: String, message: String?, throwable: Throwable?) {
        val threadString = cachedThreadString.get()
            ?: "[${Thread.currentThread().id}] ${Thread.currentThread().name}"

        cachedThreadString.set(threadString)

        val crashlytics = Firebase.crashlytics.apply {
            setCustomKey(CRASHLYTICS_KEY_PRIORITY, priority.name)
            setCustomKey(CRASHLYTICS_KEY_TAG, tag)
            setCustomKey(CRASHLYTICS_KEY_THREAD, threadString)
        }

        if (throwable == null) {
            if (message != null) {
                crashlytics.log(message)
            }
        } else {
            if (message != null) {
                crashlytics.setCustomKey(CRASHLYTICS_KEY_MESSAGE, message)
            }
            crashlytics.recordException(throwable)
        }
    }

    override fun blockUntilAllWritesFinished() {}

    companion object {
        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_THREAD = "thread"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }

}
