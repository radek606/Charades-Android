package com.ick.kalambury.net.api.exceptions

class BadRequestException : NonSuccessfulResponseException {
    constructor() : super(400)
    constructor(s: String) : super(400, s)
}