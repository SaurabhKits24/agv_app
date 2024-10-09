package com.reeman.points.exception

/**
 * 点位数据为空
 */
class PointListEmptyException(val code:Int) : IllegalStateException() {

    companion object {
        const val ROS_POINTS_EMPTY = 1
        const val LOCAL_POINTS_EMPTY = 2
        const val ROS_QRCODE_POINTS_EMPTY = 3
        const val LOCAL_QRCODE_POINTS_EMPTY = 4
        const val ROS_NO_TARGET_TYPE_POINTS = 5
        const val LOCAL_NO_TARGET_TYPE_POINTS = 6
        const val ROS_NO_VALID_AGV_POINT = 7
        const val LOCAL_NO_VALID_AGV_POINT = 8
    }

    override fun toString(): String {
        return "PointListEmptyException(code=$code)"
    }


}