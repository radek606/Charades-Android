package com.ick.kalambury.logsubmit

class LogSectionBlockedThreads : LogSection {

    override val title: String
        get() = "BLOCKED THREADS"

    override fun getContent() = buildString {
        Thread.getAllStackTraces().forEach { (thread, value) ->
            if (thread.state == Thread.State.BLOCKED) {
                append("-- [").append(thread.id).append("] ")
                    .append(thread.name)
                    .append(" (").append(thread.state.toString()).appendLine(")")

                value.forEach { append(it.toString()).appendLine() }

                appendLine()
            }
        }

        if (isEmpty()) append("None")
    }

}