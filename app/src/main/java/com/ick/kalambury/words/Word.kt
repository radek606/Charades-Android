package com.ick.kalambury.words

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Word constructor(
    @JsonProperty("text")
    val variants: List<String>,

    @JsonIgnore
    var setName: String? = null,
) : Parcelable {

    @VisibleForTesting
    internal constructor(variants: List<String>) : this(variants, null)

    @get:JsonIgnore
    val wordString: String
        get() = variants[0]

}
