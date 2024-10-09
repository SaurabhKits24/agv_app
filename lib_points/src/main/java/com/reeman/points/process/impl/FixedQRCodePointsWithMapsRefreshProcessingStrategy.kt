package com.reeman.points.process.impl

import android.annotation.SuppressLint
import com.reeman.commons.utils.SpManager
import com.reeman.points.exception.MapListEmptyException
import com.reeman.points.exception.PointListEmptyException
import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.process.PointRefreshProcessingStrategy
import com.reeman.points.process.callback.RefreshPointDataCallback
import com.reeman.points.utils.PointCacheConstants
import com.reeman.points.utils.PointCacheInfo
import com.reeman.points.utils.PointCacheUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class FixedQRCodePointsWithMapsRefreshProcessingStrategy : PointRefreshProcessingStrategy {

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
                    PointCacheInfo.checkFixedPointsWithMaps(mapsWithFixedPoints,arrayListOf(GenericPoint.AGV_TAG),checkEnterElevatorPoint)
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
                val qrCodePointsWithMapsPair =
                    PointCacheUtil.refreshQRCodePointsWithMaps(ipAddress, useLocalData)
                val isQRCodePointsUseROSData = qrCodePointsWithMapsPair.first
                val mapsWithQRCodePointsList = qrCodePointsWithMapsPair.second
                if (mapsWithQRCodePointsList.isEmpty()) {
                    Timber.d("二维码数据为空")
                    throwNoQRCodePointException(isQRCodePointsUseROSData)
                }
                val validQRCodePointsWithMaps = PointCacheUtil.getValidQRCodePointsWithMaps(
                    checkFixedPointsWithMaps,
                    mapsWithQRCodePointsList
                )
                if (validQRCodePointsWithMaps.isEmpty()) {
                    Timber.d("没有有效的二维码数据")
                    var code = PointListEmptyException.LOCAL_NO_VALID_AGV_POINT
                    if (isQRCodePointsUseROSData)code = PointListEmptyException.ROS_NO_VALID_AGV_POINT
                    throw PointListEmptyException(code)
                }
                if (isQRCodePointsUseROSData){
                    Timber.d("更新本地二维码和地图数据")
                    PointCacheUtil.saveQRCodePointsWithMaps(mapsWithQRCodePointsList)
                }
                Timber.d("点位信息: $validQRCodePointsWithMaps")
                Observable.just(validQRCodePointsWithMaps)
            }
            .subscribeOn(Schedulers.io())
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

    private fun throwNoQRCodePointException(isROSData: Boolean) {
        var code = PointListEmptyException.LOCAL_QRCODE_POINTS_EMPTY
        if (isROSData) {
            code = PointListEmptyException.ROS_QRCODE_POINTS_EMPTY
            SpManager.getInstance().edit().remove(PointCacheConstants.KEY_QRCODE_POINT_WITH_MAP_INFO).apply()
        }
        throw PointListEmptyException(code)
    }
}