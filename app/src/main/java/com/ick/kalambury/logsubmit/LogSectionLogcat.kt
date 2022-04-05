package com.ick.kalambury.logsubmit

import java.io.IOException

class LogSectionLogcat : LogSection {

    override val title: String
        get() = "LOGCAT"

    override fun getContent() = buildString {
        try {
            Runtime.getRuntime()
                .exec("logcat -d")
                .inputStream
                .bufferedReader()
                .forEachLine { appendLine(it) }
        } catch (e: IOException) {
            append("Failed to retrieve logs.")
        }
    }

}