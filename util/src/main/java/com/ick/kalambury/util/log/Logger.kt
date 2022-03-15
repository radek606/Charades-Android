package com.ick.kalambury.util.log

abstract class Logger {

    open fun shouldLog(priority: Log.Level, tag: String?) = true

    abstract fun log(priority: Log.Level, tag: String, message: String?, throwable: Throwable?)

    abstract fun blockUntilAllWritesFinished()

    internal fun logInternal(priority: Log.Level, tag: String, message: String?, throwable: Throwable?) {
        if (shouldLog(priority, tag)) {
            log(priority, tag, message, throwable)
        }
    }

}