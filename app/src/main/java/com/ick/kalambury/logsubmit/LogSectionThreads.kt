package com.ick.kalambury.logsubmit

import java.util.*

class LogSectionThreads : LogSection {

    override val title: String
        get() = "THREADS"

    override fun getContent(): String {
        val builder = StringBuilder()

        Thread.getAllStackTraces()
            .keys
            .sortedWith(Comparator.comparing(Thread::getId))
            .forEach {
                builder.append("[").append(it.id).append("] ").append(it.name).appendLine()
            }

        return builder.toString()
    }
}