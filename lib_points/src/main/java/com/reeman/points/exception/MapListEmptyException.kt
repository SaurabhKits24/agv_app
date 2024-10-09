package com.reeman.points.exception

/**
 * 地图数据为空
 */
class MapListEmptyException(val code:Int):IllegalStateException() {

    companion object{
        const val ROS_MAP_EMPTY = 1
        const val LOCAL_MAP_EMPTY = 2
        const val ROS_NO_VALID_MAP = 3
        const val LOCAL_NO_VALID_MAP = 4
    }
}