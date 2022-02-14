package com.ick.kalambury.net

import java.io.InputStream

interface TrustStore {
    val keyStoreInputStream: InputStream
    val keyStoreType: String
    val keyStorePassword: String
}