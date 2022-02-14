package com.ick.kalambury.net.connection.exceptions

import java.io.IOException

open class ConnectionException : IOException {
    var status = 0
        private set

    constructor(t: Throwable) : super(t)
    constructor(status: Int) : super() {
        this.status = status
    }

    constructor(status: Int, s: String) : super(s) {
        this.status = status
    }

    companion object {
        const val UNSUPPORTED_VERSION = 100
        const val KICKED = 101
        const val LIMIT_EXCEEDED = 102
        const val TABLE_NOT_FOUND = 103
    }
}