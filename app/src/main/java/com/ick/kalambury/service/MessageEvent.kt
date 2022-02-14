package com.ick.kalambury.service

class MessageEvent(val sourceId: String? = null, val payload: ByteArray) : ConnectionEvent()