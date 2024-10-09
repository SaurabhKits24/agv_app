package com.reeman.commons.utils

import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

object PointUtils {

    fun calculateRadian(startPoint: DoubleArray, endPoint: DoubleArray): Double {
        return calculateRadian(startPoint[0], startPoint[1], endPoint[0], endPoint[1])
    }

    /**
     * 计算点A到B的倾斜角
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    fun calculateRadian(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val deltaX = x2 - x1
        val deltaY = y2 - y1
        var radian = atan2(deltaY, deltaX)
        radian = (radian * 100.0).roundToInt() / 100.0
        return radian
    }

    /**
     * 计算两点间的距离
     */
    @JvmStatic
    fun calculateDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val deltaX = x2 - x1
        val deltaY = y2 - y1
        return BigDecimal(sqrt(deltaX * deltaX + deltaY * deltaY))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }

    @JvmStatic
    fun calculateDistance(positionA: DoubleArray, positionB: DoubleArray): Double {
        val deltaX = positionB[0] - positionA[0]
        val deltaY = positionB[1] - positionA[1]
        return BigDecimal(sqrt(deltaX * deltaX + deltaY * deltaY))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }


    /**
     * 计算AB和CB的夹角
     */
    fun calculateAngle(pointA: DoubleArray, pointB: DoubleArray, pointC: DoubleArray): Double {
        val vectorABX = pointB[0] - pointA[0]
        val vectorABY = pointB[1] - pointA[1]

        val vectorCBX = pointB[0] - pointC[0]
        val vectorCBY = pointB[1] - pointC[1]

        val dotProduct = vectorABX * vectorCBX + vectorABY * vectorCBY

        val magnitudeAB = sqrt(vectorABX * vectorABX + vectorABY * vectorABY)
        val magnitudeCB = sqrt(vectorCBX * vectorCBX + vectorCBY * vectorCBY)

        var cosTheta = dotProduct / (magnitudeAB * magnitudeCB)
        cosTheta = (-1.0).coerceAtLeast(1.0.coerceAtMost(cosTheta))
        val angleInRadians = acos(cosTheta)

        return BigDecimal(Math.toDegrees(angleInRadians))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }

    /**
     * 角度转弧度
     */
    fun degreesToRadians(degrees: Double): Double {
        return Math.toRadians(degrees)
    }


    /**
     * 定位是否在指定误差范围内
     */
    fun isPositionError(
        positionA: DoubleArray,
        positionB: DoubleArray,
        maxDistance: Double,
        maxRadian: Double
    ): Boolean {
        val distance = calculateDistance(
            positionA,
            positionB
        )
        val radian = abs(positionA[2] - positionB[2])

        return if (distance > maxDistance || abs(positionA[2] - positionB[2]) > maxRadian) {
            Timber.w("定位异常,distance : $distance , radian : $radian")
            true
        } else {
            false
        }
    }

    fun isPositionCharged(positionLast: DoubleArray?, positionNew: DoubleArray): Boolean {
        if (positionLast != null) {
            if (positionLast.size == 3 && positionNew.size == 3) {
                for ((index, d) in positionNew.withIndex()) {
                    val min = if (index == 2) 0.1 else 0.05
                    if (abs(positionLast[index] - d) > min) {
                        return true
                    }
                }
            }
            return false
        }
        return true
    }
}