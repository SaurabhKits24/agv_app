package com.reeman.points.process.impl

import android.annotation.SuppressLint
import com.reeman.commons.utils.SpManager
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

class QRCodePointsRefreshProcessingStrategy:PointRefreshProcessingStrategy {

    @SuppressLint("CheckResult")
    override fun refreshPointList(
        ipAddress: String,
        useLocalData: Boolean,
        checkEnterElevatorPoint:Boolean,
        pointTypes:List<String>,
        callback: RefreshPointDataCallback
    ) {
        PointCacheUtil.refreshPoints(ipAddress,useLocalData)
            .flatMap {
                val isROSData = it.first
                val pointsMap = it.second
                if (pointsMap.isEmpty() || !pointsMap.containsKey("waypoints")) {
                    Timber.d("数据为空")
                    throwNoPointException(isROSData)
                }
                if (isROSData) {
                    Timber.d("更新本地数据")
                    PointCacheUtil.savePoints(pointsMap)
                }
                val pointList = pointsMap["waypoints"]
                if (pointList.isNullOrEmpty()) {
                    Timber.d("数据为空")
                    throwNoPointException(isROSData)
                }
                val qrCodePointList = PointCacheInfo.checkPoints(pointList!!, arrayListOf(GenericPoint.AGV_TAG))
                if (qrCodePointList.isEmpty()){
                    Timber.w("找不到标签码类型的点位")
                    throw PointListEmptyException(if (isROSData)PointListEmptyException.ROS_NO_TARGET_TYPE_POINTS else PointListEmptyException.LOCAL_NO_TARGET_TYPE_POINTS)
                }
                val qrCodePointPair = PointCacheUtil.refreshQRCodePoints(ipAddress,useLocalData)
                val isQRCodePointsUseROSData = qrCodePointPair.first
                val qrCodePointListMap = qrCodePointPair.second
                if (!qrCodePointListMap.contains("agvTags")){
                    throwNoQRCodePointException(isQRCodePointsUseROSData)
                }
                val qrCodePoints = qrCodePointListMap["agvTags"]
                if (qrCodePoints.isNullOrEmpty()){
                    throwNoQRCodePointException(isQRCodePointsUseROSData)
                }
                val validQRCodePoints =
                    PointCacheUtil.getValidQRCodePoints(qrCodePointList, qrCodePoints!!)
                if (validQRCodePoints.isEmpty()) {
                    Timber.d("二维码点位无效")
                    throw PointListEmptyException(if (isQRCodePointsUseROSData)PointListEmptyException.ROS_NO_VALID_AGV_POINT else PointListEmptyException.LOCAL_NO_VALID_AGV_POINT)
                }
                if (isQRCodePointsUseROSData){
                    Timber.d("更新本地二维码数据")
                    PointCacheUtil.saveQRCodePoints(qrCodePointListMap)
                }
                Timber.d("点位信息: $validQRCodePoints")
                Observable.just(validQRCodePoints)
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ callback.onPointsLoadSuccess(it) }) { callback.onThrowable(it) }
    }

    private fun throwNoPointException(isROSData:Boolean){
        var code = PointListEmptyException.LOCAL_POINTS_EMPTY
        if (isROSData) {
            code = PointListEmptyException.ROS_POINTS_EMPTY
            SpManager.getInstance().edit().remove(PointCacheConstants.KEY_POINT_INFO).apply()
        }
        throw PointListEmptyException(code)
    }

    private fun throwNoQRCodePointException(isROSData: Boolean){
        var code = PointListEmptyException.LOCAL_QRCODE_POINTS_EMPTY
        if (isROSData){
            code = PointListEmptyException.ROS_QRCODE_POINTS_EMPTY
            SpManager.getInstance().edit().remove(PointCacheConstants.KEY_QRCODE_POINT_INFO).apply()
        }
        throw PointListEmptyException(code)
    }
}