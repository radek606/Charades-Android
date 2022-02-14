package com.ick.kalambury.drawing.adapters

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ArrayRes
import com.ick.kalambury.R
import com.ick.kalambury.util.inflate
import com.ick.kalambury.util.toPx

class LineWeightAdapter(@ArrayRes values: IntArray, callback: (Int) -> Unit) :
        IntegerBasedResourceAdapter(values, callback) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: parent.inflate(R.layout.item_line)
        view.setOnClickListener { callback.invoke(values[position]) }

        val imageView = view.findViewById<ImageView>(R.id.line_view)
        imageView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                values[position].toPx.toInt(), Gravity.CENTER)

        return view
    }
}