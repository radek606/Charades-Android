package com.ick.kalambury.logging

import android.content.Context
import com.ick.kalambury.util.closeSilently
import com.ick.kalambury.util.logTag
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.*
import java.util.*
import javax.inject.Inject
import android.util.Log as SystemLog

class LogFilesRepository @Inject constructor(@ApplicationContext val context: Context) {

    private var writer: Writer? = null

    fun write(entry: String): Completable {
        return Completable.fromAction {
            if (writer == null) {
                writer = Writer(getOrCreateActiveLogFile())
            }

            if (writer!!.getLogSize() >= MAX_LOG_SIZE) {
                writer!!.close()
                writer = Writer(createNewLogFile())
                trimLogFilesOverMax()
            }

            writer!!.write(entry)
        }.doOnError {
            writer!!.closeAndDelete()
            writer = Writer(createNewLogFile())
        }
    }

    fun getLogs(): Single<String> {
        return Single.fromCallable {
            buildString {
                getSortedLogFiles().forEachIndexed { index, file ->
                    try {
                        append(Reader(file).readAll())
                    } catch (e: IOException) {
                        SystemLog.w(logTag(), "Failed to read log file at index $index. Removing...")
                        file.delete()
                    }
                }
            }
        }
    }

    fun close() {
        writer?.close()
    }

    private fun trimLogFilesOverMax() {
        val logs = getSortedLogFiles()
        if (logs.size > MAX_LOG_FILES) {
            for (i in MAX_LOG_FILES until logs.size) {
                logs[i].delete()
            }
        }
    }

    private fun getOrCreateActiveLogFile(): File {
        val logs = getSortedLogFiles()
        return if (logs.isNotEmpty()) {
            logs[0]
        } else {
            createNewLogFile()
        }
    }

    private fun createNewLogFile(): File {
        return File(getOrCreateLogDirectory(), FILENAME_PREFIX + System.currentTimeMillis())
    }

    private fun getSortedLogFiles(): List<File> {
        return getOrCreateLogDirectory().listFiles()
            ?.sortedWith(Comparator.comparing(File::getName).reversed()) ?: listOf()
    }

    private fun getOrCreateLogDirectory(): File {
        val logDir = File(context.cacheDir, LOG_DIRECTORY)
        if (!logDir.exists()) {
            logDir.mkdir()
        }
        return logDir
    }

    companion object {

        private const val LOG_DIRECTORY = "log"
        private const val FILENAME_PREFIX = "log-"
        private const val MAX_LOG_FILES = 5
        private const val MAX_LOG_SIZE = 300 * 1024

    }

    private class Reader(val file: File) {

        fun readAll() = buildString { FileReader(file).forEachLine { appendLine(it) } }

    }

    private class Writer(val file: File) {

        private val outputStream: BufferedWriter = BufferedWriter(FileWriter(file, true))

        fun write(entry: String) {
            outputStream.appendLine(entry)
            outputStream.flush()
        }

        fun getLogSize(): Long {
            return file.length()
        }

        fun close() {
            outputStream.flush()
            outputStream.close()
        }

        fun closeAndDelete() {
            outputStream.closeSilently()
            file.delete()
        }

    }

}