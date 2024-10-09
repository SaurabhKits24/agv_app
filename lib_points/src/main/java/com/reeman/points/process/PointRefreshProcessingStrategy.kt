package com.reeman.points.process

import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.process.callback.RefreshPointDataCallback

interface PointRefreshProcessingStrategy {
    /**
     * @param ipAddress ROS ip
     * @param useLocalData 是否直接使用本地数据
     * @param pointTypes 返回的点位类型
     * @param callback 结果回调
     */
    fun refreshPointList(
        ipAddress: String,
        useLocalData: Boolean,
        checkEnterElevatorPoint:Boolean,
        pointTypes: List<String> = arrayListOf(GenericPoint.DELIVERY),
        callback: RefreshPointDataCallback
    )
}