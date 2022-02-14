package com.ick.kalambury.service

data class Endpoint(val id: String, val name: String? = null) {

    companion object {
        const val DEFAULT_ID = "no_id"
        val DEFAULT = Endpoint(DEFAULT_ID)
    }

}
