package com.ick.kalambury.util.log

import kotlin.math.min
import android.util.Log as SystemLog

class AndroidLogger : Logger() {

    override fun log(priority: Log.Level, tag: String, message: String?, throwable: Throwable?) {
        val fullMessage = if (message != null) {
            if (throwable != null) {
                "$message\n${throwable.stackTraceToString()}"
            } else {
                message
            }
        } else throwable?.stackTraceToString() ?: return

        if (fullMessage.length < MAX_LOG_LENGTH) {
            if (priority == Log.Level.ASSERT) {
                SystemLog.wtf(tag, fullMessage)
            } else {
                SystemLog.println(priority.toValue(), tag, fullMessage)
            }
            return
        }

        // Split by line, then ensure each line can fit into Log's maximum length.
        var i = 0
        val length = fullMessage.length
        while (i < length) {
            var newline = fullMessage.indexOf('\n', i)
            newline = if (newline != -1) newline else length
            do {
                val end = min(newline, i + MAX_LOG_LENGTH)
                val part = fullMessage.substring(i, end)
                if (priority == Log.Level.ASSERT) {
                    SystemLog.wtf(tag, part)
                } else {
                    SystemLog.println(priority.toValue(), tag, part)
                }
                i = end
            } while (i < newline)
            i++
        }
    }

    override fun blockUntilAllWritesFinished() { }

    private fun Log.Level.toValue() = when (this) {
        Log.Level.VERBOSE -> SystemLog.VERBOSE
        Log.Level.DEBUG -> SystemLog.DEBUG
        Log.Level.INFO -> SystemLog.INFO
        Log.Level.WARNING -> SystemLog.WARN
        Log.Level.ERROR -> SystemLog.ERROR
        Log.Level.ASSERT -> SystemLog.ASSERT
    }

    companion object {
        private const val MAX_LOG_LENGTH = 4000
    }

}