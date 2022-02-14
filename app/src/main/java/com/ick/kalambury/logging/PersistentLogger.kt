package com.ick.kalambury.logging

import android.os.Looper
import com.ick.kalambury.util.SchedulerProvider
import com.ick.kalambury.util.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.getOrSet
import android.util.Log as SystemLog

class PersistentLogger(
    private val repository: LogFilesRepository,
    private val logTag: String,
    schedulerProvider: SchedulerProvider,
) : Logger() {

    private val cachedThreadString: ThreadLocal<String> = ThreadLocal()

    private val sequentialScheduler: Scheduler = schedulerProvider.single()

    override fun log(priority: Log.Level, tag: String, message: String?, throwable: Throwable?) {
        for (entry in buildLogEntries(priority.toValue(), tag, message, throwable, threadString)) {
            repository.write(entry)
                .subscribeOn(sequentialScheduler)
                .subscribeBy(onError = { SystemLog.w(logTag(), "Failed to write line.") })
        }
    }

    override fun blockUntilAllWritesFinished() {
        Completable.fromAction { repository.close() }
            .subscribeOn(sequentialScheduler)
            .blockingAwait()
    }

    private fun buildLogEntries(
        level: String,
        tag: String,
        message: String?,
        throwable: Throwable?,
        threadString: String
    ): List<String> {
        val entries: MutableList<String> = mutableListOf()
        val date = Date()

        val fullMessage = if (message != null) {
            if (throwable != null) {
                "$message\n${throwable.stackTraceToString()}"
            } else {
                message
            }
        } else throwable?.stackTraceToString()

        fullMessage?.split("\\n".toRegex())?.forEach {
            entries.add(buildEntry(level, tag, it, date, threadString))
        }

        return entries
    }

    private fun buildEntry(
        level: String,
        tag: String,
        message: String?,
        date: Date,
        threadString: String
    ): String {
        return "[$logTag] [$threadString] ${DATE_FORMAT.format(date)} $level $tag $message"
    }

    private val threadString: String
        get() {
            return cachedThreadString.getOrSet {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    "main"
                } else {
                    String.format("%-5s", Thread.currentThread().id)
                }
            }
        }

    private fun Log.Level.toValue() = when (this) {
        Log.Level.VERBOSE -> "V"
        Log.Level.DEBUG -> "D"
        Log.Level.INFO -> "I"
        Log.Level.WARNING -> "W"
        Log.Level.ERROR -> "E"
        Log.Level.ASSERT -> "A"
    }

    companion object {

        private val DATE_FORMAT = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

    }

}