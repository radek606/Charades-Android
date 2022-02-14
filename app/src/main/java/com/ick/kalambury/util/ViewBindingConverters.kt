package com.ick.kalambury.util

object ViewBindingConverters {

    @JvmStatic
    fun secondsToTime(seconds: Int): String {
        return String.format("%1$1d:%2$02d", seconds / 60, seconds % 60)
    }

}
