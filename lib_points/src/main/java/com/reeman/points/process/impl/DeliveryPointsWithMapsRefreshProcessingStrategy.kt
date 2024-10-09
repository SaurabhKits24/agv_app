package com.reeman.points.process.impl

import android.annotation.SuppressLint
import com.reeman.commons.utils.SpManager
import com.reeman.points.exception.MapListEmptyException
import com.reeman.points.process.PointRefreshProcessingStrategy
import com.reeman.points.process.callback.RefreshPointDataCallback
import com.reeman.points.utils.PointCacheConstants
import com.reeman.points.utils.PointCacheInfo
import com.reeman.points.utils.PointCacheUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class DeliveryPointsWithMapsRefreshProcessingStrategy : PointRefreshProcessingStrategy {

    @SuppressLint("CheckResult")
    override fun refreshPointList(
        ipAddress: String,
        useLocalData: Boolean,
        checkEnterElevatorPoint:Boolean,
        pointTypes:List<String>,
        callback: RefreshPointDataCallback
    ) {
        PointCacheUtil.refreshPointsWithMaps(ipAddress, useLocalData)
            .flatMap {
                val isROSData = it.first
                val pointsWithMapsList = it.second
                if (pointsWithMapsList.isEmpty()) {
                    Timber.d("地图数据为空")
                    throwNoMapException(isROSData)
                }
                val pointsWithMaps =
                    PointCacheInfo.checkPointsWithMaps(pointsWithMapsList,pointTypes,checkEnterElevatorPoint)
                if (pointsWithMaps.isEmpty()) {
                    Timber.d("没有有效的地图")
                    var code = MapListEmptyException.LOCAL_NO_VALID_MAP
                    if (isROSData) code = MapListEmptyException.ROS_NO_VALID_MAP
                    throw MapListEmptyException(code)
                }
                if (isROSData) {
                    Timber.d("更新本地点位和地图数据")
                    PointCacheUtil.savePointsWithMaps(pointsWithMapsList)
                }
                Timber.d("点位信息: $pointsWithMaps")
                Observable.just(pointsWithMaps)
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ callback.onPointsWithMapsLoadSuccess(it) }) { callback.onThrowable(it) }
    }

    private fun throwNoMapException(isROSData:Boolean){
        var code = MapListEmptyException.LOCAL_MAP_EMPTY
        if (isROSData) {
            code = MapListEmptyException.ROS_MAP_EMPTY
            SpManager.getInstance().edit().remove(PointCacheConstants.KEY_POINT_WITH_MAP_INFO).apply()
        }
        throw MapListEmptyException(code)
    }
}