package com.reeman.points.model.custom

import com.reeman.points.model.request.PathPoint
import com.reeman.points.model.request.Point

class GenericPoint(
    val name: String,
    val type: String,
    val position: DoubleArray,
    val expand: String,
) {

    constructor(pathPoint: PathPoint) : this(
        name = pathPoint.name,
        type = pathPoint.type,
        position = pathPoint.getPosition(),
        expand = pathPoint.expand ?: ""
    )

    constructor(point: Point) : this(
        name = point.name,
        type = point.type,
        position = point.getPosition(),
        expand = ""
    )

    companion object {
        const val DELIVERY = "delivery"
        const val CHARGE = "charge"
        const val PRODUCT = "production"
        const val RECYCLE = "recycle"
        const val AVOID = "avoid"
        const val NORMAL = "normal"
        const val AGV_TAG = "agvtag"
        const val WAITING_ELEVATOR = "elewait"
        const val ENTER_ELEVATOR = "eleenter"
        const val INSIDE_ELEVATOR = "elein"
        const val LEAVE_ELEVATOR = "eleout"
        const val NODE_POINT = "nodepoint"
        const val WAY_POINT = "waypoint"
    }

    override fun toString(): String {
        return "GenericPoint(name='$name', type='$type', position=${position.contentToString()}, expand='$expand')"
    }


}