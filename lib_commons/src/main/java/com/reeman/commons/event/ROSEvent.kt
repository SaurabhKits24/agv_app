package com.reeman.commons.event

import com.reeman.commons.event.model.Room
import com.reeman.commons.utils.PointUtils
import com.reeman.commons.utils.PrecisionUtils
import com.reeman.commons.utils.TimeUtil
import org.json.JSONArray
import org.json.JSONObject

sealed class ROSEvent(var baseData: String = "")

class CurrentMapEvent(val map: String, val alias: String) : ROSEvent() {
    constructor(data: String) : this(
        map = data.replace("current_map[", "").replace("]", "").substringBefore(" "),
        alias = data.replace("current_map[", "").replace("]", "").substringAfter(" ", "")
            .takeIf { it.isNotEmpty() }?:""
    ) {
        baseData = data
    }
}

class HostnameEvent(val hostname: String) : ROSEvent() {
    constructor(data: String, v: Unit?) : this(
        hostname = data.replace("sys:boot:", "")
    ) {
        baseData = data
    }
}

class IPEvent(val wifiName: String, val ipAddress: String) :
    ROSEvent() {
    constructor(data: String) : this(
        wifiName = parseWifiName(data),
        ipAddress = parseIpAddress(data)
    ) {
        baseData = data
    }

    companion object {
        private fun canSplit(result: String): Boolean {
            return result.isNotEmpty() && !result.contains("connecting") && result.split(":").size == 3
        }

        private fun parseWifiName(result: String): String {
            return if (canSplit(result)) {
                result.split(":")[1]
            } else {
                ""
            }
        }

        private fun parseIpAddress(result: String): String {
            return if (canSplit(result)) {
                result.split(":")[2]
            } else {
                "127.0.0.1"
            }
        }
    }
}

class MoveStatusEvent(val status: Int) : ROSEvent() {
    constructor(data: String) : this(
        status = data.replace("move_status:", "").toIntOrNull() ?: 0
    ) {
        baseData = data
    }
}

class InitPoseEvent(val position: DoubleArray?) : ROSEvent() {
    constructor(data: String) : this(
        position = if (data.endsWith("initpose:0")) {
            null
        } else {
            data.replace("initpose:0,", "").split(" ").map { it.toDoubleOrNull() ?: 0.0 }
                .toDoubleArray()
        }
    ) {
        baseData = data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InitPoseEvent

        if (position != null) {
            if (other.position == null) return false
            if (!position.contentEquals(other.position)) return false
        } else if (other.position != null) return false

        return true
    }

    override fun hashCode(): Int {
        return position?.contentHashCode() ?: 0
    }
}

class NavPoseEvent(val position: DoubleArray) : ROSEvent() {
    constructor(data: String) : this(
        position = data.replace("nav:pose[", "").replace("]", "").split(",")
            .map { it.toDoubleOrNull() ?: 0.0 }
            .toDoubleArray()
    ) {
        baseData = data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NavPoseEvent

        if (!position.contentEquals(other.position)) return false

        return true
    }

    override fun hashCode(): Int {
        return position.contentHashCode()
    }
}

class WifiConnectResultEvent(val result: Int) : ROSEvent() {
    companion object {
        const val CONNECTING = 0
        const val CONNECTED = 1
        const val FAILED = -1
    }

    constructor(data: String) : this(
        result = when {
            data.endsWith("connecting") -> CONNECTING
            data.endsWith("success") -> CONNECTED
            else -> FAILED
        }
    ) {
        baseData = data
    }
}

class ApplyMapEvent(val isSuccess: Boolean, val map: String, val alias: String) : ROSEvent() {
    constructor(data: String) : this(
        isSuccess = data.startsWith("apply_map["),
        map = if (data.startsWith("apply_map[")) {
            data.replace("apply_map[", "").replace("]", "").substringBefore(" ")
        } else {
            ""
        },
        alias = if (data.startsWith("apply_map[")) {
            data.replace("apply_map[", "").replace("]", "").substringAfter(" ", "")
                .takeIf { it.isNotEmpty() }?:""
        } else {
            ""
        }
    ) {
        baseData = data
    }
}

class VersionInfoEvent(
    val hardwareVer: String,
    val firmwareVer: String,
    val loaderVer: String,
    val softVer: String
) : ROSEvent() {
    constructor(data: String) : this(
        hardwareVer = parseData(data, 0),
        firmwareVer = parseData(data, 1),
        loaderVer = parseData(data, 2),
        softVer = parseData(data, 3)
    ) {
        baseData = data
    }

    companion object {
        private fun parseData(data: String, index: Int): String {
            val split = data.replace("hfls_version:", "").split(" ")
            return if (index < split.size) {
                split[index]
            } else {
                "unknown"
            }
        }
    }
}

class ROSModelEvent(val model: Int) : ROSEvent() {
    companion object {
        const val NAVIGATION_MODEL = 1
        const val MAPPING_MODEL = 2
        const val REMAP_MODEL = 3
    }

    constructor(data: String) : this(
        model = data[6].toString().toIntOrNull() ?: 0
    ) {
        baseData = data
    }
}

class CoreDataEvent(
    val collisionData: Int,
    val antiFallData: Int,
    val emergencyButton: Int,
    val powerData: Int,
    val chargeState: Int
) : ROSEvent() {
    constructor(result: String) : this(
        parseData(result, 0),
        parseData(result, 1),
        parseData(result, 2),
        parseData(result, 3),
        parseData(result, 4)
    ) {
        baseData = result
    }

    companion object {
        private fun parseData(result: String, index: Int): Int {
            val split = result.replace("core_data{", "").replace("}", "").split(" ")
            return if (index < split.size) {
                split[index].toIntOrNull() ?: 0
            } else {
                0
            }
        }
    }
}

class NavigationResultEvent(
    val state: Int,
    val code: Int,
    val name: String,
    val distToGoal: Float,
    val mileage: Float
) : ROSEvent() {
    constructor(data: String) : this(
        state = parseData(data, 0).toIntOrNull() ?: 0,
        code = parseData(data, 1).toIntOrNull() ?: 0,
        name = parseData(data, 2),
        distToGoal = parseData(data, 3).toFloatOrNull() ?: 0.0f,
        mileage = parseData(data, 4).toFloatOrNull() ?: 0.0f
    ) {
        baseData = data
    }

    companion object {
        private fun parseData(result: String, index: Int): String {
            val split = result.replace("nav_result{", "").replace("}", "").split(" ")
            return if (index < split.size) {
                split[index]
            } else {
                "0"
            }
        }
    }
}

class BatteryFixResultEvent(
    val manufacturer: String,
    val nominalVoltage: Int,
    val temperature: Float,
    val cycleTimes: Int,
    val ratedCapacity: Int,
    val fullCapacity: Int,
    val capacity: Int,
    val health: Int
) {
    constructor(data: String) : this(
        manufacturer = parseData(data, 0),
        nominalVoltage = parseData(data, 1).toIntOrNull() ?: 0,
        temperature = parseData(data, 2).toFloatOrNull() ?: 0.0f,
        cycleTimes = parseData(data, 3).toIntOrNull() ?: 0,
        ratedCapacity = parseData(data, 4).toIntOrNull() ?: 0,
        fullCapacity = parseData(data, 5).toIntOrNull() ?: 0,
        capacity = parseData(data, 6).toIntOrNull() ?: 0,
        health = parseData(data, 7).toIntOrNull() ?: 0
    )

    override fun toString(): String {
        return "制造商 : $manufacturer, " +
                "标称电压 : $nominalVoltage mV, " +
                "温度 : ${temperature / 10.0f} ℃, " +
                "循环次数 : $cycleTimes, " +
                "额定容量 : $ratedCapacity mAh, " +
                "满电容量 : $fullCapacity mAh, " +
                "当前容量 : $capacity mAh, " +
                "健康程度 : ${getHealth(health)}"
    }

    private fun getHealth(health: Int): String {
        return when (health) {
            1 -> "健康"
            2 -> "过热"
            3 -> "容量老化"
            4 -> "过冷"
            else -> "未知"
        }
    }

    companion object {
        private fun parseData(data: String, index: Int): String {
            val split = data.replace("battery_info{", "").replace("}", "").split(" ")
            return if (index < split.size) {
                split[index]
            } else {
                "0"
            }
        }

    }
}

class BatteryDynamicEvent(
    val voltage: Int,
    val current: Int,
    val adapterCurrent: Int,
    val warning: String,
    val offline: Boolean
) : ROSEvent() {
    constructor(data: String) : this(
        voltage = parseData(data, 0).toIntOrNull() ?: 0,
        current = parseData(data, 1).toIntOrNull() ?: 0,
        adapterCurrent = parseData(data, 2).toIntOrNull() ?: 0,
        warning = parseData(data, 3),
        offline = parseData(data, 4) == "0"
    ) {
        baseData = data
    }

    override fun toString(): String {
        return "电压 : $voltage mV, " +
                "电流 : $current mA, " +
                "适配器电流 : $adapterCurrent mA, " +
                "警告 : $warning, " +
                "离线 : $offline"
    }

    companion object {
        private fun parseData(data: String, index: Int): String {
            val split = data.replace("current_info{", "").replace("}", "").split(" ")
            return if (index < split.size) {
                split[index]
            } else {
                "-1"
            }
        }
    }
}

class PowerOffEvent(val result: Int) : ROSEvent() {

    companion object {
        const val APPLICATION_SHUTDOWN = 1
        const val REBOOT = 2
        const val BUTTON_SHUTDOWN = 3
        const val POWER_LOW_SHUTDOWN = 4
    }

    constructor(data: String) : this(
        result = data[9].toString().toIntOrNull() ?: 0
    ) {
        baseData = data
    }
}

class SensorsEvent(
    val isIMUWarn: Boolean,
    val isLidarWarn: Boolean,
    val isOdomWarn: Boolean,
    val isCam3DWarn: Boolean,
    val isROSWarn: Boolean
) : ROSEvent() {
    val isSensorWarn: Boolean
        get() = isCam3DWarn || isLidarWarn || isOdomWarn || isIMUWarn || isROSWarn

    constructor(data: String) : this(
        isIMUWarn = parseBoolean(data, 0),
        isLidarWarn = parseBoolean(data, 1),
        isOdomWarn = parseBoolean(data, 2),
        isCam3DWarn = parseBoolean(data, 3),
        isROSWarn = parseBoolean(data, 4)
    ) {
        baseData = data
    }

    companion object {
        private fun parseBoolean(data: String, index: Int): Boolean {
            val split = data.replace("check_sensors{", "").replace("}", "").split(" ")
            return if (index < split.size) {
                split[index] == "0"
            } else {
                false
            }
        }
    }
}

class WheelStatusEvent(
    val state: Int,
    val currentLeft: Int,
    val currentRight: Int,
    val tempLeft: Float,
    val tempRight: Float,
    val driverTempLeft: Float,
    val driverTempRight: Float,
    val codeLeft: Int,
    val codeRight: Int,
    val model: String,
    val version: String
) : ROSEvent() {
    constructor(result: String) : this(
        state = parseInt(result, 0),
        currentLeft = parseInt(result, 1),
        currentRight = parseInt(result, 2),
        tempLeft = parseFloat(result, 3),
        tempRight = parseFloat(result, 4),
        driverTempLeft = parseFloat(result, 5),
        driverTempRight = parseFloat(result, 6),
        codeLeft = parseInt(result, 7),
        codeRight = parseInt(result, 8),
        model = parseString(result, 9),
        version = parseString(result, 10)
    ) {
        baseData = result
    }

    companion object {

        /**
         * 松轴 0x00
         * 锁轴0x01
         * 轮子故障 0x02
         * 刹车0x04
         * 停止(stop)0x08
         * 离线状态 0x80
         */
        const val looseShaft = 0x00
        const val lockShaft = 0x01
        const val wheelFailure = 0x02
        const val brakes = 0x03
        const val stop = 0x08
        const val offline = 0x80

        private fun parseInt(result: String, index: Int): Int {
            val splitResult = result.replace("wheel_status{", "").replace("}", "").split(" ")
            return if (index < splitResult.size) {
                splitResult[index].toInt()
            } else {
                0
            }
        }

        private fun parseFloat(result: String, index: Int): Float {
            val splitResult = result.replace("wheel_status{", "").replace("}", "").split(" ")
            return if (index < splitResult.size) {
                splitResult[index].toFloat()
            } else {
                0f
            }
        }

        private fun parseString(result: String, index: Int): String {
            val splitResult = result.replace("wheel_status{", "").replace("}", "").split(" ")
            return if (index < splitResult.size) {
                splitResult[index]
            } else {
                "unknown"
            }
        }
    }

    override fun toString(): String {
        return "工作状态: $state, 电流[左: $currentLeft 右: $currentRight], 温度[左: $tempLeft 右: $tempRight], " +
                "驱动器温度[左: $driverTempLeft 右: $driverTempRight], 错误码[左: $codeLeft 右: $codeRight], " +
                "型号: $model, 版本号: $version"
    }
}

class MissPoseEvent(val result: Int) : ROSEvent() {
    constructor(data: String) : this(
        result = data[9].toString().toIntOrNull() ?: 0
    ) {
        baseData = data
    }
}

class MoveDoneEvent(val result: Int) : ROSEvent() {
    constructor(data: String) : this(
        result = data.replace("move:done:", "").toIntOrNull() ?: 0
    ) {
        baseData = data
    }
}

class BaseVelEvent(val lineSpeed: Float, val angularSpeed: Float) : ROSEvent() {
    constructor(data: String) : this(
        lineSpeed = parseData(data, 0),
        angularSpeed = parseData(data, 1)
    ) {
        baseData = data
    }

    companion object {
        private fun parseData(data: String, index: Int): Float {
            val split = data.replace("base_vel[", "").replace("]", "").split(" ")
            return if (index < split.size) {
                split[index].toFloatOrNull() ?: 0f
            } else {
                0f
            }
        }
    }
}

class GlobalPathEvent(val pathList: List<Double>) : ROSEvent()

class SpecialPlanEvent(val roomList: List<Room>) : ROSEvent() {

    constructor(data: String) : this(
        roomList = parseData(data)
    ) {
        baseData = data
    }

    companion object {
        private fun parseData(data: String): List<Room> {
            val roomList: MutableList<Room> = ArrayList()
            try {
                val jsonObject = JSONObject(data.replace("special_plan:", ""))
                val sp: JSONArray? = jsonObject.optJSONArray("sp")
                sp?.let {
                    for (i in 0 until it.length()) {
                        val temp = it.getJSONObject(i)
                        val room = Room()
                        room.name = temp.optString("n")
                        if (room.name.isNullOrBlank()) continue
                        room.type = temp.optInt("type")
                        val c = temp.optJSONArray("c")
                        if (c == null || c.length() == 0) continue
                        if (c.length() == 4) {
                            val radian = PrecisionUtils.setScale(
                                PointUtils.calculateRadian(
                                    c.optDouble(0),
                                    c.optDouble(1),
                                    c.optDouble(2),
                                    c.optDouble(3)
                                )
                            )
                            room.coordination = ArrayList()
                            for (i1 in 0..5) {
                                if (i1 == 2 || i1 == 5) {
                                    room.coordination.add(radian)
                                } else if (i1 < 2) {
                                    room.coordination.add(c.optDouble(i1))
                                } else {
                                    room.coordination.add(c.optDouble(i1 - 1))
                                }
                            }
                        }
                        roomList.add(room)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return roomList
        }
    }
}

class AGVTagPoseEvent(val data: String) : ROSEvent() {

    constructor(data: String, u: Unit) : this(
        data = data.replace("agv_tag_pose{", "").replace("}", "")
    ) {
        baseData = data
    }
}

class AGVDockResultEvent(val target: String?, val result: Int) : ROSEvent() {
    constructor(data: String) : this(
        target = parseTarget(data),
        result = if (parseResult(data)) AGV_DOCK_SUCCESS else AGV_DOCK_FAILED
    ){
        baseData = data
    }

    companion object {

        const val AGV_DOCK_SUCCESS = 1
        const val AGV_DOCK_FAILED = -1

        private fun parseResult(data: String): Boolean = data.startsWith("agv_success{")

        private fun parseTarget(data: String): String? =
            if (parseResult(data)) {
                data.replace("agv_success{", "").replace("}", "")
            } else {
                null
            }
    }
}

class GetPlanResultEvent(val isSuccess: Boolean):ROSEvent(){

    constructor(data: String):this(
        isSuccess = data != "get_plan:error"
    ){
        baseData = data
    }
}

class FixedPathResultEvent(val pathPointList:List<String>):ROSEvent()

class RobotTypeEvent(val robotType:Int):ROSEvent(){
    constructor(data: String):this(
        robotType = data.replace("robot_type:robot", "").toInt()
    ){
        baseData = data
    }
}

class InitiativeLiftingModuleStateEvent(val action:Int,val state:Int = 1):ROSEvent()

class PowerOnTimeEvent(powerOnTime: Long):ROSEvent(){

    constructor(data: String):this(
        powerOnTime = TimeUtil.convertToTimestamp(data.replace("power_on_t:",""))
    ){
        baseData = data
    }
}










