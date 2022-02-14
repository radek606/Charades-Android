package com.ick.kalambury.net.api.exceptions

import java.io.IOException

open class NonSuccessfulResponseException : IOException {

    val code: Int

    constructor(code: Int) : super() {
        this.code = code
    }
    constructor(code: Int, s: String) : super(s) {
        this.code = code
    }

}