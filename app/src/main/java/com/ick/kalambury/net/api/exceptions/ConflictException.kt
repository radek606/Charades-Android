package com.ick.kalambury.net.api.exceptions

class ConflictException : NonSuccessfulResponseException {
    constructor() : super(409)
    constructor(s: String) : super(409, s)
}