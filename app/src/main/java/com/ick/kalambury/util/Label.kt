package com.ick.kalambury.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.core.text.parseAsHtml

class Label private constructor(
    @StringRes private val textId: Int = -1,
    private val text: String? = null,
    private val isHtml: Boolean,
    private val params: Array<out Any>,
) {

    fun getText(context: Context): CharSequence {
        if (textId == -1 && text == null) error("Either 'textId' or 'text' must be present.")

        val result = text ?: context.getString(textId, *params)

        return if (isHtml) {
            result.parseAsHtml(HtmlCompat.FROM_HTML_MODE_COMPACT)
        } else {
            result
        }
    }

    companion object {

        fun res(
            @StringRes textId: Int,
            isHtml: Boolean = false,
            vararg params: Any = arrayOf(),
        ) = Label(textId, isHtml = isHtml, params = params)

        fun text(
            text: String,
            isHtml: Boolean = false,
            vararg params: Any = arrayOf(),
        ) = Label(text = text, isHtml = isHtml, params = params)

    }

}