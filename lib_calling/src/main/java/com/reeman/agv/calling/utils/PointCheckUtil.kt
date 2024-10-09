package com.reeman.agv.calling.utils

import com.reeman.agv.calling.model.QRCodeModeTaskModel
import com.reeman.agv.calling.model.TaskPointModel
import com.reeman.agv.calling.exception.NoFindPointException
import com.reeman.dao.repository.entities.PointsVO
import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.model.custom.GenericPointsWithMap

object PointCheckUtil {

    /**
     * 检测移动端传入的点位是否有效
     */
    fun filterNonExistentPoints(
        pointList: List<TaskPointModel>,
        allPointList: List<GenericPoint>
    ) {
        val pointMap = allPointList.associateBy { it.name }
        val notExistingTaskPoints =
            pointList.filterNot { pointMap.containsKey(it.point) }.map { it.point }
        if (notExistingTaskPoints.isNotEmpty()) {
            throw NoFindPointException(
                notExistingTaskPoints
            )
        }

    }

    /**
     * 检测移动端传入的点位是否有效
     */
    fun filterNonExistentPointsWithMap(
        taskPoints: List<TaskPointModel>,
        mapsWithPointsList: List<GenericPointsWithMap>
    ) {
        val mapsWithPointsMap = mapsWithPointsList.associateBy { it.alias }
        val notExistingPoints = taskPoints.filterNot {
            mapsWithPointsMap.containsKey(it.map) && mapsWithPointsMap[it.map]?.pointList?.associateBy { point->point.name }
                ?.containsKey(it.point) == true
        }.map { "${it.map} : ${it.point}" }
        if (notExistingPoints.isNotEmpty()) {
            throw NoFindPointException(
                notExistingPoints
            )
        }
    }

    /**
     * 检测移动端下发的路线中的点位是否有效
     */
    fun filterNonExistentPathPoints(
        pointsVOList: List<PointsVO>,
        genericPointList: List<GenericPoint>
    ) {
        val pointMap = genericPointList.associateBy { it.name }
        val notExistingTaskPoints =
            pointsVOList.filterNot { pointMap.containsKey(it.point) }.map { it.point }
        if (notExistingTaskPoints.isNotEmpty()) {
            throw NoFindPointException(
                notExistingTaskPoints
            )
        }
    }

    /**
     * 检测二维码模式点位是否有效
     */
    fun filterNonExistentQRCodePoints(
        qrCodeModeTaskModelPairList: List<Pair<QRCodeModeTaskModel, QRCodeModeTaskModel>>,
        genericPointList: List<GenericPoint>
    ) {
        val qrCodeModeTaskModelList =
            qrCodeModeTaskModelPairList.flatMap { listOf(it.first, it.second) }
        val pointMap = genericPointList.associateBy { it.name }
        val notExistingQRCodePoints =
            qrCodeModeTaskModelList.filterNot { pointMap.containsKey(it.point) }.map { it.point }
        if (notExistingQRCodePoints.isNotEmpty()) {
            throw NoFindPointException(
                notExistingQRCodePoints
            )
        }
    }

    /**
     * 检测二维码模式点位是否有效
     */
    fun filterNonExistentQRCodePointsWithMaps(
        qrCodeModeTaskModelPairList: List<Pair<QRCodeModeTaskModel, QRCodeModeTaskModel>>,
        genericPointsWithMapList: List<GenericPointsWithMap>
    ) {
        val qrCodeModeTaskModelList =
            qrCodeModeTaskModelPairList.flatMap { listOf(it.first, it.second) }
        val pointsWithMapMap = genericPointsWithMapList.associateBy { it.alias }
        val notExistingQRCodePoints = qrCodeModeTaskModelList.filterNot {
            pointsWithMapMap.containsKey(it.map) && pointsWithMapMap[it.map]?.pointList?.associateBy { point -> point.name }
                ?.containsKey(it.point) == true
        }.map { "${it.map} : ${it.point}" }
        if (notExistingQRCodePoints.isNotEmpty()) {
            throw NoFindPointException(
                notExistingQRCodePoints
            )
        }
    }

}