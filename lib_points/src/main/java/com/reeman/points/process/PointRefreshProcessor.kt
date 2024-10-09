package com.reeman.points.process

import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.process.callback.RefreshPointDataCallback

class PointRefreshProcessor(
    private val pointRefreshProcessingStrategy: PointRefreshProcessingStrategy,
    private val refreshPointDataCallback: RefreshPointDataCallback
) {

    fun process(ip: String = "127.0.0.1", useLocalData: Boolean = true, checkEnterElevatorPoint:Boolean = false,pointTypes: List<String> = arrayListOf(GenericPoint.DELIVERY)) {
        pointRefreshProcessingStrategy.refreshPointList(
            ip,
            useLocalData,
            checkEnterElevatorPoint,
            pointTypes,
            refreshPointDataCallback
        )
    }
}