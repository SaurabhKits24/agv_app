package com.reeman.points.process.impl

import android.annotation.SuppressLint
import com.reeman.points.exception.MapListEmptyException
import com.reeman.points.process.PointRefreshProcessingStrategy
import com.reeman.points.process.callback.RefreshPointDataCallback
import com.reeman.points.utils.PointCacheInfo
import com.reeman.points.utils.PointCacheUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class FixedDeliveryPointsWithMapsRefreshProcessingStrategy : PointRefreshProcessingStrategy {
    @SuppressLint("CheckResult")
    override fun refreshPointList(
        ipAddress: String,
        useLocalData: Boolean,
        checkEnterElevatorPoint:Boolean,
        pointTypes:List<String>,
        callback: RefreshPointDataCallback
    ) {
        PointCacheUtil.refreshFixedPointsWithMaps(ipAddress,useLocalData)
            .flatMap {
                val isROSData = it.first
                val mapsWithFixedPoints = it.second
                if (mapsWithFixedPoints.isEmpty()){
                    Timber.d("数据为空")
                    throwNoMapException(isROSData)
                }
                val checkFixedPointsWithMaps =
                    PointCacheInfo.checkFixedPointsWithMaps(mapsWithFixedPoints,pointTypes,checkEnterElevatorPoint)
                if (checkFixedPointsWithMaps.isEmpty()) {
                    Timber.d("没有有效的地图")
                    var code = MapListEmptyException.LOCAL_NO_VALID_MAP
                    if (isROSData) code = MapListEmptyException.ROS_NO_VALID_MAP
                    throw MapListEmptyException(code)
                }
                if (isROSData){
                    PointCacheUtil.checkAutoPathModelChargePoint(ipAddress,PointCacheInfo.getMapByAlias(PointCacheInfo.chargePoint.first))
                    Timber.d("更新本地固定路线点位和地图数据")
                    PointCacheUtil.saveFixedPointsWithMaps(mapsWithFixedPoints)
                }
                Timber.d("点位信息: $checkFixedPointsWithMaps")
                Observable.just(checkFixedPointsWithMaps)
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ callback.onPointsWithMapsLoadSuccess(it) }) { callback.onThrowable(it) }
    }

    private fun throwNoMapException(isROSData:Boolean){
        var code = MapListEmptyException.LOCAL_MAP_EMPTY
        if (isROSData) {
            code = MapListEmptyException.ROS_MAP_EMPTY
            PointCacheUtil.clearFixedPointsWithMapsLocalData()
        }
        throw MapListEmptyException(code)
    }


}