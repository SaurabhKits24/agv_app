package com.reeman.commons.state

import android.app.Activity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.reeman.commons.event.BatteryFixResultEvent
import com.reeman.commons.event.CoreDataEvent
import com.reeman.commons.event.CurrentMapEvent
import com.reeman.commons.event.ROSModelEvent
import com.reeman.commons.event.SensorsEvent
import com.reeman.commons.event.VersionInfoEvent
import com.reeman.commons.settings.CommutingTimeSetting
import com.reeman.commons.settings.DoorControlSetting
import com.reeman.commons.settings.ElevatorSetting
import com.reeman.commons.settings.ModeNormalSetting
import com.reeman.commons.settings.ModeQRCodeSetting
import com.reeman.commons.settings.ModeRouteSetting
import com.reeman.commons.settings.ReturningSetting
import timber.log.Timber

object RobotInfo {

    //机器状态
    var state: State = State.IDLE

    //任务模式
    var mode: TaskMode = TaskMode.MODE_NORMAL

    //导航相关信息
    var ROSHostname: String = ""

    //机器别名
    var robotAlias: String? = null
        get() {
            return if (field.isNullOrBlank()) {
                ROSHostname
            } else {
                field
            }
        }

    var ROSWifi: String = ""
    var ROSIPAddress: String = ""

    //是否正在充电
    val isCharging
        get() = lastCoreDataEvent?.chargeState == 2 || lastCoreDataEvent?.chargeState == 3

    //是否充电桩充电
    val isWirelessCharging
        get() = lastCoreDataEvent?.chargeState == 2

    //是否适配器充电
    val isACCharging
        get() = lastCoreDataEvent?.chargeState == 3

    //是否正在对接充电桩
    val isChargeDocking
        get() = lastCoreDataEvent?.chargeState == 8

    //充电状态
    val chargeState: Int
        get() {
            return lastCoreDataEvent?.chargeState ?: return -1
        }

    //电量
    val powerLevel: Int
        get() {
            return lastCoreDataEvent?.powerData ?: return -1
        }

    fun isLowPower() = powerLevel != -1 && powerLevel <= autoChargePowerLevel

    //急停开关
    val emergencyButton: Int
        get() {
            return lastCoreDataEvent?.emergencyButton ?: return -1
        }

    //急停开关被按下
    val isEmergencyButtonDown: Boolean
        get() {
            return lastCoreDataEvent?.emergencyButton == 0
        }

    //上次充电方式
    var lastChargeType = 0

    //上次充电电量
    var lastPowerLevel: Int = -1

    //上次充电时间
    var lastChargeTime: Long = -1

    //是否正在导航
    var isNavigating = false

    //充电失败次数
    var chargeFailedCount = 0

    //电量,急停,充电状态,防跌,碰撞
    var lastCoreDataEvent: CoreDataEvent? = null

    var versionEvent: VersionInfoEvent? = null
        set(value) {
            field = value
            value?.let {
                val index = it.softVer.indexOf("-v")
                if (index != -1 && index + 7 <= it.softVer.length) {
                    val rosVersionCodeStr =
                        it.softVer.substring(index + 2, index + 7).replace(".", "")
                    if (rosVersionCodeStr.toIntOrNull() != null) {
                        rosVersionCode = rosVersionCodeStr.toInt()
                        Timber.d("ros版本号 : $rosVersionCode")
                    } else {
                        Timber.w("无法提取ros版本号: $rosVersionCodeStr")
                    }
                } else {
                    Timber.w("无法提取ros版本号: ${it.softVer}")
                }
            }
        }
    var rosVersionCode: Int = 0

    fun supportEnterElevatorPoint() = rosVersionCode >= 325

    //传感器数据
    var lastSensorsData: SensorsEvent? = null

    //机器当前坐标
    var currentPosition: DoubleArray? = null
        set(value) {
            field = if (value != null) {
                DoubleArray(value.size) { index -> value[index] }
            } else {
                null
            }
        }

    //充电屏保是否在前台
    var chargingScreenSaverShowTime: Long = -1

    var modeNormalSetting: ModeNormalSetting = ModeNormalSetting.getDefault()

    var modeRouteSetting: ModeRouteSetting = ModeRouteSetting.getDefault()

    var modeQRCodeSetting: ModeQRCodeSetting = ModeQRCodeSetting.getDefault()

    var returningSetting: ReturningSetting = ReturningSetting.getDefault()

    val isNormalModeWithManualLiftControl: Boolean
        get() = modeNormalSetting.manualLiftControlOpen && isLiftModelInstalled && isSpaceShip()
    var elevatorSetting: ElevatorSetting = ElevatorSetting.getDefault()

    var doorControlSetting: DoorControlSetting = DoorControlSetting.getDefault()

    //梯控模式开关
    val isElevatorMode: Boolean
        get() = elevatorSetting.open

    //门控模式开关
    val isDoorControlMode: Boolean
        get() = doorControlSetting.open

    //充电桩所在地图
    val chargingPileMap: Pair<String, String>?
        get() = elevatorSetting.chargingPileMap


    val productionPointMap: Pair<String, String>?
        get() = elevatorSetting.productionPointMap

    //是否正在切换地图
    var isSwitchingMap = false

    //是否正在等待initpose:0
    var isRepositioning = false

    //导航模式
    var navigationMode = NavigationMode.autoPathMode

    //地图信息
    var currentMapEvent: CurrentMapEvent = CurrentMapEvent("unknown", "unknown")

    //是否在检测能否到达目标点(不处理全局路径
    var isGetPlan = false

    /**
     * 1 : 长56,宽36(方形低盘)
     * 2 : 半径24(月轮骑士低盘)
     * 3 : 半径22.5(风火轮低盘)
     * 4 : 长81,宽56(飞船,带标签码)
     * 5 : 长70,宽47(大狗)
     * 6 : 长81,宽56(飞船,不带标签码)
     * 7 : 飞船(双激光
     * 8 : 大狗(双激光
     */
    var robotType = 4

    fun isSpaceShip(): Boolean {
        return robotType == 4 || robotType == 6 || robotType == 7
    }

    //上下班设置
    var commutingTimeSetting: CommutingTimeSetting = CommutingTimeSetting.getDefault()

    //低电电量
    var autoChargePowerLevel = 20

    //顶升状态 0:低位 1:顶起
    var liftModelState = 0
        get() {
            return if (isLiftModelInstalled && isSpaceShip()) {
                field
            } else {
                0
            }
        }

    //是否正在顶升
    var isLifting = false
        get() = isLiftModelInstalled && isSpaceShip() && field

    //是否标签码导航中
    var isQRCodeNavigating = false


    //ROS模式
    var rosModel = ROSModelEvent.NAVIGATION_MODEL

    //ROS是否正在建图
    val isMapping: Boolean
        get() = rosModel != ROSModelEvent.NAVIGATION_MODEL

    //正在任务倒计时
    var isCountdownToTask = false

    //机器自检(定时重启)
    var isSelfChecking = false

    //任务异常结束时的提示内容,在触发急停开关,线充,建图,点击弹窗确认时置为null
    var taskAbnormalFinishPrompt: String? = null

    //是否安装顶升模块
    var isLiftModelInstalled = false

    //是否开启就近停靠
    var isStopNearbyOpen = true

    //点位滚动显示
    var isPointScrollShow = true

    var activityStack = ArrayList<Activity>()

    fun addTOActivityStack(activity: Activity) {
        activityStack.add(activity)
    }

    fun removeFromActivityStack(activity: Activity) {
        activityStack.remove(activity)
    }

    fun getLastActivity(): Activity? {
        return activityStack.lastOrNull()
    }

    fun clearActivityStack() {
        activityStack.clear()
    }

    var getAltitudeState = false

    var lastTarget: String = ""

    var isNavigationCancelable = false

    var isTimeSynchronized = false

    var lastSynchronizedTimestamp = 0L

}