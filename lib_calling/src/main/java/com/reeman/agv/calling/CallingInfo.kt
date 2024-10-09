package com.reeman.agv.calling

import android.os.Build
import android.util.Log
import com.reeman.agv.calling.event.CallingTaskEvent
import com.reeman.agv.calling.event.ChargeTaskEvent
import com.reeman.agv.calling.event.NormalTaskEvent
import com.reeman.agv.calling.event.QRCodeTaskEvent
import com.reeman.agv.calling.event.ReturnTaskEvent
import com.reeman.agv.calling.event.RouteTaskEvent
import com.reeman.agv.calling.event.TaskEvent
import com.reeman.agv.calling.exception.StartTaskException
import com.reeman.agv.calling.model.TaskInfo
import com.reeman.agv.calling.setting.ModeCallingSetting
import com.reeman.agv.calling.model.TaskDetails
import com.reeman.agv.calling.model.RemoteTaskModel
import com.reeman.agv.calling.model.HeartBeat
import com.reeman.commons.event.ROSModelEvent
import com.reeman.commons.state.RobotInfo
import com.reeman.commons.state.StartTaskCode
import timber.log.Timber

object CallingInfo {

    var isQRCodeTaskUseCallingButton= false

    //急停状态(0:被按下;1:抬起)
    var emergencyStopSwitchCode = -1
        set(value) {
            field = value
            heartBeatInfo.emergencyButton = value
        }

    //充电状态(3:线充;8:对接中;大于8:对接失败)
    var chargeState = 0
        set(value) {
            field = value
            heartBeatInfo.chargeState = value
        }

    //导航模式(1:导航模式;else:建图模式)
    var mappingMode = 0
        set(value) {
            field = value
            heartBeatInfo.isMapping = value != ROSModelEvent.NAVIGATION_MODEL

        }

    //是否正在自检
    var isSelfChecking = false

    //任务执行状态
    var taskExecutingCode = 0

    //是否低电
    var isLowPower = false
        set(value) {
            field = value
            heartBeatInfo.lowPower = value
        }

    //是否工作时间
    var isWorkingTime = false

    //顶升模块目标位置:1:顶部;0:底部
    var liftModelTargetLocation = 0
        set(value) {
            field = value
            heartBeatInfo.liftModelState = value
        }

    //正在顶升中
    var isLifting = false
        set(value) {
            field = value
            heartBeatInfo.isLifting = value
        }

    //呼叫任务停留倒计时是否结束
    var isCountDownAfterCallingTask = false

    //当前activity是否可以响应任务
    var isCurrentActivityCanTakeRemoteTask = true

    //任务执行时是否可以响应任务
    var isCanTakeTaskDuringTaskExecuting = false

    //呼叫模式设置
    var callingModeSetting: ModeCallingSetting = ModeCallingSetting.getDefault()

    private val tokenList = ArrayList<Pair<String, Long>>()

    /**
     * 更新token列表
     */
    @Synchronized
    fun addToTokenList(token: String) {
        val currentTimeMillis = System.currentTimeMillis()
        val find = try {
            tokenList.find { it.first.isNotBlank() && it.first == token }
        } catch (e: Exception) {
            Timber.w(e, "tokenList异常 $tokenList")
        }
        if (find != null) {
            tokenList.remove(find)
        }
        tokenList.add(Pair(token, currentTimeMillis))
    }

    /**
     * 刷新并返回未超时的token
     */
    fun refreshTokenList(): ArrayList<Pair<String, Long>> {
        val currentTimeMillis = System.currentTimeMillis()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            tokenList.removeIf { currentTimeMillis - it.second > 10_000 }
        } else {
            tokenList.removeAll { currentTimeMillis - it.second > 10_000 }
        }
        return tokenList
    }

    /**
     * 收到移动设备拉取点位或下发的任务前先判断是否在线
     */
    fun isDeviceAlive(token: String?): Boolean {
        return tokenList.find { it.first == token } != null
    }

    fun isReadyForTask() {
        if (!isCanTakeTaskDuringTaskExecuting && taskExecutingCode != 0) throw StartTaskException(
            StartTaskCode.TASK_EXECUTING,
            taskExecutingCode
        )
        if (emergencyStopSwitchCode == 0) throw StartTaskException(
            StartTaskCode.EMERGENCY_STOP_DOWN
        )
        if (chargeState == 3) throw StartTaskException(
            StartTaskCode.AC_CHARGING
        )
        if (mappingMode != 1) throw StartTaskException(
            StartTaskCode.MAPPING
        )
        if (isSelfChecking) throw StartTaskException(
            StartTaskCode.SELF_CHECKING
        )
        if (isLowPower) throw StartTaskException(
            StartTaskCode.LOW_POWER
        )
//        if (!isWorkingTime) throw StartTaskException(StartTaskCode.NOT_WORKING_TIME)
        if (isLifting) throw StartTaskException(
            StartTaskCode.LIFTING
        )
        if (liftModelTargetLocation == 1) throw StartTaskException(
            StartTaskCode.LIFT_MODEL_LOCATION_ERROR
        )
        if (isCountDownAfterCallingTask) throw StartTaskException(
            StartTaskCode.CALLING_TASK_COUNT_DOWN
        )
        if (!isCurrentActivityCanTakeRemoteTask) throw StartTaskException(
            StartTaskCode.NOT_SUPPORT_ONLINE_TASK
        )
    }


    //呼叫任务列表
    val taskDetailsList = mutableListOf<TaskDetails>()

    fun getTaskDetailsByToken(token: String): TaskDetails? {
        return taskDetailsList.find { it.key == token }
    }

    private fun getTargetString(taskEvent: TaskEvent) =
        when (taskEvent) {
            is CallingTaskEvent -> if (RobotInfo.isElevatorMode) {
                "${taskEvent.point.first} - ${taskEvent.point.second}"
            } else {
                taskEvent.point.second
            }

            is NormalTaskEvent -> if (RobotInfo.isElevatorMode) {
                taskEvent.pointList.map { "${it.first}-${it.second}" }
            } else {
                taskEvent.pointList.map { it.second }
            }.toString().replace("[", "").replace("]", "")

            is QRCodeTaskEvent -> if (RobotInfo.isElevatorMode) {
                taskEvent.qrCodePointPairList.map { "${it.first.first}-${it.first.second} -> ${it.second.first}-${it.second.second}" }
            } else {
                taskEvent.qrCodePointPairList.map { "${it.first.second} -> ${it.second.second}" }
            }.toString().replace("[", "").replace("]", "")

            is RouteTaskEvent -> taskEvent.route.routeName
            is ChargeTaskEvent -> taskEvent.point
            is ReturnTaskEvent -> taskEvent.point
        }

    fun getRemoteTaskModelList() =
        taskDetailsList.map { taskDetails ->
            val point = getTargetString(taskDetails.taskEvent)
            RemoteTaskModel(
                point,
                taskDetails.mode,
                taskDetails.firstCallingTime,
                taskDetails.lastCallingTime,
                taskDetails.callingCount
            )
        }


    /**
     * 移除非第一个超时的点位
     * @return 第一个点是否超时
     */
    fun removeAllTimeOutPoint(): Boolean {
        var isFirstPointTimeOut = false
        if (taskDetailsList.isNotEmpty()) {
            val currentTimeMillis = System.currentTimeMillis()
            val first = taskDetailsList.first()
            isFirstPointTimeOut =
                currentTimeMillis - first.lastCallingTime > callingModeSetting.cacheTime * 60 * 1000
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                taskDetailsList.removeIf { currentTimeMillis - it.lastCallingTime > callingModeSetting.cacheTime * 60 * 1000 && it.taskEvent != first.taskEvent }
            } else {
                taskDetailsList.removeAll { currentTimeMillis - it.lastCallingTime > callingModeSetting.cacheTime * 60 * 1000 && it.taskEvent != first.taskEvent }
            }
        }
        updateTaskList()
        return isFirstPointTimeOut
    }

    private fun updateTaskList() {
        heartBeatInfo.taskList = taskDetailsList.map {
            val startTime =
                heartBeatInfo.currentTask?.startTime ?: 0

            TaskInfo(
                it.firstCallingTime,
                startTime,
                it.mode.ordinal,
                it.key,
                getTargetString(it.taskEvent)
            )
        }
    }


    fun isCallingDetailsListEmpty() = taskDetailsList.isEmpty()

    fun addCallingDetails(taskDetails: TaskDetails) {
        if (taskExecutingCode == 3 && !callingModeSetting.openCallingQueue) return
        val existingCallingDetails =
            taskDetailsList.find {
                it.mode == taskDetails.mode &&
                        getTargetString(it.taskEvent) == getTargetString(taskDetails.taskEvent)
            }
        if (existingCallingDetails != null) {
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis - existingCallingDetails.lastCallingTime > 500) {
                existingCallingDetails.callingCount = existingCallingDetails.callingCount + 1
                existingCallingDetails.lastCallingTime = currentTimeMillis
            }
        } else {
            taskDetailsList.add(taskDetails)
        }
        updateTaskList()
    }

    fun getFirstCallingDetails(): TaskDetails? = taskDetailsList.firstOrNull()

    fun removeFirstCallingDetails() {
        if (taskDetailsList.isNotEmpty()) {
            taskDetailsList.removeAt(0)
            updateTaskList()
        }
    }

    fun removeAllCallingPoints() {
        taskDetailsList.clear()
        heartBeatInfo.taskList = ArrayList()
    }

    //mqtt心跳信息
    var heartBeatInfo =
        HeartBeat()

    //呼叫按钮点位配对数据
    var callingButtonMap = mutableMapOf<String, String>()

    var callingButtonWithQRCodeModelTaskMap =
        mutableMapOf<String, List<Pair<Pair<String, String>, Pair<String, String>>>>()

    //梯控模式下呼叫按钮点位数据
    var callingButtonMapWithElevator = mutableMapOf<String, Pair<String, String>>()

    fun getCallingButtonList() =
        callingButtonMap.map { Pair(it.key, Pair("", it.value)) }.toMutableList()

    fun getCallingButtonWithElevatorList() = callingButtonMapWithElevator.toList().toMutableList()

}