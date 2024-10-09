package com.reeman.points.utils

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.reeman.commons.utils.PointUtils
import com.reeman.commons.utils.SpManager
import com.reeman.points.exception.ChargingPointCountException
import com.reeman.points.exception.ChargingPointPositionErrorException
import com.reeman.points.exception.ChargingPointNotMarkedException
import com.reeman.points.exception.MapListEmptyException
import com.reeman.points.exception.PointListEmptyException
import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.model.custom.GenericPointsWithMap
import com.reeman.points.model.request.MapsWithFixedPoints
import com.reeman.points.model.request.MapsWithPoints
import com.reeman.points.model.request.MapsWithQRCodePoints
import com.reeman.points.model.request.PathModelPoint
import com.reeman.points.model.request.Point
import com.reeman.points.model.request.QRCodePoint
import com.reeman.points.request.ApiClient
import com.reeman.points.request.url.Url
import io.reactivex.rxjava3.core.Observable
import retrofit2.HttpException
import timber.log.Timber

object PointCacheUtil {

    val gson = Gson()

    @Throws
    fun checkAutoPathModelChargePoint(ip: String, map: String? = null) {
        val response = if (map.isNullOrBlank()) {
            fetchPoints(ip)
        } else {
            fetchPointsByMap(ip, map)
        }
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        val body = response.body()
        if (body == null || !body.containsKey("waypoints")) {
            throw ChargingPointNotMarkedException()
        }
        val pointList = body["waypoints"]!!
        val chargePointCount = pointList.count { it.type == GenericPoint.CHARGE }
        if (chargePointCount == 0){
            throw ChargingPointNotMarkedException()
        }
        if (chargePointCount > 1) {
            throw ChargingPointCountException(
                chargePointCount,
                ChargingPointCountException.AUTO_PATH_MODEL
            )
        }
        pointList.find { it.type == GenericPoint.CHARGE }?.let {
            val isPositionError = PointUtils.isPositionError(
                it.getPosition(),
                PointCacheInfo.chargePoint.second.position,
                0.7,
                1.04
            )
            if (isPositionError) {
                throw ChargingPointPositionErrorException()
            }
        }
    }

    /**
     * 拉取点位
     */
    fun fetchPoints(ip: String) =
        ApiClient.getApiService().fetchPointsSync(Url.getPoints(ip)).execute()

    /**
     * 拉取指定地图的点位
     */
    fun fetchPointsByMap(ip: String, map: String) =
        ApiClient.getApiService().fetchPointsByMapSync(Url.getPointsByMap(ip), map).execute()

    /**
     * 保存标签码和地图
     */
    fun saveQRCodePointsWithMaps(qrCodePointsWithMapList: List<MapsWithQRCodePoints>) {
        SpManager.getInstance().edit().putString(
            PointCacheConstants.KEY_QRCODE_POINT_WITH_MAP_INFO,
            gson.toJson(
                qrCodePointsWithMapList,
                object : TypeToken<List<MapsWithQRCodePoints>>() {}.type
            )
        ).apply()
    }

    /**
     * 获取有效的标签码类型点位
     */
    fun getValidQRCodePointsWithMaps(
        pointsWithMapList: List<GenericPointsWithMap>,
        qrCodePointsWithMapList: List<MapsWithQRCodePoints>
    ): List<GenericPointsWithMap> {
        return pointsWithMapList.mapNotNull { pointsWithMap ->
            val nameList =
                qrCodePointsWithMapList.find { it.name == pointsWithMap.name }?.qrCodePoints?.map { it.name }
            if (nameList.isNullOrEmpty()) {
                null
            } else {
                val pointList =
                    pointsWithMap.pointList.filter { it.name in nameList }.sortedBy { it.name }
                if (pointList.isEmpty()) {
                    null
                } else {
                    GenericPointsWithMap(
                        pointsWithMap.name,
                        pointsWithMap.alias,
                        pointList,
                        pointsWithMap.pathList
                    )
                }
            }
        }
    }

    /**
     * 刷新标签码和地图
     */
    fun refreshQRCodePointsWithMaps(
        ip: String,
        useLocalData: Boolean
    ): Pair<Boolean, List<MapsWithQRCodePoints>> {
        fun getLocalQRCodePointsWithMaps(): Pair<Boolean, List<MapsWithQRCodePoints>> {
            val qrCodePointsWithMapsInfoStr = SpManager.getInstance()
                .getString(PointCacheConstants.KEY_QRCODE_POINT_WITH_MAP_INFO, null)
            if (qrCodePointsWithMapsInfoStr.isNullOrBlank()) {
                throw PointListEmptyException(PointListEmptyException.LOCAL_QRCODE_POINTS_EMPTY)
            }
            try {
                val mapsWithQRCodePoints =
                    gson.fromJson<List<MapsWithQRCodePoints>>(
                        qrCodePointsWithMapsInfoStr,
                        object : TypeToken<List<MapsWithQRCodePoints>>() {}.type
                    )
                return Pair(false, mapsWithQRCodePoints)
            } catch (e: JsonParseException) {
                Timber.w(e, "解析本地二维码点位和地图信息失败")
                throw e
            }
        }

        fun getROSQRCodePointsWithMaps(ip: String): Pair<Boolean, List<MapsWithQRCodePoints>> {
            val response = ApiClient.getApiService()
                .fetchQRCodePointsWithMapsSync(Url.getQRCodePointsWithMaps(ip)).execute()
            if (response.isSuccessful) {
                val body = response.body()
                if (!body.isNullOrEmpty()) {
                    return Pair(true, body)
                }
            }
            return getLocalQRCodePointsWithMaps()
        }

        if (useLocalData || ip == "127.0.0.1") {
            return getLocalQRCodePointsWithMaps()
        }
        return getROSQRCodePointsWithMaps(ip)

    }

    /**
     * 保存标签码信息
     */
    fun saveQRCodePoints(qrcodePointListMap: Map<String, List<QRCodePoint>>) =
        SpManager.getInstance().edit().putString(
            PointCacheConstants.KEY_QRCODE_POINT_INFO,
            gson.toJson(
                qrcodePointListMap,
                object : TypeToken<Map<String, List<QRCodePoint>>>() {}.type
            )
        ).apply()

    /**
     * 获取有效的标签码类型点位
     */
    fun getValidQRCodePoints(
        pointList: List<GenericPoint>,
        qrCodePointList: List<QRCodePoint>
    ): List<GenericPoint> {
        val qrCodePointNameList = qrCodePointList.map { it.name }
        return pointList.filter { it.name in qrCodePointNameList }
    }

    /**
     * 刷新标签码
     */
    fun refreshQRCodePoints(
        ip: String,
        useLocalData: Boolean
    ): Pair<Boolean, Map<String, List<QRCodePoint>>> {

        fun getLocalQRCodePoints(): Pair<Boolean, Map<String, List<QRCodePoint>>> {
            val qrCodePointsInfoStr =
                SpManager.getInstance().getString(PointCacheConstants.KEY_QRCODE_POINT_INFO, null)
            if (qrCodePointsInfoStr.isNullOrBlank()) {
                throw PointListEmptyException(PointListEmptyException.LOCAL_QRCODE_POINTS_EMPTY)
            }
            Timber.d("本地标签码点位信息 : $qrCodePointsInfoStr")
            try {
                val qrCodePointsMap =
                    gson.fromJson<Map<String, List<QRCodePoint>>>(
                        qrCodePointsInfoStr,
                        object : TypeToken<Map<String, List<QRCodePoint>>>() {}.type
                    )
                return Pair(false, qrCodePointsMap)
            } catch (e: JsonParseException) {
                Timber.w(e, "解析本地二维码点位信息失败")
                throw e
            }
        }

        fun getQRCodePoints(ip: String): Pair<Boolean, Map<String, List<QRCodePoint>>> {
            val response =
                ApiClient.getApiService().fetchQRCodePointsSync(Url.getQRCodePoints(ip)).execute()
            if (response.isSuccessful) {
                val body = response.body()
                if (!body.isNullOrEmpty()) {
                    return Pair(true, body)
                }
            }
            return getLocalQRCodePoints()
        }

        if (useLocalData || ip == "127.0.0.1") {
            return getLocalQRCodePoints()
        }
        return getQRCodePoints(ip)
    }

    /**
     * 清除固定路线和地图
     */
    fun clearFixedPointsWithMapsLocalData() {
        val fixedMapInfoStr =
            SpManager.getInstance().getString(PointCacheConstants.KEY_FIX_MAP_INFO, null)
        if (fixedMapInfoStr.isNullOrBlank()) return
        val mapList =
            gson.fromJson<List<String>>(fixedMapInfoStr, object : TypeToken<List<String>>() {}.type)
        if (mapList.isNullOrEmpty()) return
        val edit = SpManager.getInstance().edit()
        for (mapName in mapList) {
            edit.remove(PointCacheConstants.KEY_FIX_POINT_INFO_BY_MAP + mapName)
        }
        edit.remove(PointCacheConstants.KEY_FIX_MAP_INFO).apply()
    }

    /**
     * 保存固定路线和地图
     */
    fun saveFixedPointsWithMaps(fixedPointsWithMap: List<MapsWithFixedPoints>) {
        val filterFixedPointsWithMapsList = fixedPointsWithMap.filterNot {
            val alias = it.alias
            val pointList = it.path.point
            val pathList = it.path.path
            (alias.isEmpty()
                    || !alias.matches("-?\\d+".toRegex()))
                    || pointList.isNullOrEmpty()
                    || pathList.isNullOrEmpty()
                    || pointList.size < 4
                    || !pointList.any { point -> point.type == GenericPoint.WAITING_ELEVATOR }
                    || !pointList.any { point -> point.type == GenericPoint.INSIDE_ELEVATOR }
                    || !pointList.any { point -> point.type == GenericPoint.LEAVE_ELEVATOR }
        }
        val edit = SpManager.getInstance().edit()
        val mapList = mutableListOf<String>()
        for (mapsWithFixedPoints in filterFixedPointsWithMapsList) {
            mapList.add(mapsWithFixedPoints.name)
            edit.putString(
                PointCacheConstants.KEY_FIX_POINT_INFO_BY_MAP + mapsWithFixedPoints.name,
                gson.toJson(mapsWithFixedPoints)
            )
        }
        edit.putString(
            PointCacheConstants.KEY_FIX_MAP_INFO,
            gson.toJson(mapList, object : TypeToken<List<String>>() {}.type)
        ).apply()
    }

    /**
     * 刷新固定路线模式点位和地图
     */
    fun refreshFixedPointsWithMaps(
        ip: String,
        useLocalData: Boolean
    ): Observable<Pair<Boolean, List<MapsWithFixedPoints>>> {

        fun getLocalFixedPointsWithMaps(): Observable<Pair<Boolean, List<MapsWithFixedPoints>>> {
            return Observable.fromCallable {
                val fixedMapsInfoStr =
                    SpManager.getInstance().getString(PointCacheConstants.KEY_FIX_MAP_INFO, null)
                if (fixedMapsInfoStr.isNullOrBlank()) {
                    throw MapListEmptyException(MapListEmptyException.LOCAL_MAP_EMPTY)
                }
                Timber.d("本地固定路线地图信息 : $fixedMapsInfoStr")
                try {
                    val mapList = gson.fromJson<List<String>>(
                        fixedMapsInfoStr,
                        object : TypeToken<List<String>>() {}.type
                    )
                    if (mapList.isNullOrEmpty()) {
                        throw MapListEmptyException(MapListEmptyException.LOCAL_MAP_EMPTY)
                    }
                    Pair(false, mapList.mapNotNull {
                        val fixedPointsWithMapInfoStr = SpManager.getInstance()
                            .getString(PointCacheConstants.KEY_FIX_POINT_INFO_BY_MAP + it, null)
                        if (fixedPointsWithMapInfoStr.isNullOrBlank()) {
                            SpManager.getInstance().edit()
                                .remove(PointCacheConstants.KEY_FIX_POINT_INFO_BY_MAP + it).apply()
                            return@mapNotNull null
                        }
                        Timber.d("本地固定路线信息,地图: $it , 点位和路线 : $fixedMapsInfoStr")
                        try {
                            gson.fromJson(
                                fixedPointsWithMapInfoStr,
                                MapsWithFixedPoints::class.java
                            )
                        } catch (e: JsonParseException) {
                            SpManager.getInstance().edit()
                                .remove(PointCacheConstants.KEY_FIX_POINT_INFO_BY_MAP + it).apply()
                            Timber.w(e, "解析本地固定路线点位和地图信息失败")
                            null
                        }
                    })
                } catch (e: JsonParseException) {
                    Timber.w(e, "解析本地地图信息失败")
                    throw e
                }
            }
        }

        fun getROSFixedPointsWithMaps(ip: String) =
            ApiClient.getApiService().fetchFixedPointsWithMapsAsync(Url.getFixedPointsWithMaps(ip))
                .map { Pair(true, it) }
                .onErrorResumeNext {
                    Timber.w(it, "从ROS端拉取固定路线点位和地图失败")
                    getLocalFixedPointsWithMaps()
                }

        if (useLocalData || ip == "127.0.0.1") {
            return getLocalFixedPointsWithMaps()
        }
        return getROSFixedPointsWithMaps(ip)
    }

    /**
     * 保存固定路线信息
     */
    fun saveFixedPoints(pathModelPoint: PathModelPoint) =
        SpManager.getInstance().edit()
            .putString(PointCacheConstants.KEY_FIX_POINT_INFO, gson.toJson(pathModelPoint)).apply()

    /**
     * 刷新固定路线模式点位
     */
    fun refreshFixedPoints(
        ip: String,
        useLocalData: Boolean
    ): Observable<Pair<Boolean, PathModelPoint>> {

        fun getLocalFixedPoints(): Observable<Pair<Boolean, PathModelPoint>> {
            return Observable.fromCallable {
                val fixPointInfoStr =
                    SpManager.getInstance().getString(PointCacheConstants.KEY_FIX_POINT_INFO, null)
                if (fixPointInfoStr.isNullOrBlank()) {
                    throw PointListEmptyException(PointListEmptyException.LOCAL_POINTS_EMPTY)
                }
                Timber.d("本地固定路线点位信息 : $fixPointInfoStr")
                try {
                    val pathModelPoint =
                        gson.fromJson(fixPointInfoStr, PathModelPoint::class.java)
                    Pair(false, pathModelPoint)
                } catch (e: JsonParseException) {
                    Timber.w(e, "解析本地固定路线点位信息失败")
                    throw e
                }
            }
        }

        fun getROSFixedPoints(ip: String) =
            ApiClient.getApiService().fetchFixedPointsAsync(Url.getFixedPoints(ip))
                .map { Pair(true, it) }
                .onErrorResumeNext {
                    Timber.w(it, "从ROS端拉取固定路线点位失败")
                    getLocalFixedPoints()
                }

        if (useLocalData || ip == "127.0.0.1") {
            return getLocalFixedPoints()
        }
        return getROSFixedPoints(ip)
    }


    /**
     * 保存地图和点位
     */
    fun savePointsWithMaps(pointsWithMaps: List<MapsWithPoints>) {
        val filterPointsWithMapsList = pointsWithMaps.filterNot {
            val alias = it.alias
            val pointList = it.points
            (alias.isEmpty()
                    || !alias.matches("-?\\d+".toRegex()))
                    || pointList.isNullOrEmpty()
                    || pointList.size < 4
                    || !pointList.any { point -> point.type == GenericPoint.WAITING_ELEVATOR }
                    || !pointList.any { point -> point.type == GenericPoint.INSIDE_ELEVATOR }
                    || !pointList.any { point -> point.type == GenericPoint.LEAVE_ELEVATOR }
        }
        SpManager.getInstance().edit().putString(
            PointCacheConstants.KEY_POINT_WITH_MAP_INFO,
            gson.toJson(
                filterPointsWithMapsList,
                object : TypeToken<List<MapsWithPoints>>() {}.type
            )
        ).apply()
    }

    /**
     * 刷新地图和点位
     */
    fun refreshPointsWithMaps(
        ip: String,
        useLocalData: Boolean
    ): Observable<Pair<Boolean, List<MapsWithPoints>>> {

        fun getLocalPointsWithMaps(): Observable<Pair<Boolean, List<MapsWithPoints>>> {
            return Observable.fromCallable {
                val pointsWithMapsInfoStr =
                    SpManager.getInstance()
                        .getString(PointCacheConstants.KEY_POINT_WITH_MAP_INFO, null)
                if (pointsWithMapsInfoStr.isNullOrBlank()) {
                    throw PointListEmptyException(PointListEmptyException.LOCAL_POINTS_EMPTY)
                }
                try {
                    val pointsWithMapsList = gson.fromJson<List<MapsWithPoints>>(
                        pointsWithMapsInfoStr,
                        object : TypeToken<List<MapsWithPoints>>() {}.type
                    )
                    Pair(false, pointsWithMapsList)
                } catch (e: JsonParseException) {
                    Timber.w(e, "解析本地点位信息失败")
                    throw e
                }
            }
        }

        fun getROSPointsWithMaps(ip: String) =
            ApiClient.getApiService().fetchPointsWithMapsAsync(Url.getPointsWithMaps(ip))
                .map { Pair(true, it) }
                .onErrorResumeNext {
                    Timber.w(it, "从ROS端拉取点位和地图失败")
                    getLocalPointsWithMaps()
                }

        if (useLocalData || ip == "127.0.0.1") {
            return getLocalPointsWithMaps()
        }
        return getROSPointsWithMaps(ip)
    }

    /**
     * 保存点位信息
     */
    fun savePoints(pointListMap: Map<String, List<Point>>) =
        SpManager.getInstance().edit().putString(
            PointCacheConstants.KEY_POINT_INFO,
            gson.toJson(pointListMap, object : TypeToken<Map<String, List<Point>>>() {}.type)
        ).apply()

    /**
     * 刷新点位
     */
    fun refreshPoints(
        ip: String,
        useLocalData: Boolean
    ): Observable<Pair<Boolean, Map<String, List<Point>>>> {

        fun getLocalPoints(): Observable<Pair<Boolean, Map<String, List<Point>>>> {
            return Observable.fromCallable {
                val pointInfoStr =
                    SpManager.getInstance().getString(PointCacheConstants.KEY_POINT_INFO, "")
                if (pointInfoStr.isNullOrBlank()) {
                    throw PointListEmptyException(PointListEmptyException.LOCAL_POINTS_EMPTY)
                }
                Timber.d("本地点位信息: $pointInfoStr")
                try {
                    val pointInfo = gson.fromJson<Map<String, List<Point>>>(
                        pointInfoStr,
                        object : TypeToken<Map<String, List<Point>>>() {}.type
                    )
                    Pair(false, pointInfo)
                } catch (e: JsonParseException) {
                    Timber.w(e, "解析本地点位信息失败")
                    throw e
                }
            }
        }

        fun getROSPoints(ip: String) =
            ApiClient.getApiService().fetchPointsAsync(Url.getPoints(ip))
                .map { Pair(true, it) }
                .onErrorResumeNext {
                    Timber.w(it, "从ROS端拉取点位失败")
                    getLocalPoints()
                }

        if (useLocalData || ip == "127.0.0.1") {
            return getLocalPoints()
        }
        return getROSPoints(ip)
    }

    fun clearAllLocalData() {
        SpManager.getInstance().edit()
            .remove(PointCacheConstants.KEY_POINT_INFO)
            .remove(PointCacheConstants.KEY_POINT_WITH_MAP_INFO)
            .remove(PointCacheConstants.KEY_QRCODE_POINT_INFO)
            .remove(PointCacheConstants.KEY_QRCODE_POINT_WITH_MAP_INFO)
            .remove(PointCacheConstants.KEY_FIX_POINT_INFO).apply()
        clearFixedPointsWithMapsLocalData()
    }
}