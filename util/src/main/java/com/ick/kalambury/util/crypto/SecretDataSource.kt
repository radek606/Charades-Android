package com.ick.kalambury.util.crypto

import io.reactivex.rxjava3.core.Flowable

interface SecretDataSource {

    fun getEncryptedSecret(): Flowable<String>
    fun setEncryptedSecret(secretString: String?)

    fun getUnencryptedSecret(): Flowable<String>
    fun setUnencryptedSecret(secretString: String?)

}