package com.reeman.points.process.callback

import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.model.custom.GenericPointsWithMap

interface RefreshPointDataCallback {

    fun onPointsLoadSuccess(pointList:List<GenericPoint>){}

    fun onPointsWithMapsLoadSuccess(pointsWithMapList:List<GenericPointsWithMap>){}

    fun onThrowable(throwable: Throwable){}
}