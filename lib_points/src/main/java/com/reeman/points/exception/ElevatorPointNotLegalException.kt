package com.reeman.points.exception

/**
 * first:map alias
 * second: index:
 *              0:waiting elevator point count
 *              1:enter elevator point count
 *              2:take elevator point count
 *              3:leave elevator point count
 */
class ElevatorPointNotLegalException(val mapList:MutableList<Pair<String,IntArray>>):IllegalStateException(){

    override fun toString(): String {
        return "ElevatorPointNotFoundException(map=$mapList)"
    }
}