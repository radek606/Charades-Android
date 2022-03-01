package com.ick.kalambury.logsubmit

class LogSectionThreads : LogSection {

    override val title: String
        get() = "THREADS"

    override fun getContent(): String {
        val builder = StringBuilder()

        Thread.getAllStackTraces()
            .keys
            .sortedWith(compareBy(Thread::getId))
            .forEach {
                builder.append("[").append(it.id).append("] ").append(it.name).appendLine()
            }

        return builder.toString()
    }
}