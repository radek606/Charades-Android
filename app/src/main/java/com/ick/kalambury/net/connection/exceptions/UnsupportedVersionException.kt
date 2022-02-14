package com.ick.kalambury.net.connection.exceptions

import com.ick.kalambury.net.connection.SupportedVersionInfo

class UnsupportedVersionException : ConnectionException {
    var supportedVersionInfo: SupportedVersionInfo? = null
        private set

    constructor(supportedVersionInfo: SupportedVersionInfo) : super(UNSUPPORTED_VERSION) {
        this.supportedVersionInfo = supportedVersionInfo
    }

    constructor(s: String) : super(UNSUPPORTED_VERSION, s)
}