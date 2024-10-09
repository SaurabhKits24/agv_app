package com.reeman.points.exception

/**
 * 未选择必须的地图(充电桩和出品点所在地图
 */
class RequiredMapNotFoundException(
    val isProductionPointMapSelected: Boolean,
    val isChargePointMapSelected: Boolean,
) : IllegalStateException() {

    override fun toString(): String {
        return "RequiredMapNotFoundException(isProductionPointMapSelected=$isProductionPointMapSelected, isChargePointMapSelected=$isChargePointMapSelected)"
    }
}