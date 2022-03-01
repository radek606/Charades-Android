package com.ick.kalambury.util

import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.Logger

class TestLogger : Logger() {

    override fun log(priority: Log.Level, tag: String, message: String?, throwable: Throwable?) {
        println("${priority.toValue()} $tag $message")
    }

    override fun blockUntilAllWritesFinished() {}

    private fun Log.Level.toValue() = when (this) {
        Log.Level.VERBOSE -> "V"
        Log.Level.DEBUG -> "D"
        Log.Level.INFO -> "I"
        Log.Level.WARNING -> "W"
        Log.Level.ERROR -> "E"
        Log.Level.ASSERT -> "A"
    }

}