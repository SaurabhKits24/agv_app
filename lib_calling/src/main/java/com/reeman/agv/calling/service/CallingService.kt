package com.reeman.agv.calling.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.reeman.agv.calling.CallingInfo
import com.reeman.agv.calling.R
import com.reeman.agv.calling.event.CallingButtonEvent
import com.reeman.agv.calling.event.CallingTaskEvent
import com.reeman.agv.calling.event.CallingTaskQueueUpdateEvent
import com.reeman.agv.calling.event.ChargeTaskEvent
import com.reeman.agv.calling.event.NormalTaskEvent
import com.reeman.agv.calling.event.QRCodeButtonEvent
import com.reeman.agv.calling.event.QRCodeTaskEvent
import com.reeman.agv.calling.event.ReturnTaskEvent
import com.reeman.agv.calling.event.RouteTaskEvent
import com.reeman.agv.calling.event.StartTaskCountDownEvent
import com.reeman.agv.calling.event.UnboundButtonEvent
import com.reeman.agv.calling.exception.NoFindPointException
import com.reeman.agv.calling.exception.NoRouteException
import com.reeman.agv.calling.exception.NotSupportElevatorModeException
import com.reeman.agv.calling.exception.StartTaskException
import com.reeman.agv.calling.model.BaseModel
import com.reeman.agv.calling.model.BaseTaskModel
import com.reeman.agv.calling.model.CallingModePointModel
import com.reeman.agv.calling.model.CallingModeTaskModel
import com.reeman.agv.calling.model.QRCodeModeTaskModel
import com.reeman.agv.calling.model.ResponseModel
import com.reeman.agv.calling.model.TaskDetails
import com.reeman.agv.calling.model.TaskPointModel
import com.reeman.agv.calling.mqtt.MqttClient
import com.reeman.agv.calling.mqtt.Topic
import com.reeman.agv.calling.utils.Code
import com.reeman.agv.calling.utils.PointCheckUtil
import com.reeman.agv.calling.utils.CallingStateManager
import com.reeman.agv.calling.utils.TaskExecutingCode
import com.reeman.commons.constants.Constants
import com.reeman.commons.event.CoreDataEvent
import com.reeman.commons.event.InitiativeLiftingModuleStateEvent
import com.reeman.commons.eventbus.EventBus
import com.reeman.commons.exceptions.ElevatorNetworkNotSettException
import com.reeman.commons.settings.CommutingTimeSetting
import com.reeman.commons.state.NavigationMode
import com.reeman.commons.state.RobotInfo
import com.reeman.commons.state.StartTaskCode
import com.reeman.commons.state.TaskMode
import com.reeman.commons.utils.AESUtil
import com.reeman.commons.utils.AndroidInfoUtil
import com.reeman.commons.utils.LocaleUtil
import com.reeman.commons.utils.SpManager
import com.reeman.commons.utils.TimeUtil
import com.reeman.dao.repository.DbRepository
import com.reeman.dao.repository.entities.RouteWithPoints
import com.reeman.points.exception.ChargingPointCountException
import com.reeman.points.exception.ChargingPointNotMarkedException
import com.reeman.points.exception.ChargingPointPositionErrorException
import com.reeman.points.exception.CurrentMapNotInLegalMapListException
import com.reeman.points.exception.ElevatorPointNotLegalException
import com.reeman.points.exception.MapListEmptyException
import com.reeman.points.exception.NoLegalMapException
import com.reeman.points.exception.PathListEmptyException
import com.reeman.points.exception.PointListEmptyException
import com.reeman.points.exception.RequiredMapNotFoundException
import com.reeman.points.exception.RequiredPointsNotFoundException
import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.model.custom.GenericPointsWithMap
import com.reeman.points.process.PointRefreshProcessingStrategy
import com.reeman.points.process.PointRefreshProcessor
import com.reeman.points.process.callback.RefreshPointDataCallback
import com.reeman.points.process.impl.DeliveryPointsRefreshProcessingStrategy
import com.reeman.points.process.impl.DeliveryPointsWithMapsRefreshProcessingStrategy
import com.reeman.points.process.impl.FixedDeliveryPointsRefreshProcessingStrategy
import com.reeman.points.process.impl.FixedDeliveryPointsWithMapsRefreshProcessingStrategy
import com.reeman.points.process.impl.FixedQRCodePointsRefreshProcessingStrategy
import com.reeman.points.process.impl.FixedQRCodePointsWithMapsRefreshProcessingStrategy
import com.reeman.points.process.impl.QRCodePointsRefreshProcessingStrategy
import com.reeman.points.process.impl.QRCodePointsWithMapsRefreshProcessingStrategy
import com.reeman.points.utils.PointCacheInfo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.security.GeneralSecurityException
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class CallingService : Service(), MqttClient.OnMqttPayloadCallback {

    companion object {
        const val SUCCESS = 0
        const val FETCH_POINT_FAILED = 1
        const val START_TASK_FAILED = 2
    }

    private val gson = GsonBuilder()
        .serializeNulls()
        .create()

    private val taskQueue = LinkedBlockingQueue<() -> Unit>(100)
    private val handler = Handler(Looper.getMainLooper())

    private var disposables = CompositeDisposable()

    @Volatile
    private var isProcessing: Boolean = false
    private fun addTask(task: () -> Unit) {
        taskQueue.offer(task)
        processNextTask()
    }

    @Synchronized
    private fun processNextTask() {
        if (!isProcessing && taskQueue.isNotEmpty()) {
            Timber.w("processNextTask")
            isProcessing = true
            val task = taskQueue.poll()
            task?.invoke()
        }
    }

    private fun unlock() {
        isProcessing = false
        handler.postDelayed({ processNextTask() }, 500)
    }


    private val heartBeat = Runnable {
        try {
            val heartBeatInfo = CallingInfo.heartBeatInfo
            val tokenList = CallingInfo.refreshTokenList()
            Log.i("heartBeatInfo", "$heartBeatInfo , tokenList : $tokenList")
            if (tokenList.isEmpty() || heartBeatInfo.hostname.isEmpty() || heartBeatInfo.hostname == "unknown") return@Runnable
            val mqttClient = MqttClient.getInstance()
            if (!mqttClient.isConnected) return@Runnable
            for (pair in tokenList) {
                heartBeatInfo.token = pair.first
                try {
                    mqttClient.publishSync(
                        Topic.topicRobotHeartBeat(heartBeatInfo.hostname),
                        gson.toJson(heartBeatInfo)
                    )
                    Timber.d("心跳发送成功 : $heartBeatInfo")
                } catch (e: Exception) {
                    Timber.d(e, "心跳发送失败 : $heartBeatInfo")
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "心跳异常")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.w("calling service create")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = createNotification()
            startForeground(1002, notification)
        }
        val languageType = SpManager.getInstance()
            .getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE)
        LocaleUtil.changeAppLanguage(resources, languageType)
        val scheduledExecutorService = Executors.newScheduledThreadPool(1)
        scheduledExecutorService.scheduleWithFixedDelay(heartBeat, 10, 5, TimeUnit.SECONDS)
        val mqttClient = MqttClient.getInstance()
        mqttClient.setCallback(this)
        val disposableCoreDataEvent = CallingStateManager.getCoreDataEvent()
            .observeOn(Schedulers.io())
            .subscribe { handlerCoreDataEvent(it) }
        val disposableMappingModeEvent = CallingStateManager.getMappingModeEvent()
            .observeOn(Schedulers.io())
            .subscribe { handlerModeEvent(it) }
        val disposableSelfCheckEvent = CallingStateManager.getStateSelfCheck()
            .observeOn(Schedulers.io())
            .subscribe { handlerSelfCheckEvent(it) }
        val disposableTaskExecutingEvent = CallingStateManager.getTaskExecutingEvent()
            .observeOn(Schedulers.io())
            .subscribe { handlerTaskExecutingEvent(it) }
        val disposableLowPowerEvent = CallingStateManager.getLowPowerEvent()
            .observeOn(Schedulers.io())
            .subscribe { handlerLowPowerEvent(it) }
        val disposableInitiativeLiftingModuleStateEvent =
            CallingStateManager.getInitiativeLiftingModuleStateEvent()
                .observeOn(Schedulers.io())
                .subscribe { handlerInitiativeLiftingModuleStateResult(it) }
        val disposableCountingDownAfterCallingTaskEvent =
            CallingStateManager.getCountingDownAfterCallingTaskEvent()
                .observeOn(Schedulers.io())
                .subscribe { handlerCountdownAfterCallingTaskEvent(it) }
        val disposableCurrentActivityCanTakeRemoteTaskEvent =
            CallingStateManager.getCurrentActivityCanTakeRemoteTaskEvent()
                .observeOn(Schedulers.io())
                .subscribe { handlerCurrentActivityCanResponseCallingTaskEvent(it) }
        val disposableCanTakeTaskDuringTaskExecutingEvent =
            CallingStateManager.getCanTakeTaskDuringTaskExecuting()
                .observeOn(Schedulers.io())
                .subscribe { handlerCanResponseTaskDuringTaskExecutingEvent(it) }
        val disposableCallingButtonEvent = CallingStateManager.getCallingButtonEvent()
            .observeOn(Schedulers.io())
            .subscribe { handlerCallingButtonEvent(it) }
        val disposableQRCodeButtonEvent = CallingStateManager.getQRCodeButtonEvent()
            .observeOn(Schedulers.io())
            .subscribe { handlerQRCodeButtonEvent(it) }
        val disposableTimeTickEvent = CallingStateManager.getTimeTickEvent()
            .observeOn(Schedulers.io())
            .subscribe { handlerTimeStampEvent(it) }
        val disposableStartTaskCountDownEvent = CallingStateManager.getStartTaskCountDownEvent()
            .observeOn(Schedulers.io())
            .subscribe { handlerTaskCountDownEvent(it) }
        val disposableUnboundButtonEvent = CallingStateManager.getUnboundButtonEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { handlerUnboundButtonEvent(it) }
        disposables.addAll(
            disposableCoreDataEvent,
            disposableMappingModeEvent,
            disposableSelfCheckEvent,
            disposableTaskExecutingEvent,
            disposableLowPowerEvent,
            disposableInitiativeLiftingModuleStateEvent,
            disposableCountingDownAfterCallingTaskEvent,
            disposableCurrentActivityCanTakeRemoteTaskEvent,
            disposableCanTakeTaskDuringTaskExecutingEvent,
            disposableCallingButtonEvent,
            disposableQRCodeButtonEvent,
            disposableTimeTickEvent,
            disposableStartTaskCountDownEvent,
            disposableUnboundButtonEvent
        )
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "calling server"
            val channelName = "calling server"
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, "calling server")
            .setContentTitle("calling server")
            .setContentText("Service is running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    private fun handlerUnboundButtonEvent(event: UnboundButtonEvent) {
        Toast.makeText(
            this,
            getString(R.string.exception_unbound_button, event.key),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handlerCallingButtonEvent(event: CallingButtonEvent) {
        addTask {
            checkCallingTaskPoints(
                CallingModeTaskModel(
                    true,
                    TaskPointModel(event.map, event.point),
                    event.key
                )
            )
        }
    }

    private fun handlerQRCodeButtonEvent(event: QRCodeButtonEvent) {
        addTask {checkQRCodeModeTask(true, event.key, event.qrCodeModeTaskModelPairList)  }
    }

    private fun handlerTimeStampEvent(timestamp: Long) {
        updateWorkTime()
        val isFirstTaskTimeout: Boolean = CallingInfo.removeAllTimeOutPoint()
        Timber.w("isFirstTaskTimeout : $isFirstTaskTimeout, isCountDownAfterCallingTask : ${CallingInfo.isCountDownAfterCallingTask}, taskExecutingCode : ${CallingInfo.taskExecutingCode}")
        if (isFirstTaskTimeout && !CallingInfo.isCountDownAfterCallingTask && CallingInfo.taskExecutingCode != 1) CallingInfo.removeFirstCallingDetails()
        EventBus.sendEvent(CallingTaskQueueUpdateEvent(CallingInfo.taskDetailsList))
    }

    private fun updateWorkTime() {
        val commutingTimeSetting: CommutingTimeSetting = RobotInfo.commutingTimeSetting
        val isWorkingTime = if (commutingTimeSetting.open && TimeUtil.isCurrentInTimeScope(
                commutingTimeSetting.workingTime,
                commutingTimeSetting.afterWorkTime
            )
        ) {
            true
        } else {
            !commutingTimeSetting.open
        }
        if (CallingInfo.isWorkingTime == isWorkingTime) return
        Timber.w("上下班状态变化 : $isWorkingTime")
        CallingInfo.isWorkingTime = isWorkingTime
        addTask {
            checkIsReadyToStartCallingTask()
        }
    }


    private fun handlerCountdownAfterCallingTaskEvent(isCountingDown: Boolean) {
        if (CallingInfo.isCountDownAfterCallingTask == isCountingDown) return
        Timber.w("呼叫任务完成倒计时状态变化 : $isCountingDown")
        CallingInfo.isCountDownAfterCallingTask = isCountingDown
        addTask {
            checkIsReadyToStartCallingTask()
        }
    }

    private fun handlerCoreDataEvent(event: CoreDataEvent) {
        if (CallingInfo.emergencyStopSwitchCode == event.emergencyButton && CallingInfo.chargeState == event.chargeState) return
        Timber.w("急停和充电状态变化,急停 : ${event.emergencyButton} ,充电状态 : ${event.chargeState}")
        CallingInfo.emergencyStopSwitchCode = event.emergencyButton
        CallingInfo.chargeState = event.chargeState
        CallingInfo.heartBeatInfo.level = event.powerData
        addTask {
            checkIsReadyToStartCallingTask()
        }
    }

    private fun handlerModeEvent(model: Int) {
        if (CallingInfo.mappingMode == model) return
        Timber.w("导航模式变化 : $model")
        CallingInfo.mappingMode = model
        addTask {
            checkIsReadyToStartCallingTask()
        }
    }

    private fun handlerSelfCheckEvent(isSelfChecking: Boolean) {
        if (CallingInfo.isSelfChecking == isSelfChecking) return
        Timber.w("自检状态变化 : $isSelfChecking")
        CallingInfo.isSelfChecking = isSelfChecking
        addTask {
            checkIsReadyToStartCallingTask()
        }
    }

    private fun handlerTaskExecutingEvent(code: Int) {
        if (CallingInfo.taskExecutingCode == code) return
        Timber.w("任务执行状态变化 : $code")
        CallingInfo.taskExecutingCode = code
        if (code == TaskExecutingCode.TASK_EXECUTING) {
            CallingInfo.isCanTakeTaskDuringTaskExecuting = false
        }
        addTask {
            checkIsReadyToStartCallingTask()
        }
    }


    private fun handlerLowPowerEvent(isLowPower: Boolean) {
        if (CallingInfo.isLowPower == isLowPower) return
        Timber.w("低电状态变化 : $isLowPower")
        CallingInfo.isLowPower = isLowPower
        addTask {
            checkIsReadyToStartCallingTask()
        }
    }

    private fun handlerInitiativeLiftingModuleStateResult(event: InitiativeLiftingModuleStateEvent) {
        if (CallingInfo.liftModelTargetLocation == event.action && CallingInfo.isLifting == (event.state == 0)) return
        Timber.w(
            "顶升模块状态: 动作 : %s , 状态 : %s",
            if (event.action == 1) "上升" else "下降",
            if (event.state == 1) "完成" else "未完成"
        )
        CallingInfo.liftModelTargetLocation = event.action
        CallingInfo.isLifting = event.state == 0
        addTask {
            checkIsReadyToStartCallingTask()
        }
    }

    private fun handlerCurrentActivityCanResponseCallingTaskEvent(isCurrentActivityCanTakeRemoteTask: Boolean) {
        if (CallingInfo.isCurrentActivityCanTakeRemoteTask == isCurrentActivityCanTakeRemoteTask) return
        Timber.w("是否处于可以响应呼叫任务的activity : $isCurrentActivityCanTakeRemoteTask")
        CallingInfo.isCurrentActivityCanTakeRemoteTask = isCurrentActivityCanTakeRemoteTask
        addTask {
            checkIsReadyToStartCallingTask()
        }
    }

    private fun handlerCanResponseTaskDuringTaskExecutingEvent(isCanTakeTaskDuringTaskExecuting: Boolean) {
        if (CallingInfo.isCanTakeTaskDuringTaskExecuting == isCanTakeTaskDuringTaskExecuting) return
        Timber.w("机器人在任务中,是否可以响应移动端下发的任务 : $isCanTakeTaskDuringTaskExecuting")
        CallingInfo.isCanTakeTaskDuringTaskExecuting = isCanTakeTaskDuringTaskExecuting
    }

    private fun handlerTaskCountDownEvent(event: StartTaskCountDownEvent) {
        val hostname: String = RobotInfo.ROSHostname
        if (event.code == StartTaskCode.TASK_START) {
            startTaskResponse(event.token, getString(R.string.text_start_task, hostname))
        } else {
            val tip = when (event.code) {
                StartTaskCode.NOT_SUPPORT_ONLINE_TASK -> {

                    getString(R.string.exception_current_activity_not_support_online_task, hostname)
                }

                StartTaskCode.EMERGENCY_STOP_DOWN -> {
                    getString(R.string.exception_emergency_stop_down_cannot_start_task, hostname)
                }

                StartTaskCode.AC_CHARGING -> {
                    getString(R.string.exception_robot_ac_charging, hostname)
                }

                StartTaskCode.CANCEL_MANUAL -> {
                    getString(R.string.exception_cancel_manual, hostname)
                }

                StartTaskCode.LOW_POWER -> {
                    getString(R.string.exception_power_low_cannot_start_task, hostname)
                }

                StartTaskCode.TASK_EXECUTING -> {
                    getString(R.string.exception_task_executing, hostname)
                }

                StartTaskCode.EXECUTING_AGAIN_SWITCH_OPENED_IN_ROUTE_TASK -> {
                    getString(
                        R.string.exception_executing_again_switch_opened_in_route_task,
                        hostname
                    )
                }

                StartTaskCode.CURRENT_TASK_MODE_NOT_SUPPORT_RESPONSE_TASK -> {
                    getString(
                        R.string.exception_current_task_mode_not_support_response_task_during_task_executing,
                        hostname
                    )
                }

                else -> ""
            }
            if (!TextUtils.isEmpty(tip)) {
                response(START_TASK_FAILED, event.mode, event.token, tip)
            }
        }
    }


    /**
     * 检查是否可以开始呼叫任务
     */
    private fun checkIsReadyToStartCallingTask() {
        EventBus.sendEvent(CallingTaskQueueUpdateEvent(CallingInfo.taskDetailsList))
        if (CallingInfo.isCallingDetailsListEmpty()) {
            unlock()
            return
        }
        try {
            CallingInfo.isReadyForTask()
            val firstCallingDetails = CallingInfo.getFirstCallingDetails()
            firstCallingDetails?.let {
                EventBus.sendEvent(it.taskEvent)
            }
        } catch (e: StartTaskException) {
            Log.w(this::class.java.simpleName, e)
        } finally {
            unlock()
        }
    }


    override fun onMqttPayload(topic: String, payload: String) {
        if (topic.isBlank() || payload.isBlank()) {
            Timber.w("消息异常")
            return
        }
        val hostname = RobotInfo.ROSHostname
        val callingModeSetting = CallingInfo.callingModeSetting
        val key = callingModeSetting.key
        when {
            topic == Topic.topicPhoneHeartBeat(hostname) -> {
                val baseModel = gson.fromJson(payload, BaseModel::class.java)
                if (!baseModel.token.isNullOrBlank()) {
                    CallingInfo.addToTokenList(baseModel.token!!)
                }
            }

            topic.startsWith(Topic.topicRequestPointsStart(hostname)) -> {
                val baseModel = gson.fromJson(payload, BaseModel::class.java)
                val taskMode: TaskMode = when (topic) {
                    Topic.subscribeRequestCallingModelPoints(hostname) ->
                        TaskMode.MODE_CALLING

                    Topic.subscribeRequestNormalModelPoints(hostname) ->
                        TaskMode.MODE_NORMAL

                    Topic.subscribeRequestRouteModelPoints(hostname) ->
                        TaskMode.MODE_ROUTE

                    Topic.subscribeRequestQRCodeModelPoints(hostname) ->
                        TaskMode.MODE_QRCODE

                    else -> null
                } ?: return
                if (!key.second.contains(baseModel.token)) {
                    detailException(
                        FETCH_POINT_FAILED,
                        taskMode,
                        baseModel.token!!,
                        StartTaskException(
                            StartTaskCode.TOKEN_EXCEPTION
                        )
                    )
                    Timber.w("token非法")
                    return
                }
                if (!CallingInfo.isDeviceAlive(baseModel.token)) {
                    detailException(
                        FETCH_POINT_FAILED,
                        taskMode,
                        baseModel.token!!,
                        StartTaskException(
                            StartTaskCode.DEVICE_OFFLINE
                        )
                    )
                    Timber.w("设备不在线")
                    return
                }
                when (taskMode) {
                    TaskMode.MODE_CALLING -> {
                        fetchCallingModePoints(key.first, baseModel.token!!)
                    }

                    TaskMode.MODE_NORMAL -> {
                        fetchNormalModePoints(key.first, baseModel.token!!)
                    }

                    TaskMode.MODE_ROUTE -> {
                        fetchRouteModePoints(key.first, baseModel.token!!)
                    }

                    else -> {
                        fetchQRCodeModePoints(key.first, baseModel.token!!)
                    }
                }
            }

            topic.startsWith(Topic.topicTaskStart(hostname)) -> {
                try {
                    val baseTaskModel = gson.fromJson(payload, BaseTaskModel::class.java)
                    val taskMode: TaskMode = when (topic) {
                        Topic.subscribeCallingModelTask(hostname) ->
                            TaskMode.MODE_CALLING

                        Topic.subscribeNormalModelTask(hostname) ->
                            TaskMode.MODE_NORMAL

                        Topic.subscribeRouteModelTask(hostname) ->
                            TaskMode.MODE_ROUTE

                        Topic.subscribeQRCodeModelTask(hostname) ->
                            TaskMode.MODE_QRCODE

                        Topic.subscribeChargeModelTask(hostname) ->
                            TaskMode.MODE_CHARGE

                        Topic.subscribeReturnModelTask(hostname) ->
                            TaskMode.MODE_START_POINT

                        else -> null
                    } ?: return
                    if (!key.second.contains(baseTaskModel.token)) {
                        detailException(
                            START_TASK_FAILED, taskMode,
                            baseTaskModel.token!!,
                            StartTaskException(
                                StartTaskCode.TOKEN_EXCEPTION
                            )
                        )
                        Timber.w("token非法")
                        return
                    }
                    if (!CallingInfo.isDeviceAlive(baseTaskModel.token)) {
                        detailException(
                            START_TASK_FAILED,
                            taskMode,
                            baseTaskModel.token!!,
                            StartTaskException(
                                StartTaskCode.DEVICE_OFFLINE
                            )
                        )
                        Timber.w("设备不在线")
                        return
                    }
                    when (taskMode) {
                        TaskMode.MODE_CHARGE -> {
                            addTask {
                                checkChargeOrReturnModeTask(baseTaskModel.token!!, taskMode)
                            }
                        }

                        TaskMode.MODE_START_POINT -> {
                            addTask {
                                checkChargeOrReturnModeTask(baseTaskModel.token!!, taskMode)
                            }
                        }

                        else -> {
                            baseTaskModel.body?.let { body ->
                                val bodyDecrypt = AESUtil.decrypt(key.first, body)
                                if (bodyDecrypt == body) {
                                    detailException(
                                        START_TASK_FAILED,
                                        taskMode,
                                        baseTaskModel.token!!,
                                        StartTaskException(
                                            StartTaskCode.DECRYPT_FAILED
                                        )
                                    )
                                    Timber.w("解密失败,key: ${key.first},data: $body")
                                    return
                                }
                                when (taskMode) {
                                    TaskMode.MODE_CALLING -> {
                                        try {
                                            val taskPointModel =
                                                gson.fromJson(
                                                    bodyDecrypt,
                                                    TaskPointModel::class.java
                                                )
                                            addTask {
                                                checkCallingTaskPoints(
                                                    CallingModeTaskModel(
                                                        false,
                                                        taskPointModel,
                                                        baseTaskModel.token!!
                                                    )
                                                )
                                            }
                                        } catch (e: JsonParseException) {
                                            detailException(
                                                START_TASK_FAILED,
                                                TaskMode.MODE_CALLING,
                                                baseTaskModel.token!!,
                                                StartTaskException(
                                                    StartTaskCode.JSON_SYNTAX_EXCEPTION
                                                )
                                            )
                                            Timber.w(e, "$bodyDecrypt 转TaskPointModel失败")
                                        }

                                    }

                                    TaskMode.MODE_NORMAL -> {
                                        try {
                                            val taskPointModelList =
                                                gson.fromJson<List<TaskPointModel>>(
                                                    bodyDecrypt,
                                                    object :
                                                        TypeToken<List<TaskPointModel>>() {}.type
                                                )
                                            addTask {
                                                checkNormalTaskPoints(
                                                    baseTaskModel.token!!,
                                                    taskPointModelList
                                                )
                                            }
                                        } catch (e: JsonParseException) {
                                            detailException(
                                                START_TASK_FAILED,
                                                TaskMode.MODE_CALLING,
                                                baseTaskModel.token!!,
                                                StartTaskException(
                                                    StartTaskCode.JSON_SYNTAX_EXCEPTION
                                                )
                                            )
                                            Timber.w(e, "$bodyDecrypt 转List<TaskPointModel>失败")
                                        }
                                    }

                                    TaskMode.MODE_ROUTE -> {
                                        addTask {
                                            checkRouteTask(baseTaskModel.token!!, bodyDecrypt)
                                        }
                                    }

                                    TaskMode.MODE_QRCODE -> {
                                        val qrCodeModeTaskModelPairList = Json.decodeFromString<List<Pair<QRCodeModeTaskModel, QRCodeModeTaskModel>>>(bodyDecrypt)
                                        addTask {
                                            checkQRCodeModeTask(
                                                false,
                                                baseTaskModel.token!!,
                                                qrCodeModeTaskModelPairList
                                            )
                                        }
                                    }

                                    else -> {
                                        Timber.w("unknown topic: $topic")
                                    }
                                }
                            }
                        }
                    }

                } catch (e: JsonParseException) {
                    Timber.w(e, "json解析异常")
                }
            }
        }
    }

    private fun addTaskToQueue(taskDetails: TaskDetails) {
        if (CallingInfo.callingModeSetting.openCallingQueue) {
            CallingInfo.addCallingDetails(taskDetails)
            EventBus.sendEvent(CallingTaskQueueUpdateEvent(CallingInfo.taskDetailsList))
        }
    }

    private fun checkChargeOrReturnModeTask(token: String, mode: TaskMode) {
        Timber.w("收到回充/返航任务 token : $token , taskMode : $mode")

        fun startTask(point: String) {
            val taskEvent = if (mode == TaskMode.MODE_CHARGE) {
                ChargeTaskEvent(token, point)
            } else {
                ReturnTaskEvent(token, point)
            }
            addTaskToQueue(
                TaskDetails(
                    false,
                    token,
                    mode,
                    taskEvent
                )
            )
            CallingInfo.isReadyForTask()
            startTaskResponse(
                token,
                getString(R.string.text_will_start_task, RobotInfo.ROSHostname)
            )
            EventBus.sendEvent(
                taskEvent
            )
            unlock()
        }
        PointRefreshProcessor(getPointRefreshProcessingStrategy(),
            object : RefreshPointDataCallback {
                override fun onPointsLoadSuccess(pointList: List<GenericPoint>) {
                    startTask(pointList[0].name)
                }

                override fun onPointsWithMapsLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>) {
                    startTask(
                        if (mode == TaskMode.MODE_CHARGE) {
                            val chargePoint = PointCacheInfo.chargePoint
                            "${chargePoint.first}-${chargePoint.second}"
                        } else {
                            val productionPoint = PointCacheInfo.productionPoint
                            "${productionPoint.first}-${productionPoint.second}"
                        }
                    )
                }

                override fun onThrowable(throwable: Throwable) {
                    detailException(START_TASK_FAILED, mode, token, throwable)
                    unlock()
                }
            }).process(
            checkEnterElevatorPoint = RobotInfo.supportEnterElevatorPoint(),
            pointTypes = if (mode == TaskMode.MODE_CHARGE) {
                listOf(GenericPoint.CHARGE)
            } else {
                listOf(GenericPoint.PRODUCT)
            }
        )
    }

    private fun checkQRCodeModeTask(
        isButtonTask: Boolean,
        token: String,
        qrCodeModeTaskModelPairList: List<Pair<QRCodeModeTaskModel, QRCodeModeTaskModel>>
    ) {
        Timber.w("收到二维码任务 token : $token, qrCodeModeTaskModelPairList : $qrCodeModeTaskModelPairList")
        PointRefreshProcessor(getQRCodeRefreshProcessingStrategy(),
            object : RefreshPointDataCallback {
                override fun onPointsLoadSuccess(pointList: List<GenericPoint>) {
                    PointCheckUtil.filterNonExistentQRCodePoints(
                        qrCodeModeTaskModelPairList,
                        pointList
                    )
                    val qrCodeTaskEvent =
                        QRCodeTaskEvent(isButtonTask, token, qrCodeModeTaskModelPairList.map {
                            Pair(
                                Pair(it.first.map ?: "", it.first.point),
                                Pair(it.second.map ?: "", it.second.point)
                            )
                        })
                    addTaskToQueue(
                        TaskDetails(
                            isButtonTask,
                            token,
                            TaskMode.MODE_QRCODE,
                            qrCodeTaskEvent
                        )
                    )
                    CallingInfo.isReadyForTask()
                    startTaskResponse(
                        token,
                        getString(R.string.text_will_start_task, RobotInfo.ROSHostname)
                    )
                    EventBus.sendEvent(
                        qrCodeTaskEvent
                    )
                    unlock()
                }

                override fun onPointsWithMapsLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>) {
                    PointCheckUtil.filterNonExistentQRCodePointsWithMaps(
                        qrCodeModeTaskModelPairList,
                        pointsWithMapList
                    )
                    val qrCodeTaskEvent =
                        QRCodeTaskEvent(isButtonTask, token, qrCodeModeTaskModelPairList.map {
                            Pair(
                                Pair(it.first.map ?: "", it.first.point),
                                Pair(it.second.map ?: "", it.second.point)
                            )
                        })
                    addTaskToQueue(
                        TaskDetails(
                            isButtonTask,
                            token,
                            TaskMode.MODE_QRCODE,
                            qrCodeTaskEvent
                        )
                    )
                    CallingInfo.isReadyForTask()
                    startTaskResponse(
                        token,
                        getString(R.string.text_will_start_task, RobotInfo.ROSHostname)
                    )
                    EventBus.sendEvent(
                        qrCodeTaskEvent
                    )
                    unlock()
                }

                override fun onThrowable(throwable: Throwable) {
                    detailException(START_TASK_FAILED, TaskMode.MODE_QRCODE, token, throwable)
                    unlock()

                }
            }).process(
            checkEnterElevatorPoint = RobotInfo.supportEnterElevatorPoint(),
            pointTypes = listOf(GenericPoint.AGV_TAG)
        )
    }

    @SuppressLint("CheckResult")
    private fun checkRouteTask(token: String, routeName: String) {
        Timber.w("收到路线任务 token : $token, routeName: $routeName")

        fun checkPoints(route: RouteWithPoints) {
            PointRefreshProcessor(getPointRefreshProcessingStrategy(),
                object : RefreshPointDataCallback {
                    override fun onPointsLoadSuccess(pointList: List<GenericPoint>) {
                        PointCheckUtil.filterNonExistentPathPoints(
                            route.pointsVOList,
                            pointList
                        )
                        val routeTaskEvent = RouteTaskEvent(token, route)
                        addTaskToQueue(
                            TaskDetails(
                                false,
                                token,
                                TaskMode.MODE_ROUTE,
                                routeTaskEvent
                            )
                        )
                        CallingInfo.isReadyForTask()
                        startTaskResponse(
                            token,
                            getString(R.string.text_will_start_task, RobotInfo.ROSHostname)
                        )
                        EventBus.sendEvent(routeTaskEvent)
                        unlock()
                    }

                    override fun onThrowable(throwable: Throwable) {
                        detailException(START_TASK_FAILED, TaskMode.MODE_ROUTE, token, throwable)
                        unlock()
                    }
                }).process(
                checkEnterElevatorPoint = RobotInfo.supportEnterElevatorPoint(),
                pointTypes = listOf(
                    GenericPoint.PRODUCT,
                    GenericPoint.DELIVERY
                )
            )
        }

        if (RobotInfo.isElevatorMode) {
            detailException(
                START_TASK_FAILED,
                TaskMode.MODE_ROUTE,
                token,
                StartTaskException(
                    StartTaskCode.NOT_SUPPORT_ELEVATOR_MODE
                )
            )
            unlock()
            return
        }
        DbRepository.getInstance()
            .getRouteWithPointsByRouteNameAndNavigationMode(routeName, RobotInfo.navigationMode)
            .subscribeWith(object : SingleObserver<RouteWithPoints> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                    Timber.w(e, "查询路线失败")
                    detailException(
                        START_TASK_FAILED,
                        TaskMode.MODE_ROUTE,
                        token,
                        StartTaskException(
                            StartTaskCode.NO_ROUTE
                        )
                    )
                    unlock()
                }

                override fun onSuccess(t: RouteWithPoints) {
                    checkPoints(t)
                    unlock()
                }

            })
    }

    private fun checkNormalTaskPoints(token: String, taskPointModelList: List<TaskPointModel>) {
        Timber.w("收到普通任务 token : $token, taskPointModelList: $taskPointModelList")
        PointRefreshProcessor(getPointRefreshProcessingStrategy(),
            object : RefreshPointDataCallback {
                override fun onPointsLoadSuccess(pointList: List<GenericPoint>) {
                    PointCheckUtil.filterNonExistentPoints(taskPointModelList, pointList)
                    val normalTaskEvent = NormalTaskEvent(
                        token,
                        taskPointModelList.map { Pair(it.map ?: "", it.point) })
                    addTaskToQueue(TaskDetails(false, token, TaskMode.MODE_NORMAL, normalTaskEvent))
                    CallingInfo.isReadyForTask()
                    startTaskResponse(
                        token,
                        getString(R.string.text_will_start_task, RobotInfo.ROSHostname)
                    )
                    EventBus.sendEvent(
                        normalTaskEvent
                    )
                    unlock()
                }

                override fun onPointsWithMapsLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>) {
                    PointCheckUtil.filterNonExistentPointsWithMap(
                        taskPointModelList,
                        pointsWithMapList
                    )
                    val normalTaskEvent = NormalTaskEvent(
                        token,
                        taskPointModelList.map { Pair(it.map ?: "", it.point) })
                    addTaskToQueue(TaskDetails(false, token, TaskMode.MODE_NORMAL, normalTaskEvent))
                    CallingInfo.isReadyForTask()
                    startTaskResponse(
                        token,
                        getString(R.string.text_will_start_task, RobotInfo.ROSHostname)
                    )
                    EventBus.sendEvent(
                        normalTaskEvent
                    )
                    unlock()
                }

                override fun onThrowable(throwable: Throwable) {
                    detailException(START_TASK_FAILED, TaskMode.MODE_NORMAL, token, throwable)
                    unlock()
                }
            }).process(
            checkEnterElevatorPoint = RobotInfo.supportEnterElevatorPoint(),
            pointTypes = listOf(GenericPoint.DELIVERY)
        )
    }

    private fun checkCallingTaskPoints(callingModeTaskModel: CallingModeTaskModel) {
        Timber.d("收到呼叫任务 $callingModeTaskModel")
        val taskPointModel = callingModeTaskModel.taskPointModel
        if (taskPointModel.point.isBlank()) {
            detailException(
                START_TASK_FAILED, TaskMode.MODE_CALLING,
                callingModeTaskModel.token!!,
                StartTaskException(StartTaskCode.TASK_PARAM_ERROR)
            )
            unlock()
            return
        }
        val token = if (callingModeTaskModel.token.isNullOrBlank()) {
            AndroidInfoUtil.getSerialNumber()
        } else {
            callingModeTaskModel.token
        }
        PointRefreshProcessor(getPointRefreshProcessingStrategy(),
            object : RefreshPointDataCallback {
                override fun onPointsLoadSuccess(pointList: List<GenericPoint>) {
                    if (taskPointModel.point !in pointList.map { it.name }) {
                        throw NoFindPointException(listOf(taskPointModel.point))
                    }
                    val callingTaskEvent = CallingTaskEvent(
                        callingModeTaskModel.isButtonCalling,
                        token!!,
                        Pair(
                            callingModeTaskModel.taskPointModel.map ?: "",
                            callingModeTaskModel.taskPointModel.point
                        )
                    )
                    addTaskToQueue(
                        TaskDetails(
                            callingModeTaskModel.isButtonCalling,
                            token,
                            TaskMode.MODE_CALLING,
                            callingTaskEvent
                        )
                    )
                    CallingInfo.isReadyForTask()
                    startTaskResponse(
                        token,
                        getString(R.string.text_will_start_task, RobotInfo.ROSHostname)
                    )
                    EventBus.sendEvent(
                        callingTaskEvent
                    )
                    unlock()
                }

                override fun onPointsWithMapsLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>) {
                    if (taskPointModel.map in pointsWithMapList.map { it.alias }) {
                        if (pointsWithMapList.find { it.alias == taskPointModel.map }?.pointList?.map { it.name }
                                ?.contains(taskPointModel.point) == true) {
                            val callingTaskEvent = CallingTaskEvent(
                                callingModeTaskModel.isButtonCalling,
                                token!!,
                                Pair(
                                    callingModeTaskModel.taskPointModel.map ?: "",
                                    callingModeTaskModel.taskPointModel.point
                                )
                            )
                            addTaskToQueue(
                                TaskDetails(
                                    callingModeTaskModel.isButtonCalling,
                                    token,
                                    TaskMode.MODE_CALLING,
                                    callingTaskEvent
                                )
                            )
                            CallingInfo.isReadyForTask()
                            startTaskResponse(
                                token,
                                getString(R.string.text_will_start_task, RobotInfo.ROSHostname)
                            )
                            EventBus.sendEvent(
                                callingTaskEvent
                            )
                            unlock()
                            return
                        }
                    }
                    throw NoFindPointException(
                        listOf(taskPointModel.map + " : " + taskPointModel.point)
                    )
                }

                override fun onThrowable(throwable: Throwable) {
                    detailException(
                        START_TASK_FAILED,
                        TaskMode.MODE_CALLING,
                        token!!,
                        throwable
                    )
                    unlock()
                }
            }).process(
            checkEnterElevatorPoint = RobotInfo.supportEnterElevatorPoint(),
            pointTypes = arrayListOf(
                GenericPoint.DELIVERY,
                GenericPoint.CHARGE,
                GenericPoint.PRODUCT,
                GenericPoint.AGV_TAG
            )
        )
    }

    /**
     * 获取二维码模式拉点策略
     */
    private fun getQRCodeRefreshProcessingStrategy(): PointRefreshProcessingStrategy {
        return if (RobotInfo.isElevatorMode) {
            if (RobotInfo.navigationMode == NavigationMode.autoPathMode) {
                QRCodePointsWithMapsRefreshProcessingStrategy()
            } else {
                FixedQRCodePointsWithMapsRefreshProcessingStrategy()
            }
        } else {
            if (RobotInfo.navigationMode == NavigationMode.autoPathMode) {
                QRCodePointsRefreshProcessingStrategy()
            } else {
                FixedQRCodePointsRefreshProcessingStrategy()
            }
        }
    }

    /**
     * 获取非二维码模式拉点策略
     */
    private fun getPointRefreshProcessingStrategy(): PointRefreshProcessingStrategy {
        return if (RobotInfo.isElevatorMode) {
            if (RobotInfo.navigationMode == NavigationMode.autoPathMode) {
                DeliveryPointsWithMapsRefreshProcessingStrategy()
            } else {
                FixedDeliveryPointsWithMapsRefreshProcessingStrategy()
            }
        } else {
            if (RobotInfo.navigationMode == NavigationMode.autoPathMode) {
                DeliveryPointsRefreshProcessingStrategy()
            } else {
                FixedDeliveryPointsRefreshProcessingStrategy()
            }
        }
    }

    private fun fetchNormalModePoints(key: String, token: String) {
        PointRefreshProcessor(
            getPointRefreshProcessingStrategy(),
            object : RefreshPointDataCallback {
                override fun onPointsLoadSuccess(pointList: List<GenericPoint>) {
                    val pointsMap = HashMap<String, List<String>>()
                    pointsMap[RobotInfo.currentMapEvent.map] = pointList.map { it.name }
                    try {
                        val body =
                            AESUtil.encrypt(
                                key,
                                gson.toJson(CallingModePointModel(false, pointsMap))
                            )
                        response(SUCCESS, TaskMode.MODE_NORMAL, token, body)
                    } catch (e: GeneralSecurityException) {
                        Timber.w(e, "加密失败")
                    }
                }

                override fun onPointsWithMapsLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>) {
                    val pointsMap =
                        pointsWithMapList.associate { it.alias to it.pointList.map { point -> point.name } }
                    try {
                        val body =
                            AESUtil.encrypt(
                                key,
                                gson.toJson(CallingModePointModel(true, pointsMap))
                            )
                        response(SUCCESS, TaskMode.MODE_NORMAL, token, body)
                    } catch (e: GeneralSecurityException) {
                        Timber.w(e, "加密失败")
                    }
                }

                override fun onThrowable(throwable: Throwable) {
                    detailException(FETCH_POINT_FAILED, TaskMode.MODE_NORMAL, token, throwable)
                }
            }).process(checkEnterElevatorPoint = RobotInfo.supportEnterElevatorPoint())
    }


    private fun fetchCallingModePoints(key: String, token: String) {
        PointRefreshProcessor(
            getPointRefreshProcessingStrategy(),
            object : RefreshPointDataCallback {
                override fun onPointsLoadSuccess(pointList: List<GenericPoint>) {
                    val pointsMap = HashMap<String, List<String>>()
                    pointsMap[RobotInfo.currentMapEvent.map] = pointList.map { it.name }
                    try {
                        val body =
                            AESUtil.encrypt(
                                key,
                                gson.toJson(CallingModePointModel(false, pointsMap))
                            )
                        response(SUCCESS, TaskMode.MODE_CALLING, token, body)
                    } catch (e: GeneralSecurityException) {
                        Timber.w(e, "加密失败")
                    }
                }

                override fun onPointsWithMapsLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>) {
                    val pointsMap =
                        pointsWithMapList.associate { it.alias to it.pointList.map { point -> point.name } }
                    try {
                        val body =
                            AESUtil.encrypt(
                                key,
                                gson.toJson(CallingModePointModel(true, pointsMap))
                            )
                        response(SUCCESS, TaskMode.MODE_CALLING, token, body)
                    } catch (e: GeneralSecurityException) {
                        Timber.w(e, "加密失败")
                    }
                }

                override fun onThrowable(throwable: Throwable) {
                    detailException(FETCH_POINT_FAILED, TaskMode.MODE_CALLING, token, throwable)
                }
            }).process(
            checkEnterElevatorPoint = RobotInfo.supportEnterElevatorPoint(),
            pointTypes = arrayListOf(
                GenericPoint.DELIVERY,
                GenericPoint.CHARGE,
                GenericPoint.PRODUCT
            )
        )
    }

    private fun fetchRouteModePoints(key: String, token: String) {
        if (RobotInfo.isElevatorMode) {
            detailException(
                FETCH_POINT_FAILED,
                TaskMode.MODE_ROUTE,
                token,
                NotSupportElevatorModeException()
            )
            return
        }

        fun fetchRoutes(pointList: List<String>) {
            DbRepository.getInstance().getAllRouteWithPoints(RobotInfo.navigationMode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : SingleObserver<List<RouteWithPoints>> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onError(e: Throwable) {
                        Timber.w(e, "查询本地路线失败")
                        detailException(
                            FETCH_POINT_FAILED,
                            TaskMode.MODE_ROUTE,
                            token,
                            NoRouteException()
                        )
                    }

                    override fun onSuccess(t: List<RouteWithPoints>) {
                        val filter =
                            t.filter { pointList.containsAll(it.pointsVOList.map { it.point }) }
                        if (filter.isEmpty()) {
                            Timber.d("本地路线中存在无效点位")
                            detailException(
                                FETCH_POINT_FAILED,
                                TaskMode.MODE_ROUTE,
                                token,
                                NoRouteException()
                            )
                            return
                        }
                        try {
                            val body =
                                AESUtil.encrypt(key, gson.toJson(filter.map { it.routeName }))
                            response(SUCCESS, TaskMode.MODE_ROUTE, token, body)
                        } catch (e: GeneralSecurityException) {
                            Timber.w(e, "加密失败")
                        }
                    }

                })
        }

        val pointRefreshProcessingStrategy =
            if (RobotInfo.navigationMode == NavigationMode.autoPathMode) {
                DeliveryPointsRefreshProcessingStrategy()
            } else {
                FixedDeliveryPointsRefreshProcessingStrategy()
            }
        PointRefreshProcessor(pointRefreshProcessingStrategy, object : RefreshPointDataCallback {
            override fun onPointsLoadSuccess(pointList: List<GenericPoint>) {
                fetchRoutes(pointList.map { it.name })
            }

            override fun onThrowable(throwable: Throwable) {
                detailException(FETCH_POINT_FAILED, TaskMode.MODE_ROUTE, token, throwable)
            }
        }).process(
            checkEnterElevatorPoint = RobotInfo.supportEnterElevatorPoint(),
            pointTypes = listOf(GenericPoint.DELIVERY, GenericPoint.PRODUCT)
        )
    }

    private fun fetchQRCodeModePoints(key: String, token: String) {
//        if (!RobotInfo.isSpaceShip()|| !RobotInfo.isLiftModelInstalled){
//            detailException(FETCH_POINT_FAILED, TaskMode.MODE_QRCODE, token, throwable)
//            return
//        }
        PointRefreshProcessor(
            getQRCodeRefreshProcessingStrategy(),
            object : RefreshPointDataCallback {
                override fun onPointsLoadSuccess(pointList: List<GenericPoint>) {
                    val pointsMap = HashMap<String, List<String>>()
                    pointsMap[RobotInfo.currentMapEvent.map] = pointList.map { it.name }
                    try {
                        val body =
                            AESUtil.encrypt(
                                key,
                                gson.toJson(CallingModePointModel(false, pointsMap))
                            )
                        response(SUCCESS, TaskMode.MODE_QRCODE, token, body)
                    } catch (e: GeneralSecurityException) {
                        Timber.w(e, "加密失败")
                    }
                }

                override fun onPointsWithMapsLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>) {
                    val pointsMap =
                        pointsWithMapList.associate { it.alias to it.pointList.map { point -> point.name } }
                    try {
                        val body =
                            AESUtil.encrypt(
                                key,
                                gson.toJson(CallingModePointModel(true, pointsMap))
                            )
                        response(SUCCESS, TaskMode.MODE_QRCODE, token, body)
                    } catch (e: GeneralSecurityException) {
                        Timber.w(e, "加密失败")
                    }
                }

                override fun onThrowable(throwable: Throwable) {
                    detailException(FETCH_POINT_FAILED, TaskMode.MODE_QRCODE, token, throwable)
                }
            }).process(
            checkEnterElevatorPoint = RobotInfo.supportEnterElevatorPoint(),
            pointTypes = arrayListOf(GenericPoint.AGV_TAG)
        )
    }

    fun startTaskResponse(token: String, body: String) {
        handler.post {
            Toast.makeText(this, body.replace("${RobotInfo.ROSHostname}:\n", ""), Toast.LENGTH_LONG)
                .show()
        }
        if (!CallingInfo.callingModeSetting.key.second.contains(token)) return
        val mqttClient = MqttClient.getInstance()
        val payload = gson.toJson(ResponseModel(token, body, SUCCESS))
        val topic = Topic.topicStartTaskResponse(RobotInfo.ROSHostname)
        mqttClient.publish(topic, payload)
            .subscribe({ _ ->
                Timber.d(
                    "开始任务响应成功,topic : %s,payload : %s ",
                    topic,
                    payload
                )
            }
            ) { throwable ->
                Timber.w(
                    throwable,
                    "响应失败,topic : %s,payload : %s ",
                    topic,
                    payload
                )
            }
    }

    private fun response(code: Int, taskMode: TaskMode, token: String, body: String) {
        if (code != 0) {
            handler.post {
                Toast.makeText(
                    this,
                    body.replace("${RobotInfo.ROSHostname}:\n", ""),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
        if (!CallingInfo.callingModeSetting.key.second.contains(token)) return
        var topic = ""
        val hostname = RobotInfo.ROSHostname
        if (code in listOf(SUCCESS, Code.FETCH_DATA_ELEVATOR_WARN, Code.FETCH_DATA_POINT_WARN)) {
            when (taskMode) {
                TaskMode.MODE_CALLING -> {
                    topic = Topic.publishCallingModelPoints(hostname)
                }

                TaskMode.MODE_NORMAL -> {
                    topic = Topic.publishNormalModelPoints(hostname)
                }

                TaskMode.MODE_QRCODE -> {
                    topic = Topic.publishQRCodeModelPoints(hostname)
                }

                TaskMode.MODE_ROUTE -> {
                    topic = Topic.publishRouteModelPoints(hostname)
                }

                else -> {

                }
            }
        } else {
            topic = Topic.topicStartTaskResponse(hostname)
        }
        val mqttClient = MqttClient.getInstance()
        val payload = gson.toJson(ResponseModel(token, body, code))
        mqttClient.publish(topic, payload)
            .subscribe({ _ ->
                Timber.d(
                    "响应成功,topic : %s,payload : %s ",
                    topic,
                    payload
                )
            }
            ) { throwable ->
                Timber.w(
                    throwable,
                    "响应失败,topic : %s,payload : %s ",
                    topic,
                    payload
                )
            }
    }

    private fun detailException(
        code: Int,
        taskMode: TaskMode,
        token: String,
        throwable: Throwable
    ) {
        Timber.w(throwable, "拉取数据失败")
        val hostname = RobotInfo.ROSHostname
        val mCode = when (code) {
            START_TASK_FAILED -> {
                when (throwable) {
                    is StartTaskException -> Code.START_TASK_OTHER_WARN
                    is ElevatorNetworkNotSettException -> Code.START_TASK_ELEVATOR_WARN
                    is NotSupportElevatorModeException -> Code.START_TASK_ELEVATOR_WARN
                    is RequiredMapNotFoundException -> Code.START_TASK_ELEVATOR_WARN
                    else -> Code.START_TASK_POINT_WARN
                }
            }

            FETCH_POINT_FAILED -> {
                when (throwable) {
                    is ElevatorNetworkNotSettException -> Code.FETCH_DATA_ELEVATOR_WARN
                    is NotSupportElevatorModeException -> Code.FETCH_DATA_ELEVATOR_WARN
                    is RequiredMapNotFoundException -> Code.FETCH_DATA_ELEVATOR_WARN
                    else -> Code.FETCH_DATA_POINT_WARN
                }
            }

            else -> SUCCESS
        }
        val tip: String = when (throwable) {
            is StartTaskException -> when (throwable.code) {
                StartTaskCode.TASK_EXECUTING -> when (throwable.reasonCode) {
                    4 -> getString(R.string.exception_last_task_abnormal_finish, hostname)
                    3 -> getString(R.string.exception_task_executing, hostname)
                    else -> getString(R.string.exception_task_will_start, hostname)
                }

                StartTaskCode.EMERGENCY_STOP_DOWN -> getString(
                    R.string.exception_emergency_stop_down_cannot_start_task,
                    hostname
                )

                StartTaskCode.AC_CHARGING -> getString(
                    R.string.exception_robot_ac_charging,
                    hostname
                )

                StartTaskCode.MAPPING -> getString(R.string.exception_mapping, hostname)
                StartTaskCode.SELF_CHECKING -> getString(R.string.exception_self_checking, hostname)
                StartTaskCode.LOW_POWER -> getString(
                    R.string.exception_power_low_cannot_start_task,
                    hostname
                )

                StartTaskCode.LIFTING -> getString(R.string.exception_is_lifting, hostname)
                StartTaskCode.LIFT_MODEL_LOCATION_ERROR -> getString(
                    R.string.exception_lift_model_not_reset,
                    hostname
                )

                StartTaskCode.CALLING_TASK_COUNT_DOWN -> getString(
                    R.string.exception_task_executing,
                    hostname
                )

                StartTaskCode.NOT_SUPPORT_ONLINE_TASK -> getString(
                    R.string.exception_current_activity_not_support_online_task,
                    hostname
                )

                StartTaskCode.DECRYPT_FAILED -> getString(
                    R.string.exception_decrypt_failed_please_repair,
                    hostname
                )

                StartTaskCode.TASK_PARAM_ERROR -> getString(
                    R.string.exception_param_error,
                    hostname
                )

                StartTaskCode.TOKEN_EXCEPTION -> getString(
                    R.string.exception_can_not_find_token,
                    hostname
                )

                StartTaskCode.NO_ROUTE -> getString(R.string.exception_not_found_route, hostname)
                StartTaskCode.JSON_SYNTAX_EXCEPTION -> getString(
                    R.string.exception_json_syntax_exception,
                    hostname
                )

                StartTaskCode.DEVICE_OFFLINE -> getString(
                    if (code == 1) R.string.exception_device_offline_cannot_get_point else R.string.exception_device_offline_cannot_start_task,
                    hostname
                )

                StartTaskCode.NOT_SUPPORT_ELEVATOR_MODE -> getString(
                    R.string.exception_not_support_elevator_mode,
                    hostname
                )

                else -> ""
            }

            is ElevatorNetworkNotSettException -> when {
                throwable.isOutSideNetworkNotSet && throwable.isInsideNetWorkNotSet -> getString(
                    R.string.exception_please_choose_inside_network_and_outside_network,
                    hostname
                )

                throwable.isInsideNetWorkNotSet -> getString(
                    R.string.exception_please_choose_inside_network,
                    hostname
                )

                else -> getString(R.string.exception_please_choose_outside_network, hostname)
            }

            is NotSupportElevatorModeException -> getString(
                R.string.exception_not_support_elevator_mode,
                hostname
            )

            is NoRouteException -> getString(R.string.exception_get_route_failed, hostname)
            is NoFindPointException -> getString(
                R.string.exception_not_found_points,
                hostname,
                throwable.points.toString()
            )

            is RequiredMapNotFoundException -> when {
                throwable.isProductionPointMapSelected -> getString(
                    R.string.exception_not_choose_charging_point_map,
                    hostname
                )

                else -> getString(
                    R.string.exception_not_choose_production_point_map,
                    hostname
                )

            }

            is NoLegalMapException -> getString(
                R.string.exception_loaded_success_map_illegal,
                hostname
            )

            is ElevatorPointNotLegalException -> {
                val mapList = throwable.mapList
                val stringBuilder = StringBuilder()

                mapList.forEachIndexed { index, entry ->
                    val (first, second) = entry
                    stringBuilder.append(
                        getString(
                            R.string.exception_map_not_legal,
                            hostname,
                            first
                        )
                    ).append("\n")

                    val exceptions = arrayOf(
                        R.string.exception_not_mark_waiting_elevator_point to R.string.exception_check_more_than_one_waiting_elevator_point,
                        R.string.exception_not_mark_enter_elevator_point to R.string.exception_check_more_than_one_enter_elevator_point,
                        R.string.exception_not_mark_inside_elevator_point to R.string.exception_check_more_than_one_inside_elevator_point,
                        R.string.exception_not_mark_leave_elevator_point to R.string.exception_check_more_than_one_leave_elevator_point
                    )

                    var firstException = true

                    second.forEachIndexed { idx, count ->
                        if (count == 0) {
                            if (!firstException) {
                                stringBuilder.append(", ")
                            }
                            stringBuilder.append(getString(exceptions[idx].first))
                            firstException = false
                        } else if (count > 1) {
                            if (!firstException) {
                                stringBuilder.append(", ")
                            }
                            stringBuilder.append(getString(exceptions[idx].second, count))
                            firstException = false
                        }
                    }

                    if (index == mapList.size - 1) {
                        stringBuilder.append(";")
                    } else {
                        stringBuilder.append(".")
                    }
                    stringBuilder.append("\n")
                }
                stringBuilder.toString()
            }

            is CurrentMapNotInLegalMapListException -> getString(
                R.string.exception_current_map_not_in_legal_map_list,
                hostname
            )

            is RequiredPointsNotFoundException -> when {
                !throwable.isChargePointMarked && !throwable.isProductionPointMarked -> getString(
                    R.string.exception_not_mark_charge_point_and_product_point,
                    hostname
                )

                !throwable.isProductionPointMarked -> getString(
                    R.string.exception_not_mark_product_point,
                    hostname
                )

                else -> getString(R.string.exception_not_mark_charge_point)
            }

            is ChargingPointPositionErrorException -> getString(
                R.string.exception_check_charge_point_position_error,
                hostname
            )

            is ChargingPointNotMarkedException -> getString(
                R.string.exception_not_mark_charge_point_in_mark_point_page,
                hostname
            )

            is ChargingPointCountException -> {
                if (throwable.count == 0) {
                    getString(R.string.exception_not_mark_charge_point, hostname)
                } else {
                    getString(
                        R.string.exception_check_charge_point_count_error,
                        if (RobotInfo.navigationMode == NavigationMode.autoPathMode) {
                            getString(R.string.text_auto_model)
                        } else {
                            getString(R.string.text_fix_model)
                        },
                        throwable.count
                    )
                }
            }

            is PathListEmptyException -> getString(R.string.exception_not_mark_path, hostname)
            is PointListEmptyException -> getString(R.string.exception_not_mark_point, hostname)
            is MapListEmptyException -> getString(R.string.exception_not_find_map, hostname)
            else -> {
                getString(R.string.exception_no_cache_points, hostname)
            }
        }
        response(mCode, taskMode, token, tip)
    }


}