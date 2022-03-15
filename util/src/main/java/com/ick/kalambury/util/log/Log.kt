package com.ick.kalambury.util.log

import androidx.annotation.MainThread
import kotlin.reflect.KClass

object Log {

    enum class Level {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        ASSERT,
    }

    private val loggers: MutableMap<KClass<out Logger>, Logger> = mutableMapOf()

    @MainThread
    fun initialize(vararg loggers: Logger) {
        loggers.forEach { this.loggers[it::class] = it }
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

    /**
     * Log only to specified logger. Need to manually specify log level.
     */
    fun logTo(logger: KClass<out Logger>, priority: Level, tag: String, message: String? = null, throwable: Throwable? = null) {
        loggers[logger]?.logInternal(priority, tag, message, throwable)
    }

    fun blockUntilAllWritesFinished() {
        loggers.values.forEach { it.blockUntilAllWritesFinished() }
    }

    private fun log(priority: Level, tag: String, message: String?, throwable: Throwable?) {
        loggers.values.forEach { it.logInternal(priority, tag, message, throwable) }
    }

}

val <reified T : Any> T.logTag: String
    inline get() = this.javaClass.simpleName