package com.reeman.points.exception

/**
 * 路线为空
 */
class PathListEmptyException(val code:Int):IllegalStateException() {

    companion object{
        const val ROS_PATH_EMPTY = 1
        const val LOCAL_PATH_EMPTY = 2
    }

    override fun toString(): String {
        return "PathListEmptyException(code=$code)"
    }


}