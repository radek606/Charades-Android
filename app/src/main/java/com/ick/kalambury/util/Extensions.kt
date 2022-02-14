package com.ick.kalambury.util

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.util.Base64
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.protobuf.GeneratedMessageLite
import java.io.Closeable

fun Context.showMessageDialog(
    @StringRes title: Int? = null,
    @StringRes messageId: Int? = null,
    messageString: CharSequence? = null,
    @StringRes positiveButton: Int = android.R.string.ok,
    @StringRes neutralButton: Int? = null,
    @StringRes negativeButton: Int? = null,
    cancelable: Boolean = true,
    listener: ((DialogInterface, Int) -> Unit)? = null,
): AlertDialog {
    return MaterialAlertDialogBuilder(this).apply {
        if(title != null) setTitle(title)
        if(messageId != null) setMessage(messageId) else setMessage(messageString)
        setPositiveButton(positiveButton, listener)
        if (neutralButton != null) setNeutralButton(neutralButton, listener)
        if (negativeButton != null) setNegativeButton(negativeButton, listener)
    }.create().apply {
        setCancelable(cancelable)
        setCanceledOnTouchOutside(cancelable)
        show()
    }
}

fun ViewGroup.inflate(@LayoutRes resource: Int, attachToRoot: Boolean = false) : View {
    return LayoutInflater.from(this.context).inflate(resource, this, attachToRoot)
}

fun Context.inflate(@LayoutRes resource: Int, root: ViewGroup? = null, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(this).inflate(resource, root, attachToRoot)
}

fun Context.toast(@StringRes message: Int, length: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, length).show()
}

fun View.snackbar(@StringRes message: Int, length: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, length).show()
}

inline fun View.snackbar(@StringRes message: Int, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit) {
    val snackbar = Snackbar.make(this, message, length)
    snackbar.f()
    snackbar.show()
}

fun Snackbar.action(@StringRes actionRes: Int, @ColorRes color: Int? = null, listener: (View) -> Unit) {
    setAction(actionRes, listener)
    color?.let { setActionTextColor(ContextCompat.getColor(context, color)) }
}

fun Closeable.closeSilently() {
    try {
        close()
    } catch (_: Exception) {
    }
}

inline val Number.toPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics)

inline fun <reified T : Any> T.logTag(): String = this.javaClass.simpleName

inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}

fun GeneratedMessageLite<*,*>.toBase64(flags: Int = Base64.NO_WRAP): String =
    Base64.encodeToString(toByteArray(), flags)