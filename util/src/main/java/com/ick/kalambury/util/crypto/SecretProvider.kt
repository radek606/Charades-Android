package com.ick.kalambury.util.crypto

import io.reactivex.rxjava3.core.Maybe

interface SecretProvider {

    fun getOrCreateSecret(): Maybe<Secret>

}