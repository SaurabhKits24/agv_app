package com.reeman.points.exception

/**
 * 充电桩数量不对
 */
class ChargingPointCountException(val count:Int, val pathModel:Int):IllegalStateException(){

    companion object{
        const val AUTO_PATH_MODEL = 0
        const val FIXED_PATH_MODEL = 1
    }
}

