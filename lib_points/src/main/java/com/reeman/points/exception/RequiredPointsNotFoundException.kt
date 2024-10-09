package com.reeman.points.exception

/**
 * 找不到必须的点位
 */
class RequiredPointsNotFoundException(
    val isProductionPointMarked: Boolean,
    val isChargePointMarked: Boolean
) : IllegalStateException() {
    override fun toString(): String {
        return "RequiredPointsNotFoundException(isProductionPointMarked=$isProductionPointMarked, isChargePointMarked=$isChargePointMarked)"
    }
}