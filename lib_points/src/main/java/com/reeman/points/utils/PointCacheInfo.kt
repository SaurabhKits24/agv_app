package com.reeman.points.utils

import com.reeman.commons.state.RobotInfo
import com.reeman.commons.utils.PointUtils
import com.reeman.points.exception.ChargingPointCountException
import com.reeman.points.exception.CurrentMapNotInLegalMapListException
import com.reeman.points.exception.ElevatorPointNotLegalException
import com.reeman.points.exception.NoLegalMapException
import com.reeman.points.exception.RequiredMapNotFoundException
import com.reeman.points.exception.RequiredPointsNotFoundException
import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.model.custom.GenericPointsWithMap
import com.reeman.points.model.request.MapsWithFixedPoints
import com.reeman.points.model.request.MapsWithPoints
import com.reeman.points.model.request.Path
import com.reeman.points.model.request.PathPoint
import com.reeman.points.model.request.Point
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.sqrt

object PointCacheInfo {
    /**
     * 出品点:
     *      first:地图别名,非梯控模式下为 ""
     *      second:点位信息
     */
    lateinit var productionPoint: Pair<String, GenericPoint>

    /**
     * 充电桩:
     *      first:地图别名,非梯控模式下为 ""
     *      second:点位信息
     */
    lateinit var chargePoint: Pair<String, GenericPoint>

    /**
     * 候梯点
     */
    lateinit var waitingElevatorPoint: GenericPoint

    /**
     * 进梯点
     */
    lateinit var enterElevatorPoint: GenericPoint

    /**
     * 乘梯点
     */
    lateinit var takeElevatorPoint: GenericPoint

    /**
     * 出梯点
     */
    lateinit var leaveElevatorPoint: GenericPoint

    fun isChargePointInitialized() = ::chargePoint.isInitialized

    fun isEnterElevatorPointInitialized() = ::enterElevatorPoint.isInitialized

    /**
     * 当前地图所有点位信息
     */
    private var points = mutableListOf<GenericPoint>()
        set(value) {
            field.clear()
            field.addAll(value)
        }

    /**
     * 梯控模式下所有有效的的地图和点位
     */
    private var pointsWithMaps = mutableListOf<GenericPointsWithMap>()
        set(value) {
            field.clear()
            field.addAll(value)
        }

    /**
     * 固定路线模式下当前地图的路线信息
     */
    var paths = mutableListOf<Path>()

    /**
     * 当前位置所在路线的宽度
     */
    fun getPathWidth(first: String, second: String): Double {
        val path = paths.find { it.sourcePoint == first && it.destinationPoint == second }
        return path?.getPathWidth() ?: 1.0
    }

    /**
     * 通过地图别名查询地图名称
     */
    fun getMapByAlias(alias: String) = pointsWithMaps.find { it.alias == alias }?.name

    /**
     * 通过地图别名查询指定类型点位
     */
    fun getPointByAliasAndPointType(alias: String, type: String) =
        pointsWithMaps.find { it.alias == alias }!!.pointList.find { it.type == type }!!


    /**
     * 查询有效的门控点位
     */
    fun getDoorControlPoint(
        pathPoints: List<String>,
        doorName: String,
    ): Pair<GenericPoint, GenericPoint>? {
        val prefixAList = pathPoints.filter { it.startsWith(doorName) }

        if (prefixAList.size == 2) {
            val filter = points.filter { it.type == GenericPoint.NODE_POINT }
            val doorFrontPoint = filter.find { it.name == prefixAList[0] }
            val doorBackPoint = filter.find { it.name == prefixAList[1] }
            if (doorFrontPoint != null && doorBackPoint != null) {
                return Pair(doorFrontPoint, doorBackPoint)
            }
        }
        return null
    }

    /**
     * 查询指定类型的的点位
     */
    fun getPointListByType(types: List<String>): List<GenericPoint> {
        return points.filter { it.type in types }.sortedBy { it.name }
    }

    /**
     * 更新当前楼层的点位和路线
     */
    fun updateCurrentMapPointsByMap(updateEnterElevatorPoint: Boolean, map: String) {
        pointsWithMaps.find { it.name == map }?.let {
            points = it.pointList.toMutableList()
            updateCurrentMapElevatorPointsAndPaths(updateEnterElevatorPoint, it)
            Timber.d("已更新本地点位和路线至地图 $map")
        }
    }

    /**
     * 查询指定类型的的点位
     */
    fun getPointsWithMapListByType(types: List<String>): List<GenericPointsWithMap> {
        return pointsWithMaps.mapNotNull {
            val filterPoints =
                it.pointList.filter { point -> point.type in types }
                    .sortedBy { point -> point.name }
            if (filterPoints.isEmpty()) {
                null
            } else {
                GenericPointsWithMap(it.name, it.alias, filterPoints, it.pathList)
            }
        }.sortedBy { it.alias }
    }

    /**
     * 检查地图
     */
    fun checkPoints(
        points: List<Point>,
        types: List<String> = arrayListOf(GenericPoint.DELIVERY)
    ): List<GenericPoint> {
        return checkPointsGeneric(points, types, { it.type }) { GenericPoint(it) }
    }

    /**
     * 检查地图
     */
    fun checkFixedPoints(
        points: List<PathPoint>,
        types: List<String> = arrayListOf(GenericPoint.DELIVERY),
    ): List<GenericPoint> {
        return checkPointsGeneric(points, types, { it.type }) { GenericPoint(it) }
    }

    /**
     * 检查地图和点位
     */
    fun checkPointsWithMaps(
        pointsWithMaps: List<MapsWithPoints>,
        types: List<String> = arrayListOf(GenericPoint.DELIVERY),
        checkEnterElevatorPoint: Boolean
    ): List<GenericPointsWithMap> {
        return checkPointsWithMapsGeneric(
            checkEnterElevatorPoint,
            pointsWithMaps,
            types
        ) { GenericPointsWithMap(it) }
    }

    /**
     * 检查地图和点位
     */
    fun checkFixedPointsWithMaps(
        pointsWithMaps: List<MapsWithFixedPoints>,
        types: List<String> = arrayListOf(GenericPoint.DELIVERY),
        checkEnterElevatorPoint: Boolean
    ): List<GenericPointsWithMap> {
        return checkPointsWithMapsGeneric(
            checkEnterElevatorPoint,
            pointsWithMaps,
            types
        ) { GenericPointsWithMap(it) }
    }

    /**
     * 更新当前地图中的梯控相关点位和路线
     */
    private fun updateCurrentMapElevatorPointsAndPaths(
        updateEnterElevatorPoint: Boolean,
        genericPointsWithMap: GenericPointsWithMap
    ) {
        genericPointsWithMap.pointList.let {
            if (updateEnterElevatorPoint) {
                enterElevatorPoint =
                    it.find { genericPoint -> genericPoint.type == GenericPoint.ENTER_ELEVATOR }!!
            }
            waitingElevatorPoint =
                it.find { genericPoint -> genericPoint.type == GenericPoint.WAITING_ELEVATOR }!!
            takeElevatorPoint =
                it.find { genericPoint -> genericPoint.type == GenericPoint.INSIDE_ELEVATOR }!!
            leaveElevatorPoint =
                it.find { genericPoint -> genericPoint.type == GenericPoint.LEAVE_ELEVATOR }!!
        }
        points = genericPointsWithMap.pointList.toMutableList()
        if (genericPointsWithMap.pathList != null) {
            paths = genericPointsWithMap.pathList.toMutableList()
        }
    }

    fun clearAllCacheData() {
        points.clear()
        pointsWithMaps.clear()
    }


    private fun <T> checkPointsWithMapsGeneric(
        checkEnterElevatorPoint: Boolean,
        pointsWithMaps: List<T>,
        types: List<String> = arrayListOf(GenericPoint.DELIVERY),
        genericPointsWithMapsCreator: (T) -> GenericPointsWithMap
    ): List<GenericPointsWithMap> {
        val productionPointMap = RobotInfo.elevatorSetting.productionPointMap
        val chargingPileMap = RobotInfo.elevatorSetting.chargingPileMap
        if (productionPointMap == null || chargingPileMap == null) {
            throw RequiredMapNotFoundException(productionPointMap != null, chargingPileMap != null)
        }
        val elevatorPointNotLegalMapList = mutableListOf<Pair<String, IntArray>>()
        val filterPointsWithMapsList = pointsWithMaps.filter {
            val creator = genericPointsWithMapsCreator(it)
            val alias = creator.alias
            val pointList = creator.pointList
            val pathList = creator.pathList
            val isMapLegal = (alias.isNotBlank()
                    && alias.matches("-?\\d+".toRegex())
                    && pointList.size >= 4
                    && (pathList == null || pathList.isNotEmpty())
                    && pointList.any { point -> point.type in types })
            if (isMapLegal) {
                val waitingElevatorPointCount =
                    pointList.count { point -> point.type == GenericPoint.WAITING_ELEVATOR }
                val insideElevatorPointCount =
                    pointList.count { point -> point.type == GenericPoint.INSIDE_ELEVATOR }
                val leaveElevatorPointCount =
                    pointList.count { point -> point.type == GenericPoint.LEAVE_ELEVATOR }
                if (checkEnterElevatorPoint) {
                    val enterElevatorPointCount =
                        pointList.count { point -> point.type == GenericPoint.ENTER_ELEVATOR }
                    if (waitingElevatorPointCount != 1 || insideElevatorPointCount != 1 || leaveElevatorPointCount != 1 || enterElevatorPointCount != 1) {
                        val pointCountArray = IntArray(4)
                        pointCountArray[0] = waitingElevatorPointCount
                        pointCountArray[1] = enterElevatorPointCount
                        pointCountArray[2] = insideElevatorPointCount
                        pointCountArray[3] = leaveElevatorPointCount
                        elevatorPointNotLegalMapList.add(Pair(alias, pointCountArray))
                        return@filter false
                    }
                } else {
                    if (waitingElevatorPointCount != 1 || insideElevatorPointCount != 1 || leaveElevatorPointCount != 1) {
                        val pointCountArray = IntArray(4)
                        pointCountArray[0] = waitingElevatorPointCount
                        pointCountArray[1] = 1
                        pointCountArray[2] = insideElevatorPointCount
                        pointCountArray[3] = leaveElevatorPointCount
                        elevatorPointNotLegalMapList.add(Pair(alias, pointCountArray))
                        return@filter false
                    }
                }
            }
            isMapLegal
        }
        if (elevatorPointNotLegalMapList.isNotEmpty()) {
            throw ElevatorPointNotLegalException(elevatorPointNotLegalMapList)
        }
        if (filterPointsWithMapsList.isEmpty()) {
            throw NoLegalMapException()
        }
        val currentMapWithPoints =
            filterPointsWithMapsList.find { genericPointsWithMapsCreator(it).name == RobotInfo.currentMapEvent.map }
                ?: throw CurrentMapNotInLegalMapListException()
        val findProductionPointMap =
            filterPointsWithMapsList.find { genericPointsWithMapsCreator(it).alias == productionPointMap.first }
        val findChargePointMap =
            filterPointsWithMapsList.find { genericPointsWithMapsCreator(it).alias == chargingPileMap.first }
        if (findProductionPointMap == null || findChargePointMap == null) {
            throw RequiredPointsNotFoundException(
                findProductionPointMap != null,
                findChargePointMap != null
            )
        }
        val chargePointCount = genericPointsWithMapsCreator(findChargePointMap).let {
            it.pointList.count { point -> point.type == GenericPoint.CHARGE }
        }
        if (chargePointCount != 1) {
            val pathModel = if (pointsWithMaps[0] is MapsWithPoints) {
                ChargingPointCountException.AUTO_PATH_MODEL
            } else {
                ChargingPointCountException.FIXED_PATH_MODEL
            }
            throw ChargingPointCountException(
                chargePointCount,
                pathModel
            )
        }
        val findProductionPoint = genericPointsWithMapsCreator(findProductionPointMap).let {
            it.pointList.find { point -> point.type == GenericPoint.PRODUCT }
        }
        val findChargePoint = genericPointsWithMapsCreator(findChargePointMap).let {
            it.pointList.find { point -> point.type == GenericPoint.CHARGE }
        }
        if (findProductionPoint == null || findChargePoint == null) {
            throw RequiredPointsNotFoundException(
                findProductionPoint != null,
                findChargePoint != null
            )
        }
        updateCurrentMapElevatorPointsAndPaths(
            checkEnterElevatorPoint,
            genericPointsWithMapsCreator(currentMapWithPoints)
        )
        this.productionPoint = Pair(productionPointMap.first, findProductionPoint)
        this.chargePoint = Pair(chargingPileMap.first, findChargePoint)
        this.pointsWithMaps =
            filterPointsWithMapsList.map { genericPointsWithMapsCreator(it) }.toMutableList()
        return getPointsWithMapListByType(types)
    }

    private fun <T> checkPointsGeneric(
        points: List<T>,
        types: List<String> = arrayListOf(GenericPoint.DELIVERY),
        genericPointGetter: (T) -> String,
        genericPointCreator: (T) -> GenericPoint
    ): List<GenericPoint> {
        val chargePointCount = points.count { genericPointGetter(it) == GenericPoint.CHARGE }
        if (chargePointCount != 1) {
            throw ChargingPointCountException(
                chargePointCount,
                ChargingPointCountException.FIXED_PATH_MODEL
            )
        }
        val productionPoint = points.find { genericPointGetter(it) == GenericPoint.PRODUCT }?.let {
            productionPoint = Pair("", genericPointCreator(it))
            Timber.d("出品点 : $productionPoint")
        }
        val chargePoint = points.find { genericPointGetter(it) == GenericPoint.CHARGE }?.let {
            chargePoint = Pair("", genericPointCreator(it))
            Timber.d("充电桩 : $chargePoint")
        }
        if (productionPoint == null || chargePoint == null) {
            throw RequiredPointsNotFoundException(productionPoint != null, chargePoint != null)
        }
        this.points = points.map { genericPointCreator(it) }.toMutableList()
        return getPointListByType(types)
    }

    fun checkIsChargePointMarked(points: List<Point>) {
        checkChargePointGeneric(points) { GenericPoint(it) }
    }

    fun checkIsFixedChargePointMarked(points: List<PathPoint>) {
        checkChargePointGeneric(points) { GenericPoint(it) }
    }

    fun checkIsProductionPointMarked(points: List<Point>) {
        checkProductionPointGeneric(points) { GenericPoint(it) }
    }

    fun checkIsFixedProductionPointMarked(points: List<PathPoint>) {
        checkProductionPointGeneric(points) { GenericPoint(it) }
    }


    private fun <T> checkChargePointGeneric(
        points: List<T>,
        genericPointCreator: (T) -> GenericPoint
    ) {
        if (!points.any { genericPointCreator(it).type == GenericPoint.CHARGE }) {
            throw RequiredPointsNotFoundException(
                isProductionPointMarked = true,
                isChargePointMarked = false
            )
        }
    }

    private fun <T> checkProductionPointGeneric(
        points: List<T>,
        genericPointCreator: (T) -> GenericPoint
    ) {
        if (!points.any { genericPointCreator(it).type == GenericPoint.PRODUCT }) {
            throw RequiredPointsNotFoundException(
                isProductionPointMarked = false,
                isChargePointMarked = true
            )
        }
    }

    /**
     * 更新路线
     */
    fun updatePath(
        position: DoubleArray,
        pointQueue: MutableList<String>
    ) {
        if (pointQueue.isEmpty() || pointQueue.size <= 1) {
            if (pointQueue.size != 1) {
                Timber.v("无法更新路线 $pointQueue")
            }
            return
        }
        val take = pointQueue.take(3)

        val firstPathPoint = points.find { it.name == pointQueue[0] }
        val secondPathPoint = points.find { it.name == pointQueue[1] }

        if (firstPathPoint == null || secondPathPoint == null) {
            if (firstPathPoint == null) Timber.w("找不到路线点 ${take[0]}")
            if (secondPathPoint == null) Timber.w("找不到路线点 ${take[1]}")
            return
        }

        val positionStatus = getPerpendicularPositionStatus(
            firstPathPoint.position, secondPathPoint.position, position
        )

        when (positionStatus.positionStatus) {
            2 -> {
                pointQueue.removeFirst()
                Timber.w("机器走到第二个路线点前,移除第一个点")
            }

            1 -> {
                if (positionStatus.distanceToEndPosition < positionStatus.distanceToStartPosition) {
                    pointQueue.removeFirst()
                    Timber.w("机器在路线点${firstPathPoint.name} 和路线点${secondPathPoint.name}中间,移除第一个点")
                } else if (positionStatus.perpendicularLength > 0.5 && take.size == 3) {
                    val thirdPathPoint = points.find { it.name == pointQueue[2] }
                    if (thirdPathPoint != null) {
                        handleThirdPathPoint(
                            firstPathPoint.position,
                            secondPathPoint.position,
                            thirdPathPoint.position,
                            position,
                            pointQueue
                        )
                    }
                }
            }

            else -> {
                if (take.size == 3) {
                    val thirdPathPoint = points.find { it.name == pointQueue[2] }
                    if (thirdPathPoint != null) {
                        handleThirdPathPoint(
                            firstPathPoint.position,
                            secondPathPoint.position,
                            thirdPathPoint.position,
                            position,
                            pointQueue
                        )
                    }
                }
            }
        }

        return
    }

    private fun handleThirdPathPoint(
        firstPosition: DoubleArray,
        secondPosition: DoubleArray,
        thirdPathPosition: DoubleArray,
        position: DoubleArray,
        pointQueue: MutableList<String>
    ) {
        val calculateAngle =
            PointUtils.calculateAngle(firstPosition, secondPosition, thirdPathPosition)

        if (abs(calculateAngle) < 90) {
            Timber.w("夹角 $calculateAngle,考虑路线平滑")
            val perpendicularPositionStatus = getPerpendicularPositionStatus(
                secondPosition, thirdPathPosition, position
            )
            when (perpendicularPositionStatus.positionStatus) {
                2 -> {
                    pointQueue.removeFirst()
                    pointQueue.removeFirst()
                    Timber.w("机器走到第三个路线点前,移除前两个点")
                }

                1 -> {
                    if (perpendicularPositionStatus.distanceToEndPosition < perpendicularPositionStatus.distanceToStartPosition) {
                        pointQueue.removeFirst()
                        pointQueue.removeFirst()
                        Timber.w("机器在第二个路线点和第三个路线点中间,移除前两个点")
                    }
                }
            }
        }
    }

    /**
     * positionStatus: 垂足位置: 0:在start前;1:在start-end中间;2:在end后
     * perpendicularLength 垂线长度
     * distanceToStartPosition 垂足到start的距离
     * distanceToEndPosition 垂足到end的距离
     */
    data class PerpendicularInfo(
        val positionStatus: Int,
        val perpendicularLength: Double,
        val distanceToStartPosition: Double,
        val distanceToEndPosition: Double
    ) {
        override fun toString(): String {
            return "PerpendicularInfo(positionStatus=$positionStatus, perpendicularLength=$perpendicularLength, distanceToStartPosition=$distanceToStartPosition, distanceToEndPosition=$distanceToEndPosition)"
        }
    }

    /**
     * 计算当前位置与路线点前两个点的相对位置
     */
    private fun getPerpendicularPositionStatus(
        startPosition: DoubleArray,
        endPosition: DoubleArray,
        currentPosition: DoubleArray
    ): PerpendicularInfo {
        val startEnd =
            doubleArrayOf(endPosition[0] - startPosition[0], endPosition[1] - startPosition[1])
        val startCurrent = doubleArrayOf(
            currentPosition[0] - startPosition[0],
            currentPosition[1] - startPosition[1]
        )

        val dotProduct = startCurrent[0] * startEnd[0] + startCurrent[1] * startEnd[1]
        val startEndLengthSquare = startEnd[0] * startEnd[0] + startEnd[1] * startEnd[1]
        val projectionLength = dotProduct / startEndLengthSquare

        val perpendicularPoint = doubleArrayOf(
            startPosition[0] + startEnd[0] * projectionLength,
            startPosition[1] + startEnd[1] * projectionLength
        )

        val perpendicularLength = sqrt(
            (perpendicularPoint[0] - currentPosition[0]) * (perpendicularPoint[0] - currentPosition[0]) +
                    (perpendicularPoint[1] - currentPosition[1]) * (perpendicularPoint[1] - currentPosition[1])
        )

        val distanceToStart = sqrt(
            (currentPosition[0] - startPosition[0]) * (currentPosition[0] - startPosition[0]) +
                    (currentPosition[1] - startPosition[1]) * (currentPosition[1] - startPosition[1])
        )
        val distanceToEnd = sqrt(
            (currentPosition[0] - endPosition[0]) * (currentPosition[0] - endPosition[0]) +
                    (currentPosition[1] - endPosition[1]) * (currentPosition[1] - endPosition[1])
        )

        val positionStatus = when {
            projectionLength < 0 -> 0
            projectionLength > 1 -> 2
            else -> 1
        }

        return PerpendicularInfo(
            positionStatus,
            perpendicularLength,
            distanceToStart,
            distanceToEnd
        )
    }


}