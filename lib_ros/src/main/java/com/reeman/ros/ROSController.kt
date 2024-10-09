package com.reeman.ros

import android.os.Build
import android.util.Log
import com.reeman.commons.BuildConfig
import com.reeman.commons.event.AGVDockResultEvent
import com.reeman.commons.event.AGVTagPoseEvent
import com.reeman.commons.event.ApplyMapEvent
import com.reeman.commons.event.BaseVelEvent
import com.reeman.commons.event.BatteryDynamicEvent
import com.reeman.commons.event.BatteryFixResultEvent
import com.reeman.commons.event.CoreDataEvent
import com.reeman.commons.event.CurrentMapEvent
import com.reeman.commons.event.FixedPathResultEvent
import com.reeman.commons.event.GetPlanResultEvent
import com.reeman.commons.event.GlobalPathEvent
import com.reeman.commons.event.HostnameEvent
import com.reeman.commons.event.IPEvent
import com.reeman.commons.event.InitPoseEvent
import com.reeman.commons.event.InitiativeLiftingModuleStateEvent
import com.reeman.commons.event.MissPoseEvent
import com.reeman.commons.event.MoveDoneEvent
import com.reeman.commons.event.MoveStatusEvent
import com.reeman.commons.event.NavPoseEvent
import com.reeman.commons.event.NavigationResultEvent
import com.reeman.commons.event.PowerOffEvent
import com.reeman.commons.event.PowerOnTimeEvent
import com.reeman.commons.event.ROSEvent
import com.reeman.commons.event.ROSModelEvent
import com.reeman.commons.event.RobotTypeEvent
import com.reeman.commons.event.SensorsEvent
import com.reeman.commons.event.SpecialPlanEvent
import com.reeman.commons.event.VersionInfoEvent
import com.reeman.commons.event.WheelStatusEvent
import com.reeman.commons.event.WifiConnectResultEvent
import com.reeman.commons.provider.SerialPortProvider
import com.reeman.commons.state.RobotInfo
import com.reeman.ros.callback.ROSCallback
import com.reeman.serialport.controller.RobotActionController
import com.reeman.serialport.controller.RosCallbackParser
import io.reactivex.Completable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object ROSController : RosCallbackParser.RosCallback {
    private var robotActionController: RobotActionController? = null

    private var scheduledExecutorService: ScheduledExecutorService? = null
    private var lastBatteryFixResultEvent: BatteryFixResultEvent? = null

    private var lastSpeed: String? = null
    private var lastTolerance = 0
    private var lastRobotLength = -1F
    private var lastRobotWidth = -1F
    private var lastRadius = -1F
    private var lastLidarWidth = -1F
    private var lastGlobalcost = -1
    private var lastDistance = -1.0
    private var lastPositionAutoUploadReport = 0
    private val globalPathList = mutableListOf<String>()
    private val fixedPathList = mutableListOf<String>()


    private var disposable: Disposable? = null
    private val rosCallbackList = CopyOnWriteArrayList<ROSCallback>()
    private val eventQueue: ConcurrentLinkedQueue<ROSEvent> = ConcurrentLinkedQueue()

    var isModelRequest = false

    /**
     * 地图切换/导航重启后重置所有相关参数
     */
    fun resetAllROSParameter() {
        lastSpeed = null
        lastTolerance = 0
        lastRobotLength = -1F
        lastRobotWidth = -1F
        lastRadius = -1F
        lastLidarWidth = -1F
        lastGlobalcost = -1
        lastDistance = -1.0
        lastPositionAutoUploadReport = 0
    }

    fun resetLastSpeed() {
        lastSpeed = null
    }

    fun registerListener(callback: ROSCallback) {
        Log.w(this::class.simpleName, "registerListener")
        synchronized(rosCallbackList) {
            rosCallbackList.add(callback)
        }
    }

    fun unregisterListener(callback: ROSCallback) {
        Log.w(this::class.simpleName, "unregisterListener")
        synchronized(rosCallbackList) {
            rosCallbackList.remove(callback)
        }
    }


    init {
        robotActionController = RobotActionController.getInstance()
        scheduledExecutorService = Executors.newScheduledThreadPool(2)
    }

    @Throws(Exception::class)
    fun init(vararg uploadDirs: String) {
        robotActionController?.init(
            115200,
            SerialPortProvider.ofChassis(Build.PRODUCT),
            this,
            *uploadDirs
        )
        scheduledExecutorService?.scheduleWithFixedDelay(
            { robotActionController?.getBatteryInfo() },
            5,
            5,
            TimeUnit.SECONDS
        )
        disposable = Observable.interval(5, 5, TimeUnit.MILLISECONDS)
            .flatMap {
                if (eventQueue.isNotEmpty()) {
                    val rosEvent = eventQueue.poll()
                    rosEvent?.let { event ->
                        return@flatMap Observable.just(event)
                    }
                }
                Observable.empty()
            }
            .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                eventDispatch(it)
            }, {
                throw it
            })
        currentInfoControl(true)
        positionAutoUploadControl(true)
    }

    private fun logBatteryInfo(batteryDynamicEvent: BatteryDynamicEvent) {
        Timber.tag(BuildConfig.BATTERY_REPORT_DIR).w(
            "电量 : ${RobotInfo.lastCoreDataEvent?.powerData}, 充电状态 : ${RobotInfo.lastCoreDataEvent?.chargeState}, $batteryDynamicEvent, $lastBatteryFixResultEvent"
        )
    }

    private fun logWheelStatus(wheelStatusEvent: WheelStatusEvent) {
        Timber.tag(BuildConfig.WHEEL_INFO_DIR).w(wheelStatusEvent.toString())
    }


    @Synchronized
    fun stopListen() {
        rosCallbackList.clear()
        currentInfoControl(false)
        positionAutoUploadControl(false)
        Completable.timer(50, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe{object :CompletableObserver{
                override fun onSubscribe(d: Disposable) {
                    TODO("Not yet implemented")
                }

                override fun onComplete() {
                    TODO("Not yet implemented")
                    robotActionController?.stopListen()
                    robotActionController = null
                }

                override fun onError(e: Throwable) {
                    TODO("Not yet implemented")
                }
            }}
        scheduledExecutorService?.shutdown()
        scheduledExecutorService = null
    }

    private fun eventDispatch(rosEvent: ROSEvent) {
        if (rosEvent !is BaseVelEvent
            && rosEvent !is BatteryDynamicEvent
            && rosEvent !is CoreDataEvent
            && rosEvent !is NavPoseEvent
            && rosEvent !is WheelStatusEvent
            && rosEvent !is VersionInfoEvent
            && rosEvent !is SensorsEvent
            && rosEvent !is GlobalPathEvent
            && rosEvent !is FixedPathResultEvent
            && rosEvent !is AGVTagPoseEvent
        ) {
            Timber.w("ros msg: ${rosEvent.baseData}")
        }
        if (rosCallbackList.isNotEmpty()) {
            synchronized(rosCallbackList) {
                if (rosCallbackList.isNotEmpty()) {
                    Log.w(this::class.simpleName, "eventDispatch : $rosCallbackList")
                    when (rosEvent) {
                        is ApplyMapEvent -> rosCallbackList.forEach { it.onApplyMapEvent(rosEvent) }
                        is BaseVelEvent -> rosCallbackList.forEach { it.onBaseVelEvent(rosEvent) }
                        is BatteryDynamicEvent -> logBatteryInfo(rosEvent)
                        is CoreDataEvent -> rosCallbackList.forEach { it.onCoreDataEvent(rosEvent) }
                        is CurrentMapEvent -> rosCallbackList.forEach {
                            it.onCurrentMapEvent(
                                rosEvent
                            )
                        }

                        is HostnameEvent -> rosCallbackList.forEach { it.onHostNameEvent(rosEvent) }
                        is IPEvent -> rosCallbackList.forEach { it.onIPEvent(rosEvent) }
                        is InitPoseEvent -> rosCallbackList.forEach { it.onInitPoseEvent(rosEvent) }
                        is MissPoseEvent -> rosCallbackList.forEach { it.onMissPoseEvent(rosEvent) }
                        is MoveDoneEvent -> rosCallbackList.forEach { it.onMoveDoneEvent(rosEvent) }
                        is MoveStatusEvent -> rosCallbackList.forEach {
                            it.onMoveStatusEvent(
                                rosEvent
                            )
                        }

                        is NavPoseEvent -> rosCallbackList.forEach { it.onNavPoseEvent(rosEvent) }
                        is NavigationResultEvent -> rosCallbackList.forEach {
                            it.onNavResultEvent(
                                rosEvent
                            )
                        }

                        is PowerOffEvent -> rosCallbackList.forEach { it.onPowerOffEvent(rosEvent) }
                        is ROSModelEvent -> rosCallbackList.forEach { it.onROSModelEvent(rosEvent) }
                        is SensorsEvent -> rosCallbackList.forEach { it.onSensorsEvent(rosEvent) }
                        is VersionInfoEvent -> rosCallbackList.forEach { it.onVersionEvent(rosEvent) }
                        is WheelStatusEvent -> {
                            logWheelStatus(rosEvent)
                            rosCallbackList.forEach {
                                it.onWheelStatusEvent(rosEvent)
                            }
                        }

                        is WifiConnectResultEvent -> rosCallbackList.forEach {
                            it.onWifiConnectEvent(
                                rosEvent
                            )
                        }

                        is GlobalPathEvent -> rosCallbackList.forEach {
                            it.onGlobalPathEvent(
                                rosEvent
                            )
                        }

                        is AGVDockResultEvent -> rosCallbackList.forEach {
                            it.onAGVDockResultEvent(
                                rosEvent
                            )
                        }

                        is AGVTagPoseEvent -> rosCallbackList.forEach {
                            it.onAGVTagPoseEvent(
                                rosEvent
                            )
                        }

                        is FixedPathResultEvent -> rosCallbackList.forEach {
                            it.onFixedPathResultEvent(
                                rosEvent
                            )
                        }

                        is GetPlanResultEvent -> rosCallbackList.forEach {
                            it.onGetPlanResultEvent(
                                rosEvent
                            )
                        }

                        is InitiativeLiftingModuleStateEvent -> rosCallbackList.forEach {
                            it.onInitiativeLiftingModuleStateEvent(
                                rosEvent
                            )
                        }

                        is RobotTypeEvent -> rosCallbackList.forEach { it.onRobotTypeEvent(rosEvent) }
                        is SpecialPlanEvent -> rosCallbackList.forEach {
                            it.onSpecialPlanEvent(
                                rosEvent
                            )
                        }

                        is PowerOnTimeEvent -> rosCallbackList.forEach {
                            it.onROSPowerOnTimeEvent(
                                rosEvent
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResult(result: String) {
        when {
            result.startsWith("current_map") -> eventQueue.add(CurrentMapEvent(result))

            result.startsWith("sys:boot") -> eventQueue.add(HostnameEvent(result, Unit))

            result.startsWith("ip") -> eventQueue.add(IPEvent(result))

            result.startsWith("wlan") -> eventQueue.add(IPEvent(""))

            result.startsWith("move_status:") -> eventQueue.add(MoveStatusEvent(result))

            result.startsWith("initpose:0") -> eventQueue.add(InitPoseEvent(result))

            result.startsWith("nav:pose[") -> eventQueue.add(NavPoseEvent(result))

            result.startsWith("wifi:connect") -> eventQueue.add(WifiConnectResultEvent(result))

            result.startsWith("apply_map") -> eventQueue.add(ApplyMapEvent(result))

            result.startsWith("hfls_version") -> eventQueue.add(VersionInfoEvent(result))

            result.startsWith("model:") -> eventQueue.add(ROSModelEvent(result))

            result.startsWith("core_data") -> eventQueue.add(CoreDataEvent(result))

            result.startsWith("nav_result") -> {
                if (result.startsWith("nav_result{3") ||
                    result.startsWith("nav_result{2") ||
                    result.startsWith("nav_result{4") ||
                    result.startsWith("nav_result{5") ||
                    result.startsWith("nav_result{6")
                ) {
                    eventQueue.add(NavigationResultEvent(result))
                } else {
                    Timber.w("导航结果2 : $result")
                }
            }

            result.startsWith("battery_info") -> lastBatteryFixResultEvent =
                BatteryFixResultEvent(result)

            result.startsWith("current_info") -> eventQueue.add(BatteryDynamicEvent(result))

            result.startsWith("power_off") -> eventQueue.add(PowerOffEvent(result))

            result.startsWith("base_upgrade:") -> Timber.w("电源板升级 : $result")

            result.startsWith("check_sensors") -> eventQueue.add(SensorsEvent(result))

            result.startsWith("wheel_status") -> eventQueue.add(WheelStatusEvent(result))

            result.startsWith("misspose") -> eventQueue.add(MissPoseEvent(result))

            result.startsWith("robot_type:") -> eventQueue.add(RobotTypeEvent(result))

            result.startsWith("move:done:") -> eventQueue.add(MoveDoneEvent(result))

            result.startsWith("base_vel[") -> eventQueue.add(BaseVelEvent(result))

            result.startsWith("global_path") -> receiveGlobalPath(result)

            result.startsWith("special_plan") -> eventQueue.add(SpecialPlanEvent(result))

            result.startsWith("agv_tag_pose") -> eventQueue.add(AGVTagPoseEvent(result, Unit))

            result.startsWith("agv_") -> eventQueue.add(AGVDockResultEvent(result))

            result.startsWith("get_plan") -> eventQueue.add(GetPlanResultEvent(result))

            result.startsWith("short_dij") -> receiveFixedPath(result)

            result.startsWith("robot_type:") -> eventQueue.add(RobotTypeEvent(result))

            result.startsWith("base_data") -> {
                val replace = result.replace("base_data:{", "").replace("}", "").replace(" ", "")
                if (replace.startsWith("06", 4)) {
                    eventQueue.add(
                        InitiativeLiftingModuleStateEvent(replace.substring(6, 8).toInt())
                    )
                } else if (replace.startsWith("08", 4)) {
                    eventQueue.add(
                        InitiativeLiftingModuleStateEvent(
                            replace.substring(6, 8).toInt(), replace.substring(8, 10).toInt()
                        )
                    )
                } else {
                    Log.v("rosController", "unused path_through message: $result")
                }
            }

            result.startsWith("power_on_t:") -> eventQueue.add(PowerOnTimeEvent(result))

            else -> Log.v("rosController", "unused message: $result")
        }

    }

    private fun receiveFixedPath(path: String) {
        val isNewPath = path.startsWith("short_dij:")
        if (isNewPath) fixedPathList.clear()
        Timber.d(path)
        if (path.endsWith("+") || !isNewPath) {
            fixedPathList.add(path)
        }
        if (path.endsWith("+")) return
        if (fixedPathList.isNotEmpty()) {
            val pathList = fixedPathList
                .joinToString("") {
                    var result = it
                    if (result.startsWith("short_dij:")) {
                        result = result.replaceFirst("short_dij:", "")
                    } else if (result.startsWith("short_dij1:")) {
                        result = result.replaceFirst("short_dij1:", "")
                    }
                    if (result.endsWith("+")) {
                        result = result.replace(Regex("\\+$"), "")
                    }
                    result
                }
                .split(" ")
                .filter { it.isNotBlank() }
            Timber.w("固定路线: $pathList")
            eventQueue.add(FixedPathResultEvent(pathList))
            fixedPathList.clear()
            return
        }
        val pathList =
            path.replace("short_dij:", "").trim().split(" ")
        Timber.w("固定路线: $pathList")
        eventQueue.add(FixedPathResultEvent(pathList))
    }

    private fun receiveGlobalPath(path: String) {
        val isNewPath = path.startsWith("global_path:")
        if (isNewPath) globalPathList.clear()
        Timber.d(path)
        if (path.endsWith(" +") || !isNewPath) {
            globalPathList.add(path)
        }
        if (path.endsWith(" +"))
            return
        if (globalPathList.isNotEmpty()) {
            val pathList = globalPathList
                .joinToString("") {
                    it.replace("global_path:", "")
                        .replace("global_path1:", "")
                        .replace("+", "")

                }
                .split(" ")
                .filter { it.isNotBlank() }
                .mapNotNull { it.toDoubleOrNull() }
            Timber.w("全局路线: $pathList")
            eventQueue.add(GlobalPathEvent(pathList))
            globalPathList.clear()
            return
        }
        val pathList =
            path.replace("global_path:", "").trim().split(" ").mapNotNull { it.toDoubleOrNull() }
        Timber.w("全局路线: $pathList")
        eventQueue.add(GlobalPathEvent(pathList))
    }

    fun closeLaserReport() {
        robotActionController?.sendCommand("switch_lidar[off]")
    }

    fun turn(angle: Int) {
        Timber.w("转向 :%s", angle)
        robotActionController?.sendCommand("move[0,$angle,0.1]")
    }

    fun straight(distance: Int, speed: Double) {
        robotActionController?.sendCommand("move[$distance,0,$speed]")
    }

    fun setROSIP(ip: String) {
        robotActionController?.setIpAddress(ip)
    }

    fun stopMove() {
        Timber.w("停止移动")
        robotActionController?.stopMove()
    }

    fun applyMap(map: String) {
        Timber.d("切换地图 $map")
        robotActionController?.applyMap(map)
    }

    fun relocateByCoordinate(coordinate: DoubleArray) {
        Timber.w("重定位 ${coordinate.contentToString()}")
        robotActionController?.relocateByCoordinate(coordinate)
    }

    fun relocAbsolute(pointName: String) {
        Timber.w("绝对位置重定位 %s", pointName)
        robotActionController?.sendCommand("nav:reloc_absolute[$pointName]")
    }

    fun relocAbPoint(coordinate: DoubleArray) {
        Timber.w("绝对位置重定位 ${coordinate.contentToString()}")
        robotActionController?.sendCommand(
            "nav:reloc_abpoint${
                coordinate.contentToString().replace(" ", "")
            }"
        )
    }

    fun getCurrentMap() {
        robotActionController?.getCurrentMap()
    }

    fun navigationByCoordinate(position: DoubleArray) {
        RobotInfo.isNavigating = true
        RobotInfo.isGetPlan = false
        RobotInfo.isNavigationCancelable = false
        val contentToString = position.contentToString().replace(" ", "")
        RobotInfo.lastTarget = contentToString.replace("[", "").replace("]", "")
        Timber.w("导航去 $contentToString")
        robotActionController?.sendCommand("goal:nav$contentToString")
    }

    fun navigationByPoint(point: String) {
        RobotInfo.isNavigating = true
        RobotInfo.isGetPlan = false
        RobotInfo.isNavigationCancelable = false
        RobotInfo.lastTarget = point
        Timber.d("导航去 $point")
        robotActionController?.navigationByPoint(point)
    }

    fun navigationByPathPoint(point: String) {
        RobotInfo.isNavigating = true
        RobotInfo.isGetPlan = false
        RobotInfo.isNavigationCancelable = false
        RobotInfo.lastTarget = point
        Timber.d("固定路线导航去 $point")
        robotActionController!!.sendCommand("points_path[$point]")
    }

    fun navigateListPoint(positions: DoubleArray) {
        RobotInfo.isNavigating = true
        RobotInfo.isGetPlan = false
        RobotInfo.isNavigationCancelable = false
        val contentToString = positions.contentToString().replace(" ", "")
        RobotInfo.lastTarget = contentToString.replace("[", "").replace("]", "")
        Timber.d("固定路线导航 : %s", contentToString)
        robotActionController?.sendCommand("list_point$contentToString")
    }

    fun connectROSWifi(ssid: String, pwd: String) {
        robotActionController?.connectROSWifi(ssid, pwd)
    }

    fun getHostIP() {
        robotActionController?.getHostIp()
    }

    fun getHostname() {
        robotActionController?.getHostName()
    }

    fun dockChargingPile() {
        Timber.w("对接充电桩")
        robotActionController?.dockStart()
    }

    fun cancelCharge() {
        Timber.w("取消充电")
        robotActionController?.cancelCharge()
    }

    fun cancelNavigation() {
        Timber.w("取消导航")
        RobotInfo.isNavigating = false
        robotActionController?.cancelNavigation()
    }

    /**
     * 退出标签码对接状态
     */
    fun agvStop() {
        Timber.w("退出标签码对接状态")
        RobotInfo.isQRCodeNavigating = false
        robotActionController?.sendCommand("agv:stop")
    }

    fun getCurrentPosition() {
        robotActionController?.getCurrentPosition()
    }

    fun sysReboot() {
        robotActionController?.sysReboot()
    }

    fun cpuPerformance() {
        robotActionController?.cpuPerformance()
    }

    fun positionAutoUploadControl(report: Boolean) {
        if ((report && lastPositionAutoUploadReport == 1) || (!report && lastPositionAutoUploadReport == 0)) return
        lastPositionAutoUploadReport = if (report) 1 else 0
        Timber.w("坐标上报 ${if (report) "打开" else "关闭"}")
        robotActionController?.positionAutoUploadControl(report)
    }

    fun currentInfoControl(autoReport: Boolean) {
        robotActionController?.currentInfoControl(autoReport)
    }

    fun setSpeed(speed: String) {
        if (speed == lastSpeed) return
        lastSpeed = speed
        robotActionController?.setNavSpeed(speed)
    }

    fun heartBeat() {
        robotActionController?.heartBeat()
    }

    fun getAltitudeState() {
        Timber.w("查询顶升模块位置")
        RobotInfo.getAltitudeState = true
        robotActionController?.sendToBase(0x08, 0x00)
    }

    fun liftUp() {
        Timber.w("顶升控制:抬起")
        RobotInfo.getAltitudeState = false
        RobotInfo.isLifting = true
        robotActionController?.sendToBase(0x06, 0x01)
    }

    fun liftDown() {
        Timber.w("顶升控制:放下")
        RobotInfo.getAltitudeState = false
        RobotInfo.isLifting = true
        robotActionController?.sendToBase(0x06, 0x00)
    }

    /**
     * @param data 0:关闭所有功能
     * 4:顶升io控制
     */
    fun ioControl(data: Int) {
        robotActionController?.sendToBase(0x03, data)
    }

    /**
     * 设置就近停靠
     *
     * @param distance
     */
    fun setTolerance(distance: Int) {
        if (distance == lastTolerance) return
        lastTolerance = distance
        robotActionController?.sendCommand("set_tolerance[$distance]")
    }

    /**
     * 设置机器从二维码离开时的方向和距离
     *
     * @param distance 0: 机器直接离开;
     * 正数: 机器向前移动指定距离后开始导航,浮点型,单位:米;
     * 负数: 机器向后移动指定距离后开始导航,浮点型,单位:米;
     * 默认值: 0.7;
     */
    fun agvMove(distance: Float) {
        robotActionController?.sendCommand("agv_move[$distance]")
    }

    /**
     * 设置机器二维码导航时的方向
     *
     * @param direction 0: 机器倒退着进行二维码导航;
     * 1: 机器正向进行二维码导航;
     * 默认值: 0;
     */
    fun agvMode(direction: Int) {
        robotActionController?.sendCommand("agv_mode[$direction]")
    }


    /**
     * 设置机器宽度(方形低盘)
     *
     * @param length max 1.5
     * @param width  max:1.5
     * 0,0恢复
     */
    fun footprint(length: Float, width: Float) {
        if (lastRobotLength == length && lastRobotWidth == width) return
        lastRobotLength = length
        lastRobotWidth = width
        robotActionController?.sendCommand("footprint[$length,$width]")
    }

    /**
     * 设置机器半径(圆形底盘)
     *
     * @param radius
     */
    fun robotRadius(radius: Float) {
        if (lastRadius == radius) return
        lastRadius = radius
        robotActionController?.sendCommand("robot_radius[$radius]")
    }

    fun getRobotType() {
        robotActionController?.sendCommand("robot_type")
    }

    /**
     * 恢复模式
     *
     * @param open
     */
    fun avoidObstacle(open: Boolean) {
        robotActionController?.sendCommand("avoid_obstacle[${if (open) "on" else "off"}]")
    }

    /**
     * 过滤激光&3D噪点范围
     *
     * @param width 宽度,单位:米
     * 正常导航时设置0.05;
     * 驮运货物时默认0.3,最大1;
     */
    fun lidarMin(width: Float) {
        if (width == lastLidarWidth) return
        lastLidarWidth = width
        robotActionController?.sendCommand("lidar_min[$width]")
    }

    /**
     * 设置全局路径是否考虑临时障碍物
     *
     * @param i 1:考虑 0:不考虑
     */
    fun setGlobalCost(i: Int) {
        if (lastGlobalcost == i) return
        lastGlobalcost = i
        robotActionController?.sendCommand("set_globalcost_p[$i]")
    }

    /**
     * 判断能否导航
     *
     * @param name
     */
    fun getPlanName(name: String) {
        RobotInfo.isGetPlan = true
        robotActionController?.sendCommand("get_plan_name[$name]")
    }

    /**
     * 判断能否导航
     *
     * @param x
     * @param y
     * @param angle
     */
    fun getPlanPoint(position: DoubleArray) {
        RobotInfo.isGetPlan = true
        robotActionController?.sendCommand(
            "get_plan_point${
                position.contentToString().replace(" ", "")
            }"
        )
    }

    fun maxPlanDist(distance: Double) {
        if (distance != lastDistance) {
            lastDistance = distance
            robotActionController?.sendCommand("max_plan_dist[$distance]")
        }
    }

    fun straight(i: Int) {
        robotActionController?.moveRight(i, 0)
    }

    fun modelRequest() {
        isModelRequest = true
        robotActionController?.modelRequest()
    }

    fun getPowerOnTime() {
        robotActionController?.sendCommand("get_poweron_time")
    }
}