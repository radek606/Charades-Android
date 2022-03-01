package com.ick.kalambury.util.log

import androidx.annotation.MainThread

object Log {

    enum class Level {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        ASSERT,
    }

    private val loggers: MutableList<Logger> = mutableListOf()

    @MainThread
    fun initialize(vararg logger: Logger) {
        loggers.addAll(logger)
    }

    fun v(tag: String, message: String? = null, throwable: Throwable? = null) {
        log(Level.VERBOSE, tag, message, throwable)
    }

    fun i(tag: String, message: String? = null, throwable: Throwable? = null) {
        log(Level.INFO, tag, message, throwable)
    }

    fun d(tag: String, message: String? = null, throwable: Throwable? = null) {
        log(Level.DEBUG, tag, message, throwable)
    }

    fun w(tag: String, message: String? = null, throwable: Throwable? = null) {
        log(Level.WARNING, tag, message, throwable)
    }

    fun e(tag: String, message: String? = null, throwable: Throwable? = null) {
        log(Level.ERROR, tag, message, throwable)
    }

    fun wtf(tag: String, message: String? = null, throwable: Throwable? = null) {
        log(Level.ASSERT, tag, message, throwable)
    }

    fun blockUntilAllWritesFinished() {
        loggers.forEach { it.blockUntilAllWritesFinished() }
    }

    private fun log(priority: Level, tag: String, message: String?, throwable: Throwable?) {
        loggers.forEach { it.rawLog(priority, tag, message, throwable) }
    }

}

inline fun <reified T : Any> T.logTag(): String = this.javaClass.simpleName