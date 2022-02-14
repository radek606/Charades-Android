package com.ick.kalambury.logsubmit

import com.ick.kalambury.logging.LogFilesRepository

class LogSectionLogger(private val logFilesRepository: LogFilesRepository) : LogSection {

    override val title: String
        get() = "LOGGER"

    override fun getContent(): String {
        return logFilesRepository.getLogs()
            .onErrorReturnItem("Failed to retrieve logs.")
            .blockingGet()
    }

}