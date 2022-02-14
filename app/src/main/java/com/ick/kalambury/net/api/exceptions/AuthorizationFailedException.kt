/**
 * Copyright (C) 2014-2016 Open Whisper Systems
 *
 * Licensed according to the LICENSE file in this repository.
 */
package com.ick.kalambury.net.api.exceptions

class AuthorizationFailedException : NonSuccessfulResponseException {
    constructor() : super(401)
    constructor(s: String) : super(401, s)
}