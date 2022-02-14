package com.ick.kalambury.list.model

import com.ick.kalambury.entities.EndpointData as EndpointDataProto

class EndpointData constructor(
    override val id: String,
    override val text: CharSequence,
    val minVersionName: String,
    val minVersionCode: Int,
    override val selected: Boolean = false,
) : ListableData, Connectable() {

    constructor(proto: EndpointDataProto) : this(
        proto.id,
        proto.name,
        proto.minVersionName,
        proto.minVersionCode,
        false
    )

}