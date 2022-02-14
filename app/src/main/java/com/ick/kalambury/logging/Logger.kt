package com.ick.kalambury.logging

abstract class Logger {

    open fun isEnabled(priority: Log.Level, tag: String?) = true

    abstract fun log(priority: Log.Level, tag: String, message: String?, throwable: Throwable?)

    abstract fun blockUntilAllWritesFinished()

    internal fun rawLog(priority: Log.Level, tag: String, message: String?, throwable: Throwable?) {
        if (isEnabled(priority, tag)) {
            log(priority, tag, message, throwable)
        }
    }

}