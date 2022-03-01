package com.ick.kalambury.wordsrepository.model

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class Word constructor(
    @JsonProperty("text")
    val variants: List<String>,

    @JsonIgnore
    var setName: String? = null,
) {

    @VisibleForTesting
    internal constructor(variants: List<String>) : this(variants, null)

    @get:JsonIgnore
    val wordString: String
        get() = variants[0]

}
