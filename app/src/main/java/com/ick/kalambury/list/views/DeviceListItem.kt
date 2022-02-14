package com.ick.kalambury.list.views

import android.content.Context
import android.util.AttributeSet
import com.ick.kalambury.databinding.ListItemDeviceBinding
import com.ick.kalambury.list.model.EndpointData

class DeviceListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ListItem<EndpointData, ListItemDeviceBinding>(context, attrs, defStyleAttr, defStyleRes) {

    override val data: EndpointData
        get() = binding.data as EndpointData

}