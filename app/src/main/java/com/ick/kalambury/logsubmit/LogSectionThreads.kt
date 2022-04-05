package com.ick.kalambury.logsubmit

class LogSectionThreads : LogSection {

    override val title: String
        get() = "THREADS"

    override fun getContent() = buildString {
        Thread.getAllStackTraces()
            .keys
            .sortedWith(compareBy(Thread::getId))
            .forEach {
                append("[").append(it.id).append("] ").append(it.name).appendLine()
            }
    }
}