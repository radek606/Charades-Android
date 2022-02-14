package com.ick.kalambury.drawing.adapters

import android.widget.BaseAdapter
import androidx.annotation.ArrayRes

abstract class IntegerBasedResourceAdapter(
        @ArrayRes val values: IntArray,
        var callback: (Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int {
        return values.size
    }

    override fun getItem(position: Int): Any {
        return values[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

}