package com.ick.kalambury.net

import android.content.Context
import com.ick.kalambury.R
import java.io.InputStream

class BackendServiceTrustStore(val context: Context) : TrustStore {

    override val keyStoreInputStream: InputStream
        get() = context.resources.openRawResource(-1) //R.raw.truststore

    override val keyStoreType: String
        get() = "PKCS12"

    override val keyStorePassword: String
        get() = ""

}