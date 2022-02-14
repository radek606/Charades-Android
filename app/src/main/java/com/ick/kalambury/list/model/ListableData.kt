package com.ick.kalambury.list.model

interface ListableData {

    companion object {
        const val DEFAULT_ID = "no_id"
    }

    val id: String
    val text: CharSequence
    val selected: Boolean

}
