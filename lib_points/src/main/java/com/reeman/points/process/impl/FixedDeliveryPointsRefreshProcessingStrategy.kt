package com.reeman.points.process.impl

import android.annotation.SuppressLint
import com.reeman.commons.utils.SpManager
import com.reeman.points.exception.PathListEmptyException
import com.reeman.points.exception.PointListEmptyException
import com.reeman.points.process.PointRefreshProcessingStrategy
import com.reeman.points.process.callback.RefreshPointDataCallback
import com.reeman.points.utils.PointCacheConstants
import com.reeman.points.utils.PointCacheInfo
import com.reeman.points.utils.PointCacheUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class FixedDeliveryPointsRefreshProcessingStrategy : PointRefreshProcessingStrategy {
    @SuppressLint("CheckResult")
    override fun refreshPointList(
        ipAddress: String,
        useLocalData: Boolean,
        checkEnterElevatorPoint:Boolean,
        pointTypes: List<String>,
        callback: RefreshPointDataCallback
    ) {
        PointCacheUtil.refreshFixedPoints(ipAddress, useLocalData)
            .flatMap {
                val isROSData = it.first
                val pathModelPoint = it.second
                val pathPointList = pathModelPoint.point
                val pathList = pathModelPoint.path
                if (pathPointList.isNullOrEmpty()) {
                    Timber.d("数据为空")
                    throwNoPointException(isROSData)
                }
                if (pathList.isNullOrEmpty()) {
                    Timber.d("路线为空")
                    var code = PathListEmptyException.LOCAL_PATH_EMPTY
                    if (isROSData) {
                        code = PathListEmptyException.ROS_PATH_EMPTY
                        SpManager.getInstance().edit()
                            .remove(PointCacheConstants.KEY_FIX_POINT_INFO).apply()
                    }
                    throw PathListEmptyException(code)
                }
                val fixedPoints = PointCacheInfo.checkFixedPoints(pathPointList!!, pointTypes)
                if (fixedPoints.isEmpty()) {
                    var code = PointListEmptyException.LOCAL_NO_TARGET_TYPE_POINTS
                    if (isROSData) code = PointListEmptyException.ROS_NO_TARGET_TYPE_POINTS
                    throw PointListEmptyException(code)
                }
                if (isROSData) {
                    PointCacheUtil.checkAutoPathModelChargePoint(ipAddress)
                    Timber.d("更新本地数据")
                    PointCacheUtil.saveFixedPoints(pathModelPoint)
                }
                PointCacheInfo.paths = pathList.toMutableList()
                Timber.d("点位信息: $fixedPoints")
                Observable.just(fixedPoints)
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ callback.onPointsLoadSuccess(it) }) { callback.onThrowable(it) }
    }

    private fun throwNoPointException(isROSData: Boolean) {
        var code = PointListEmptyException.LOCAL_POINTS_EMPTY
        if (isROSData) {
            code = PointListEmptyException.ROS_POINTS_EMPTY
            SpManager.getInstance().edit().remove(PointCacheConstants.KEY_FIX_POINT_INFO).apply()
        }
        throw PointListEmptyException(code)
    }
}