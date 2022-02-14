package com.ick.kalambury.logsubmit

class LogSectionBlockedThreads : LogSection {

    override val title: String
        get() = "BLOCKED THREADS"

    override fun getContent(): String {
        val builder = StringBuilder()

        Thread.getAllStackTraces().forEach { (thread, value) ->
            if (thread.state == Thread.State.BLOCKED) {
                builder.append("-- [").append(thread.id).append("] ")
                    .append(thread.name)
                    .append(" (").append(thread.state.toString()).appendLine(")")

                value.forEach { builder.append(it.toString()).appendLine() }

                builder.appendLine()
            }
        }

        return if (builder.isEmpty()) "None" else builder.toString()
    }
}