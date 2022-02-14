package com.ick.kalambury.net.api.exceptions

import java.io.IOException

class NetworkFailureException : IOException {
    constructor(exception: Throwable) : super(exception)
    constructor(s: String) : super(s)
}