package com.ick.kalambury.logsubmit

interface LogSection {

    val title: String
    fun getContent(): String

}