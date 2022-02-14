package com.ick.kalambury.logsubmit

import java.io.IOException

class LogSectionLogcat : LogSection {

    override val title: String
        get() = "LOGCAT"

    override fun getContent(): String {
        return try {
            val log = StringBuilder()

            Runtime.getRuntime()
                .exec("logcat -d")
                .inputStream
                .bufferedReader()
                .forEachLine { log.appendLine(it) }

            log.toString()
        } catch (e: IOException) {
            "Failed to retrieve logs."
        }
    }

}