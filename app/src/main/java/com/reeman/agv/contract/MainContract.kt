package com.reeman.agv.contract

import android.app.Activity
import android.content.Context
import com.reeman.agv.presenter.IPresenter
import com.reeman.agv.view.IView
import com.reeman.dao.repository.entities.RouteWithPoints
import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.model.custom.GenericPointsWithMap

interface MainContract {
    interface Presenter : IPresenter {
        /**
         * 刷新出品点
         * @param context
         */
        fun refreshProductModePoint(context: Context)

        /**
         * 刷新充电点
         * @param context
         */
        fun refreshChargeModePoint(context: Context)

        /**
         * 刷新二维码模式点位
         * @param context
         */
        fun refreshQRCodeModePoints(context: Context)

        /**
         * 刷新路线模式点位
         * @param context
         */
        fun refreshRouteModePoints(context: Context)

        /**
         * 刷新普通模式点位
         * @param context
         */
        fun refreshNormalModePoints(context: Context)

        /**
         * 开始执行路线模式任务
         * @param activity
         * @param routeWithPoints
         */
        fun startRouteModeTask(activity: Activity, routeWithPoints: RouteWithPoints)

        /**
         * 开始执行二维码模式任务
         * @param activity
         * @param qrCodeModelPointList
         */
        fun startQRCodeModeTask(
            activity: Activity,
            qrCodeModelPointList: List<Pair<Pair<String,String>,Pair<String,String>>>
        )

        /**
         * 开始执行普通模式任务
         * @param activity
         * @param points
         */
        fun startNormalModeTask(activity: Activity, points: List<Pair<String,String>>)
        fun gotoProductPoint(activity: Activity, isAutoWork: Boolean)
        fun gotoChargePoint(activity: Activity, isAutoWork: Boolean)
    }

    interface View : IView {

        /**
         * 普通模式-当前地图点位加载完成
         * @param pointList
         */
        fun onNormalModePointsDataLoadSuccess(pointList: List<GenericPoint>)

        /**
         * 普通模式-所有地图点位加载成功
         * @param pointsWithMapList
         */
        fun onNormalModeMapsWithPointsDataLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>)

        /**
         * 路线模式-所有点位加载成功
         * @param pointList
         */
        fun onRouteModelDataLoadSuccess(pointList: List<GenericPoint>)

        /**
         * 二维码模式-所有点位加载成功
         * @param pointList
         */
        fun onQRCodeModelDataLoadSuccess(pointList: List<GenericPoint>)

        /**
         * 二维码模式-所有地图点位加载成功
         * @param pointsWithMapList
         */
        fun onQRCodeModelMapsWithPointsDataLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>)

        /**
         * 充电模式-点位加载成功
         */
        fun onChargeModelDataLoadSuccess()

        /**
         * 出品点模式-点位加载成功
         */
        fun onProductModelDataLoadSuccess()

        /**
         * 数据加载失败
         * @param errorTip
         */
        fun onDataLoadFailed(errorTip: String)

        /**
         * 数据加载失败
         * @param throwable
         */
        fun onDataLoadFailed(throwable: Throwable)
    }
}