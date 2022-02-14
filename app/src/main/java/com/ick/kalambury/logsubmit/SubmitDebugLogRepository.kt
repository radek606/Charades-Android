package com.ick.kalambury.logsubmit

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class SubmitDebugLogRepository @Inject constructor(
    private val logSections: ArrayList<LogSection>,
) {

    fun getComposedLog(): Single<String> {
        return Observable.fromIterable(logSections)
            .reduce(StringBuilder()) { builder, section ->
                builder.apply {
                    appendLine(section.title)
                    append(section.getContent())
                    repeat(SECTION_SPACING) { appendLine() }
                }
            }
            .map(StringBuilder::toString)
    }

    companion object {
        private const val SECTION_SPACING = 3
    }

}