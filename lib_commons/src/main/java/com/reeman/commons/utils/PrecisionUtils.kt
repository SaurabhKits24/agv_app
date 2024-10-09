package com.reeman.commons.utils

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 精度控制
 */
object PrecisionUtils {

    private const val DEFAULT_SCALE = 2

    fun add(a: Double, b: Double): Double {
        return BigDecimal(a.toString())
            .add(BigDecimal(b.toString()))
            .setScale(DEFAULT_SCALE, RoundingMode.HALF_UP)
            .toDouble()
    }

    fun add(a: Float, b: Float): Float {
        return BigDecimal(a.toString())
            .add(BigDecimal(b.toString()))
            .setScale(DEFAULT_SCALE, RoundingMode.HALF_UP)
            .toFloat()
    }

    fun subtract(a: Double, b: Double): Double {
        return BigDecimal(a.toString())
            .subtract(BigDecimal(b.toString()))
            .setScale(DEFAULT_SCALE, RoundingMode.HALF_UP)
            .toDouble()
    }

    fun subtract(a: Float, b: Float): Float {
        return BigDecimal(a.toString())
            .subtract(BigDecimal(b.toString()))
            .setScale(DEFAULT_SCALE, RoundingMode.HALF_UP)
            .toFloat()
    }

    fun multiply(a: Double, b: Double): Double {
        return BigDecimal(a.toString())
            .multiply(BigDecimal(b.toString()))
            .setScale(DEFAULT_SCALE, RoundingMode.HALF_UP)
            .toDouble()
    }

    fun multiply(a: Float, b: Float): Float {
        return BigDecimal(a.toString())
            .multiply(BigDecimal(b.toString()))
            .setScale(DEFAULT_SCALE, RoundingMode.HALF_UP)
            .toFloat()
    }

    fun divide(a: Double, b: Double): Double {
        return BigDecimal(a.toString())
            .divide(BigDecimal(b.toString()), DEFAULT_SCALE, RoundingMode.HALF_UP)
            .toDouble()
    }

    fun divide(a: Float, b: Float): Float {
        return BigDecimal(a.toString())
            .divide(BigDecimal(b.toString()), DEFAULT_SCALE, RoundingMode.HALF_UP)
            .toFloat()
    }

    fun setScale(value: Double, scale: Int = DEFAULT_SCALE): Double {
        return BigDecimal(value.toString())
            .setScale(scale, RoundingMode.HALF_UP)
            .toDouble()
    }

    fun setScale(value: Float, scale: Int = DEFAULT_SCALE): Float {
        return BigDecimal(value.toString())
            .setScale(scale, RoundingMode.HALF_UP)
            .toFloat()
    }
}
