package com.reeman.agv.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.reeman.agv.BuildConfig
import com.reeman.agv.R
import com.reeman.agv.base.BaseActivity
import com.reeman.agv.base.BaseApplication.mApp
import com.reeman.agv.calling.button.CallingHelper.isStart
import com.reeman.agv.calling.button.CallingHelper.start
import com.reeman.agv.calling.event.MqttConnectionEvent
import com.reeman.agv.calling.mqtt.MqttClient
import com.reeman.agv.calling.utils.CallingStateManager
import com.reeman.agv.calling.utils.TaskExecutingCode
import com.reeman.agv.constants.Errors
import com.reeman.agv.contract.MainContract
import com.reeman.agv.fragments.main.MainContentFragment
import com.reeman.agv.fragments.main.MainContentFragment.OnMainContentClickListener
import com.reeman.agv.fragments.main.ModeNormalFragment
import com.reeman.agv.fragments.main.ModeNormalFragment.ModeNormalClickListener
import com.reeman.agv.fragments.main.ModeNormalWithMapFragment
import com.reeman.agv.fragments.main.ModeNormalWithMapFragment.ModeNormalWithMapClickListener
import com.reeman.agv.fragments.main.ModeQRCodeFragment
import com.reeman.agv.fragments.main.ModeQRCodeWithMapFragment
import com.reeman.agv.fragments.main.ModeRouteAttributesEditFragment
import com.reeman.agv.fragments.main.ModeRouteFragment
import com.reeman.agv.fragments.main.ModeRoutePointEditFragment
import com.reeman.agv.fragments.main.listener.ModeQRCodeClickListener
import com.reeman.agv.fragments.main.listener.OnGreenButtonClickListener
import com.reeman.agv.presenter.impl.MainPresenter
import com.reeman.agv.request.notifier.Notifier
import com.reeman.agv.request.notifier.NotifyConstant
import com.reeman.agv.utils.ScreenUtils
import com.reeman.agv.utils.VoiceHelper
import com.reeman.agv.widgets.EasyDialog
import com.reeman.agv.widgets.EasyDialog.OnTimeStampListener
import com.reeman.agv.widgets.FloatingCountdown
import com.reeman.commons.constants.Constants
import com.reeman.commons.event.AndroidNetWorkEvent
import com.reeman.commons.event.ApplyMapEvent
import com.reeman.commons.event.GreenButtonEvent
import com.reeman.commons.event.HostnameEvent
import com.reeman.commons.event.InitiativeLiftingModuleStateEvent
import com.reeman.commons.event.RobotTypeEvent
import com.reeman.commons.event.TimeStampEvent
import com.reeman.commons.eventbus.EventBus
import com.reeman.commons.model.request.Msg
import com.reeman.commons.state.RobotInfo
import com.reeman.commons.state.TaskMode
import com.reeman.commons.utils.ClickHelper
import com.reeman.commons.utils.ClickHelper.OnFastClickListener
import com.reeman.commons.utils.SpManager
import com.reeman.commons.utils.TimeUtil
import com.reeman.commons.utils.WIFIUtils
import com.reeman.dao.repository.DbRepository
import com.reeman.dao.repository.entities.CrashNotify
import com.reeman.dao.repository.entities.RouteWithPoints
import com.reeman.points.model.custom.GenericPoint
import com.reeman.points.model.custom.GenericPointsWithMap
import com.reeman.ros.ROSController
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.Date

class MainActivity : BaseActivity(), MainContract.View, OnFastClickListener,
    OnMainContentClickListener {
    private var isFirstEnter = true
    private lateinit var presenter: MainPresenter
    private lateinit var tvHostname: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvBattery: TextView
    private lateinit var tvSSID: TextView
    private lateinit var tvCall: TextView
    private lateinit var tvCallButton: TextView
    private lateinit var ivHome: AppCompatImageView
    private lateinit var layoutHeader: LinearLayout
    private lateinit var clickHelper: ClickHelper
    private var isAutoWork = false
    private var androidWifiConnectCount = 0
    override fun initData() {
        presenter = MainPresenter(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            ScreenUtils.hideBottomUIMenu(this)
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun initCustomView() {
        clickHelper = ClickHelper(this)
        tvHostname = `$`(R.id.tv_hostname)
        tvTime = `$`(R.id.tv_time)
        tvBattery = `$`(R.id.tv_battery)
        tvSSID = `$`(R.id.tv_ssid)
        ivHome = `$`(R.id.iv_home)
        tvCall = `$`(R.id.tv_call)
        layoutHeader = `$`(R.id.layout_header)
        tvCallButton = `$`(R.id.tv_call_button)
        tvHostname.setOnClickListener { clickHelper.fastClick() }
        ivHome.setOnClickListener(this)
        switchMainContentFragment()
    }

    private fun switchMainContentFragment() {
        ivHome.visibility = View.INVISIBLE
        val mainContentFragment = MainContentFragment(this)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
            .replace(R.id.main_fragment_view, mainContentFragment).commit()
    }

    private fun switchModeNormalFragment(pointList: List<GenericPoint>) {
        val modeNormalFragment = ModeNormalFragment(pointList, modeNormalClickListener)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
            .replace(R.id.main_fragment_view, modeNormalFragment).commit()
        ivHome.postDelayed({ ivHome.visibility = View.VISIBLE }, 100)
    }

    private fun switchModeNormalWithMapFragment(
        pointsWithMapList: List<GenericPointsWithMap>
    ) {
        val modeNormalWithMapFragment = ModeNormalWithMapFragment(
            pointsWithMapList,
            modeNormalWithMapClickListener
        )
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
            .replace(R.id.main_fragment_view, modeNormalWithMapFragment).commit()
        ivHome.postDelayed({ ivHome.visibility = View.VISIBLE }, 100)
    }

    private fun switchModeQRCodeFragment(pointList: List<GenericPoint>) {
        val modeQRCodeFragment = ModeQRCodeFragment(pointList, modeQRCodeClickListener)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
            .replace(R.id.main_fragment_view, modeQRCodeFragment).commit()
        ivHome.postDelayed({ ivHome.visibility = View.VISIBLE }, 100)
    }

    private fun switchModeQRCodeWithMapFragment(
        pointsWithMapList: List<GenericPointsWithMap>
    ) {
        val modeQRCodeWithMapFragment = ModeQRCodeWithMapFragment(
            pointsWithMapList,
            modeQRCodeClickListener
        )
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
            .replace(R.id.main_fragment_view, modeQRCodeWithMapFragment).commit()
        ivHome.postDelayed({ ivHome.visibility = View.VISIBLE }, 100)
    }

    private fun switchModeRouteFragment(
        isEditMode: Boolean
    ) {
        val modeRouteFragment = ModeRouteFragment(isEditMode, modeRouteClickListener)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
            .replace(R.id.main_fragment_view, modeRouteFragment).commit()
        layoutHeader.postDelayed({ layoutHeader.visibility = View.VISIBLE }, 200)
        ivHome.postDelayed({ ivHome.visibility = View.VISIBLE }, 100)

    }

    private fun switchModeRouteEditPointFragment(routeWithPoints: RouteWithPoints) {
        val modeRouteEditPointFragment =
            ModeRoutePointEditFragment(RouteWithPoints(routeWithPoints), modeRoutePointEditListener)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
            .replace(R.id.main_fragment_view, modeRouteEditPointFragment).commit()
    }

    private fun switchModeRouteAttributesEditFragment(
        routeWithPoints: RouteWithPoints
    ) {
        val modeRouteAttributesEditFragment = ModeRouteAttributesEditFragment(
            routeWithPoints,
            modeRouteAttributesEditClickListener
        )

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
            .replace(R.id.main_fragment_view, modeRouteAttributesEditFragment).commit()
        layoutHeader.visibility = View.GONE
    }

    private val modeRoutePointEditListener =
        object : ModeRoutePointEditFragment.OnModeRoutePointEditListener {
            override fun onReturnClick(routeWithPoints: RouteWithPoints) {
                switchModeRouteAttributesEditFragment(routeWithPoints)
            }

        }

    private val modeRouteAttributesEditClickListener = object :
        ModeRouteAttributesEditFragment.ModeRouteAttributesEditClickListener {

        override fun onReturnClick() {
            switchModeRouteFragment(true)
        }

        override fun onEditPoints(routeWithPoints: RouteWithPoints) {
            switchModeRouteEditPointFragment(routeWithPoints)
        }


    }

    private val modeRouteClickListener = object : ModeRouteFragment.ModeRouteClickListener {

        override fun onGetRouteFailed() {
            switchMainContentFragment()
        }

        override fun onAddClick(routeWithPoints: RouteWithPoints) {
            switchModeRouteAttributesEditFragment(routeWithPoints)
        }

        override fun onEditClick(routeWithPoints: RouteWithPoints) {
            switchModeRouteAttributesEditFragment(routeWithPoints)
        }

        override fun onStart(routeWithPoints: RouteWithPoints) {
            val modeRouteSetting = RobotInfo.modeRouteSetting
            if (!modeRouteSetting.startTaskCountDownSwitch) {
                if (!canStartTask()) return
                presenter.startRouteModeTask(this@MainActivity, routeWithPoints)
                return
            }
            if (!canStartTask()) return
            EasyDialog.getInstance(this@MainActivity).warnWithScheduledUpdateDetail(
                getString(
                    R.string.text_will_start_task_after_count_down,
                    modeRouteSetting.startTaskCountDownTime
                ),
                R.string.text_start_right_now,
                R.string.text_cancel,
                { dialog: Dialog, id: Int ->
                    dialog.dismiss()
                    RobotInfo.isCountdownToTask = false
                    if (id == R.id.btn_confirm) {
                        if (!canStartTask()) return@warnWithScheduledUpdateDetail
                        presenter.startRouteModeTask(this@MainActivity, routeWithPoints)
                    } else {
                        CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.FREE)
                    }
                },
                object : OnTimeStampListener {
                    override fun onTimestamp(
                        title: TextView,
                        content: TextView,
                        cancelBtn: Button,
                        neutralBtn: Button,
                        confirmBtn: Button,
                        current: Int
                    ) {
                        content.text = getString(
                            R.string.text_will_start_task_after_count_down,
                            modeRouteSetting.startTaskCountDownTime - current
                        )
                    }

                    override fun onTimeOut(dialog: EasyDialog) {
                        dialog.dismiss()
                        RobotInfo.isCountdownToTask = false
                        if (!canStartTask()) return
                        presenter.startRouteModeTask(this@MainActivity, routeWithPoints)
                    }
                },
                1000,
                modeRouteSetting.startTaskCountDownTime * 1000
            )
            RobotInfo.isCountdownToTask = true
            CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN)
        }
    }


    private val modeQRCodeClickListener = ModeQRCodeClickListener { qrCodeModelList ->
        val modeQRCodeSetting = RobotInfo.modeQRCodeSetting
        if (!modeQRCodeSetting.startTaskCountDownSwitch) {
            startQRCodeModeTask(qrCodeModelList)
            return@ModeQRCodeClickListener
        }
        if (!canStartTask()) return@ModeQRCodeClickListener
        EasyDialog.getInstance(this@MainActivity).warnWithScheduledUpdateDetail(
            getString(
                R.string.text_will_start_task_after_count_down,
                modeQRCodeSetting.startTaskCountDownTime
            ),
            R.string.text_start_right_now,
            R.string.text_cancel,
            { dialog: Dialog, id: Int ->
                dialog.dismiss()
                RobotInfo.isCountdownToTask = false
                if (id == R.id.btn_confirm) {
                    startQRCodeModeTask(qrCodeModelList)
                } else {
                    CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.FREE)
                }
            },
            object : OnTimeStampListener {
                override fun onTimestamp(
                    title: TextView,
                    content: TextView,
                    cancelBtn: Button,
                    neutralBtn: Button,
                    confirmBtn: Button,
                    current: Int
                ) {
                    content.text = getString(
                        R.string.text_will_start_task_after_count_down,
                        modeQRCodeSetting.startTaskCountDownTime - current
                    )
                }

                override fun onTimeOut(dialog: EasyDialog) {
                    dialog.dismiss()
                    RobotInfo.isCountdownToTask = false
                    startQRCodeModeTask(qrCodeModelList)
                }
            },
            1000,
            modeQRCodeSetting.startTaskCountDownTime * 1000
        )
        RobotInfo.isCountdownToTask = true
        CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN)
    }

    private fun startQRCodeModeTask(qrCodeModelList: List<Pair<Pair<String, String>, Pair<String, String>>>) {
        if (!canStartTask()) return
        if (RobotInfo.liftModelState == 1) {
            EasyDialog.getInstance(this@MainActivity)
                .warn(getString(R.string.text_check_altitude_up)) { dialog: Dialog, id: Int ->
                    if (id == R.id.btn_confirm) {
                        ROSController.ioControl(4)
                        mHandler.postDelayed({
                            if (RobotInfo.isEmergencyButtonDown) {
                                VoiceHelper.play("voice_scram_stop_turn_on")
                                EasyDialog.getInstance(this@MainActivity)
                                    .warnError(getString(R.string.voice_scram_stop_turn_on))
                                return@postDelayed
                            }
                            ROSController.liftDown()
                            callingInfo.isLifting = true
                            EasyDialog.getLoadingInstance(this@MainActivity)
                                .loading(getString(R.string.text_pickup_model_resetting))
                        }, 200)
                    }
                    dialog.dismiss()
                }
            CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.FREE)
            return
        }
        presenter.startQRCodeModeTask(this@MainActivity, qrCodeModelList)
    }

    private val modeNormalWithMapClickListener = ModeNormalWithMapClickListener { points ->
        val modeNormalSetting = RobotInfo.modeNormalSetting
        if (!modeNormalSetting.startTaskCountDownSwitch) {
            if (!canStartTask()) return@ModeNormalWithMapClickListener
            presenter.startNormalModeTask(this@MainActivity, points)
            return@ModeNormalWithMapClickListener
        }
        if (!canStartTask()) return@ModeNormalWithMapClickListener
        EasyDialog.getInstance(this@MainActivity).warnWithScheduledUpdateDetail(
            getString(
                R.string.text_will_start_task_after_count_down,
                modeNormalSetting.startTaskCountDownTime
            ),
            R.string.text_start_right_now,
            R.string.text_cancel,
            { dialog: Dialog, id: Int ->
                dialog.dismiss()
                RobotInfo.isCountdownToTask = false
                if (id == R.id.btn_confirm) {
                    if (!canStartTask()) return@warnWithScheduledUpdateDetail
                    presenter.startNormalModeTask(this@MainActivity, points)
                } else {
                    CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.FREE)
                }
            },
            object : OnTimeStampListener {
                override fun onTimestamp(
                    title: TextView,
                    content: TextView,
                    cancelBtn: Button,
                    neutralBtn: Button,
                    confirmBtn: Button,
                    current: Int
                ) {
                    content.text = getString(
                        R.string.text_will_start_task_after_count_down,
                        modeNormalSetting.startTaskCountDownTime - current
                    )
                }

                override fun onTimeOut(dialog: EasyDialog) {
                    dialog.dismiss()
                    RobotInfo.isCountdownToTask = false
                    if (!canStartTask()) return
                    presenter.startNormalModeTask(this@MainActivity, points)
                }
            },
            1000,
            modeNormalSetting.startTaskCountDownTime * 1000
        )
        RobotInfo.isCountdownToTask = true
        CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN)
    }
    private val modeNormalClickListener = ModeNormalClickListener { points ->
        val modeNormalSetting = RobotInfo.modeNormalSetting
        if (!modeNormalSetting.startTaskCountDownSwitch) {
            if (!canStartTask()) return@ModeNormalClickListener
            presenter.startNormalModeTask(this@MainActivity, points)
            return@ModeNormalClickListener
        }
        if (!canStartTask()) return@ModeNormalClickListener
        EasyDialog.getInstance(this@MainActivity).warnWithScheduledUpdateDetail(
            getString(
                R.string.text_will_start_task_after_count_down,
                modeNormalSetting.startTaskCountDownTime
            ),
            R.string.text_start_right_now,
            R.string.text_cancel,
            { dialog: Dialog, id: Int ->
                dialog.dismiss()
                RobotInfo.isCountdownToTask = false
                if (id == R.id.btn_confirm) {
                    if (!canStartTask()) return@warnWithScheduledUpdateDetail
                    presenter.startNormalModeTask(this@MainActivity, points)
                } else {
                    CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.FREE)
                }
            },
            object : OnTimeStampListener {
                override fun onTimestamp(
                    title: TextView,
                    content: TextView,
                    cancelBtn: Button,
                    neutralBtn: Button,
                    confirmBtn: Button,
                    current: Int
                ) {
                    content.text = getString(
                        R.string.text_will_start_task_after_count_down,
                        modeNormalSetting.startTaskCountDownTime - current
                    )
                }

                override fun onTimeOut(dialog: EasyDialog) {
                    dialog.dismiss()
                    RobotInfo.isCountdownToTask = false
                    if (!canStartTask()) return
                    presenter.startNormalModeTask(this@MainActivity, points)
                }
            },
            1000,
            modeNormalSetting.startTaskCountDownTime * 1000
        )
        RobotInfo.isCountdownToTask = true
        CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN)
    }

    private fun refreshState() {
        tvHostname.text = RobotInfo.ROSHostname
        tvTime.text = TimeUtil.formatHourAndMinute(Date())
        tvBattery.text = String.format("%s%%", RobotInfo.powerLevel)
        var connectWifiSSID = WIFIUtils.getConnectWifiSSID(this)
        if ("" == connectWifiSSID) connectWifiSSID = getString(R.string.text_not_connected)
        tvSSID.text = connectWifiSSID
    }

    override fun onRestart() {
        super.onRestart()
        isFirstEnter = false
    }

    @SuppressLint("CheckResult")
    override fun onResume() {
        super.onResume()
        refreshState()
        ROSController.getHostIP()
        ROSController.modelRequest()
        ROSController.getHostname()
        ROSController.getCurrentMap()
        ROSController.getRobotType()
        ROSController.positionAutoUploadControl(true)
        if (isFirstEnter) {
            ROSController.heartBeat()
            mHandler.postDelayed(chargeRunnable, 10000)
            CallingStateManager.setTimeTickEvent(System.currentTimeMillis())
        }
        if (!isStart()) {
            try {
                start()
                tvCallButton.visibility = View.VISIBLE
            } catch (e: Exception) {
                tvCallButton.visibility = View.GONE
                Timber.w(e, "打开呼叫串口失败")
            }
        } else {
            tvCallButton.visibility = View.VISIBLE
        }
        updateCallingMap()
        if (!BuildConfig.DEBUG && WIFIUtils.isNetworkConnected(mApp)) {
            var wifiSSID = WIFIUtils.getConnectWifiSSID(mApp)
            if (wifiSSID.isBlank()) wifiSSID = getString(R.string.text_not_connected)
            if (null != wifiSSID && wifiSSID == RobotInfo.ROSWifi) {
                DbRepository.getInstance().allCrashNotify
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({ crashNotifies: List<CrashNotify> ->
                        if (crashNotifies.isNotEmpty()) {
                            val crashNotify = crashNotifies[0]
                            val notify2 = Notifier.notify2(
                                Msg(
                                    NotifyConstant.SYSTEM_NOTIFY,
                                    "application crash(应用崩溃)",
                                    crashNotify.notify,
                                    RobotInfo.ROSHostname
                                )
                            )
                            notify2?.subscribe({ _: Map<String?, Any?>? ->
                                Timber.w("上传crash日志成功")
                                DbRepository.getInstance().deleteNotify(crashNotify.id)
                            }) { throwable: Throwable? -> Timber.w(throwable, "上传crash日志失败") }
                        }
                    }) { throwable: Throwable? ->
                        Timber.tag("selectCrash").w(throwable, "查询本地通知失败")
                    }
            }
        }
        registerObservers()
    }

    private fun registerObservers() {
        EventBus.registerObserver(this, object : EventBus.EventObserver<AndroidNetWorkEvent> {
            override fun onEvent(event: AndroidNetWorkEvent) {
                onAndroidNetworkChangeEvent(event)
            }
        })
        EventBus.registerObserver(this, object : EventBus.EventObserver<GreenButtonEvent> {
            override fun onEvent(event: GreenButtonEvent) {
                onGreenButtonEvent(event)
            }
        })
        EventBus.registerObserver(this, object : EventBus.EventObserver<MqttConnectionEvent> {
            override fun onEvent(event: MqttConnectionEvent) {
                OnMqttConnectEvent(event)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(connectMqttRunnable)
    }

    override fun shouldResponse2TimeEvent(): Boolean {
        return true
    }

    override fun shouldResponseCallingEvent(): Boolean {
        return true
    }

    override fun onClick(view: View) {
        super.onClick(view)
    }

    override fun onCustomClickResult(id: Int) {
        super.onCustomClickResult(id)
        if (id == R.id.iv_home) {
            switchMainContentFragment()
        }
    }

    override fun onCustomBatteryChange(level: Int) {
        super.onCustomBatteryChange(level)
        tvBattery.text = String.format("%s%%", level)
    }

    override fun onCustomTimeStamp(event: TimeStampEvent) {
        tvTime.text = TimeUtil.formatHourAndMinute(Date())
        val commutingTimeSetting = RobotInfo.commutingTimeSetting
        if (!RobotInfo.isCountdownToTask && !FloatingCountdown.isShow() && commutingTimeSetting.open && !RobotInfo.isEmergencyButtonDown && !RobotInfo.isACCharging) {
            if (TimeUtil.isCurrentInTimeScope(
                    commutingTimeSetting.workingTime,
                    commutingTimeSetting.afterWorkTime
                )
            ) {
                if (RobotInfo.isWirelessCharging && RobotInfo.powerLevel >= commutingTimeSetting.autoWorkPower) {
                    hideChargingView()
                    EasyDialog.getLoadingInstance(this)
                        .loading(getString(R.string.text_init_product_point))
                    isAutoWork = true
                    VoiceHelper.play("voice_work_start_and_go_to_product_point") {
                        presenter.refreshProductModePoint(
                            this@MainActivity
                        )
                    }
                    return
                }
            } else {
                if (!RobotInfo.isCharging && RobotInfo.chargeFailedCount < 2) {
                    EasyDialog.getLoadingInstance(this)
                        .loading(getString(R.string.text_init_charging_point))
                    isAutoWork = true
                    VoiceHelper.play("voice_work_end_and_go_to_charge") {
                        presenter.refreshChargeModePoint(
                            this@MainActivity
                        )
                    }
                    return
                }
            }
        }
        super.onCustomTimeStamp(event)
    }

    override fun onCustomEmergencyStopStateChange(emergencyStopState: Int) {
        super.onCustomEmergencyStopStateChange(emergencyStopState)
        if (RobotInfo.isLifting && emergencyStopState == 0) {
            if ((!RobotInfo.isSelfChecking || !RobotInfo.isMapping) && EasyDialog.isShow()) EasyDialog.getInstance()
                .dismiss()
        } else if (emergencyStopState == 1) {
            if (RobotInfo.isSpaceShip() && RobotInfo.isLiftModelInstalled) {
                ROSController.getAltitudeState()
            }
        }
    }

    override fun onRobotTypeEvent(event: RobotTypeEvent) {
        RobotInfo.robotType = event.robotType
        callingInfo.heartBeatInfo.robotType = event.robotType
        if (event.robotType == 4 || event.robotType == 6 || event.robotType == 7) {
            val liftModelInstalled =
                SpManager.getInstance().getBoolean(Constants.KEY_LIFT_MODEL_INSTALLATION, true)
            RobotInfo.isLiftModelInstalled = liftModelInstalled
            if (liftModelInstalled) {
                mHandler.postDelayed({ ROSController.getAltitudeState() }, 200)
            }
        } else {
            RobotInfo.isLiftModelInstalled = false
        }
    }

    override fun onApplyMapEvent(event: ApplyMapEvent) {
        super.onApplyMapEvent(event)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.main_fragment_view)
        if (currentFragment !is MainContentFragment) {
            switchMainContentFragment()
            if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss()
            EasyDialog.getInstance(this)
                .warnError(getString(R.string.text_check_apply_map_please_reload_points))
        }
    }

    override fun onHostNameEvent(event: HostnameEvent) {
        tvHostname.text = event.hostname
        RobotInfo.ROSHostname = event.hostname
        callingInfo.heartBeatInfo.hostname = event.hostname
        mHandler.postDelayed(connectMqttRunnable, 500)
    }

    private fun onAndroidNetworkChangeEvent(event: AndroidNetWorkEvent) {
        when (event.intent.action) {
            WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                val info =
                    event.intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                if (info?.state == NetworkInfo.State.DISCONNECTED) {
                    Timber.v("DISCONNECTED")
                    refreshState()
                } else if (info?.state == NetworkInfo.State.CONNECTED) {
                    Timber.v("CONNECTED")
                    if (++androidWifiConnectCount > 1) {
                        androidWifiConnectCount = 0
                        mHandler.postDelayed(connectMqttRunnable, 3000)
                        refreshState()
                    }
                }
            }

            WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                val wifiState = event.intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
                if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    refreshState()
                }
            }
        }
    }

    override fun onNormalModePointsDataLoadSuccess(pointList: List<GenericPoint>) {
        if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable) EasyDialog.getInstance()
            .dismiss()
        switchModeNormalFragment(pointList)
    }

    override fun onNormalModeMapsWithPointsDataLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>) {
        if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable) EasyDialog.getInstance()
            .dismiss()
        switchModeNormalWithMapFragment(pointsWithMapList)
    }

    override fun onRouteModelDataLoadSuccess(pointList: List<GenericPoint>) {
        if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable) EasyDialog.getInstance()
            .dismiss()
        switchModeRouteFragment(false)
    }

    override fun onQRCodeModelDataLoadSuccess(pointList: List<GenericPoint>) {
        if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable) EasyDialog.getInstance()
            .dismiss()
        switchModeQRCodeFragment(pointList)
    }

    override fun onQRCodeModelMapsWithPointsDataLoadSuccess(pointsWithMapList: List<GenericPointsWithMap>) {
        if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable) EasyDialog.getInstance()
            .dismiss()
        switchModeQRCodeWithMapFragment(pointsWithMapList)
    }

    override fun onChargeModelDataLoadSuccess() {
        if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable) EasyDialog.getInstance()
            .dismiss()
        startTask(true)
    }

    override fun onProductModelDataLoadSuccess() {
        if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable) EasyDialog.getInstance()
            .dismiss()
        startTask(false)
    }

    override fun onDataLoadFailed(errorTip: String) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss()
        EasyDialog.getInstance(this).warnError(errorTip)
    }

    override fun onDataLoadFailed(throwable: Throwable) {
        Timber.w(throwable, "拉取点位失败")
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss()
        EasyDialog.getInstance(this).warnError(Errors.getDataLoadFailedTip(this, throwable))
    }

    override fun onFastClick() {
        EasyDialog.getInstance(this)
            .confirm(getString(R.string.text_exit_app)) { dialog: Dialog, id: Int ->
                dialog.dismiss()
                if (id == R.id.btn_confirm) {
                    Timber.w("退出应用")
                    finish()
                    ScreenUtils.setImmersive(this)
                    EventBus.unregisterAll(this)
                    mApp.exit()
                }
            }
    }

    override fun onModeClick(mode: TaskMode) {
        when (mode) {
            TaskMode.MODE_NORMAL -> presenter.refreshNormalModePoints(this)
            TaskMode.MODE_START_POINT -> {
                isAutoWork = false
                presenter.refreshProductModePoint(this)
            }

            TaskMode.MODE_CHARGE -> {
                isAutoWork = false
                presenter.refreshChargeModePoint(this)
            }

            TaskMode.MODE_ROUTE -> presenter.refreshRouteModePoints(this)
            TaskMode.MODE_QRCODE -> presenter.refreshQRCodeModePoints(this)
            else -> {
                Timber.d("unknown task mode")
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_F2) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.main_fragment_view)
            if (currentFragment is OnGreenButtonClickListener) {
                currentFragment.onKeyUpEvent()
            }
            return false
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun onGreenButtonEvent(event: GreenButtonEvent) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.main_fragment_view)
        if (currentFragment is OnGreenButtonClickListener) {
            currentFragment.onKeyUpEvent()
        }
    }

    /**
     * 开始任务
     */
    private fun startTask(isChargePoint: Boolean) {
        val returningSetting = RobotInfo.returningSetting
        if (!returningSetting.startTaskCountDownSwitch) {
            if (!canStartTask()) return
            if (isChargePoint) {
                presenter.gotoChargePoint(this, isAutoWork)
            } else {
                presenter.gotoProductPoint(this, isAutoWork)
            }
            return
        }
        if (!canStartTask()) return
        EasyDialog.getInstance(this@MainActivity).warnWithScheduledUpdateDetail(
            getString(
                if (RobotInfo.mode == TaskMode.MODE_CHARGE) R.string.text_will_goto_charging_pile_after_count_down else R.string.text_will_goto_production_point_after_count_down,
                returningSetting.startTaskCountDownTime
            ),
            R.string.text_start_right_now,
            R.string.text_cancel,
            { dialog: Dialog, id: Int ->
                dialog.dismiss()
                RobotInfo.isCountdownToTask = false
                if (id == R.id.btn_confirm) {
                    if (!canStartTask()) return@warnWithScheduledUpdateDetail
                    if (isChargePoint) {
                        presenter.gotoChargePoint(this, isAutoWork)
                    } else {
                        presenter.gotoProductPoint(this, isAutoWork)
                    }
                } else {
                    CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.FREE)
                }
            },
            object : OnTimeStampListener {
                override fun onTimestamp(
                    title: TextView,
                    content: TextView,
                    cancelBtn: Button,
                    neutralBtn: Button,
                    confirmBtn: Button,
                    current: Int
                ) {
                    content.text = getString(
                        if (RobotInfo.mode == TaskMode.MODE_CHARGE) R.string.text_will_goto_charging_pile_after_count_down else R.string.text_will_goto_production_point_after_count_down,
                        returningSetting.startTaskCountDownTime - current
                    )
                }

                override fun onTimeOut(dialog: EasyDialog) {
                    dialog.dismiss()
                    RobotInfo.isCountdownToTask = false
                    if (!canStartTask()) return
                    if (isChargePoint) {
                        presenter.gotoChargePoint(this@MainActivity, isAutoWork)
                    } else {
                        presenter.gotoProductPoint(this@MainActivity, isAutoWork)
                    }
                }
            },
            1000,
            returningSetting.startTaskCountDownTime * 1000
        )
        RobotInfo.isCountdownToTask = true
        CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN)
    }

    private fun canStartTask(): Boolean {
        if (RobotInfo.isEmergencyButtonDown) {
            EasyDialog.getInstance(this).warnError(getString(R.string.voice_scram_stop_turn_on))
            VoiceHelper.play("voice_scram_stop_turn_on")
            CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.FREE)
            return false
        }
        if (RobotInfo.isACCharging) {
            VoiceHelper.play("voice_charging_and_can_not_move")
            EasyDialog.getInstance(this)
                .warnError(getString(R.string.voice_charging_and_can_not_move))
            CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.FREE)
            return false
        }
        if (RobotInfo.isLifting) {
            EasyDialog.getInstance(this)
                .warn(getString(R.string.text_check_altitude_moving_click_confirm_to_continue)) { dialog: Dialog, did: Int ->
                    if (did == R.id.btn_confirm) {
                        ROSController.ioControl(4)
                        mHandler.postDelayed({
                            if (RobotInfo.isEmergencyButtonDown) {
                                VoiceHelper.play("voice_scram_stop_turn_on")
                                EasyDialog.getInstance(this)
                                    .warnError(getString(R.string.voice_scram_stop_turn_on))
                                return@postDelayed
                            }
                            if (RobotInfo.isCharging) {
                                EasyDialog.getInstance(this)
                                    .warnError(getString(R.string.voice_charging_and_can_not_move))
                                return@postDelayed
                            }
                            if (RobotInfo.liftModelState == 0) {
                                ROSController.liftDown()
                            } else {
                                ROSController.liftUp()
                            }
                            callingInfo.isLifting = true
                            EasyDialog.getLoadingInstance(this)
                                .loading(getString(R.string.text_pickup_model_resetting))
                        }, 200)
                    }
                    dialog.dismiss()
                }
            CallingStateManager.setTaskExecutingEvent(TaskExecutingCode.FREE)
            return false
        }
        return true
    }

    private fun OnMqttConnectEvent(event: MqttConnectionEvent) {
        tvCall.visibility =
            if (event.isConnected) View.VISIBLE else View.GONE
    }

    override fun onInitiativeLiftingModuleStateEvent(event: InitiativeLiftingModuleStateEvent) {
        super.onInitiativeLiftingModuleStateEvent(event)
        Timber.w(
            "是否安装顶升模块: %s ,顶升模块状态: 动作 : %s , 状态 : %s",
            RobotInfo.isLiftModelInstalled,
            if (event.action == 1) "上升" else "下降",
            if (event.state == 1) "完成" else "未完成"
        )
        Timber.w(
            "dialogIsShow : %s , dialogIsAutoDismissEnable : %s , taskAbnormalFinishPrompt : %s",
            EasyDialog.isShow(),
            EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable,
            RobotInfo.taskAbnormalFinishPrompt
        )
        if (!RobotInfo.isLiftModelInstalled || !RobotInfo.isSpaceShip()) return
        RobotInfo.isLifting = event.state == 0
        RobotInfo.liftModelState = event.action
        callingInfo.isLifting = event.state == 0
        Timber.i(
            "isEmergencyButtonDown : %s, isRouteTaskWaitingForStart : %s",
            RobotInfo.isEmergencyButtonDown,
            isRouteTaskWaitingForStart
        )
        if (RobotInfo.isEmergencyButtonDown || isRouteTaskWaitingForStart || RobotInfo.isCharging) return
        val currentFragment = supportFragmentManager.findFragmentById(R.id.main_fragment_view)
        if (currentFragment !is ModeQRCodeFragment && currentFragment !is ModeQRCodeWithMapFragment) {
            Timber.i("currentFragment instanceof ModeQRCodeFragment")
            if (EasyDialog.isShow() && !EasyDialog.getInstance().isTiming && RobotInfo.taskAbnormalFinishPrompt == null) EasyDialog.getInstance()
                .dismiss()
            return
        }
        if (EasyDialog.isShow()) {
            if (!EasyDialog.getInstance().isAutoDismissEnable) {
                return
            }
            EasyDialog.getInstance().dismiss()
        }
        if (event.action == 1) {
            EasyDialog.getInstance(this)
                .warn(getString(R.string.text_check_altitude_up)) { dialog: Dialog, id: Int ->
                    if (id == R.id.btn_confirm) {
                        ROSController.ioControl(4)
                        mHandler.postDelayed({
                            if (RobotInfo.isEmergencyButtonDown) {
                                VoiceHelper.play("voice_scram_stop_turn_on")
                                EasyDialog.getInstance(this)
                                    .warnError(getString(R.string.voice_scram_stop_turn_on))
                                return@postDelayed
                            }
                            ROSController.liftDown()
                            callingInfo.isLifting = true
                            EasyDialog.getLoadingInstance(this)
                                .loading(getString(R.string.text_pickup_model_resetting))
                        }, 200)
                    }
                    dialog.dismiss()
                }
            return
        }
        if (event.state == 0) {
            EasyDialog.getInstance(this)
                .warn(getString(R.string.text_check_altitude_moving)) { dialog: Dialog, id: Int ->
                    if (id == R.id.btn_confirm) {
                        ROSController.ioControl(4)
                        mHandler.postDelayed({
                            if (RobotInfo.isEmergencyButtonDown) {
                                VoiceHelper.play("voice_scram_stop_turn_on")
                                EasyDialog.getInstance(this)
                                    .warnError(getString(R.string.voice_scram_stop_turn_on))
                                return@postDelayed
                            }
                            ROSController.liftDown()
                            callingInfo.isLifting = true
                            EasyDialog.getLoadingInstance(this)
                                .loading(getString(R.string.text_pickup_model_resetting))
                        }, 200)
                    }
                    dialog.dismiss()
                }
            return
        }
        if (EasyDialog.isShow() && !EasyDialog.getInstance().isTiming && RobotInfo.taskAbnormalFinishPrompt == null) {
            EasyDialog.getInstance().dismiss()
        }
    }

    private val connectMqttRunnable = Runnable {
        val mqttClient = MqttClient.getInstance()
        tvCall.visibility = if (mqttClient.isConnected) View.VISIBLE else View.GONE
        if (!mqttClient.isConnected && !mqttClient.isConnecting && WIFIUtils.isNetworkConnected(this@MainActivity)) {
            mqttClient
                .connect(RobotInfo.ROSHostname, this@MainActivity)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext { v: Int? -> Timber.w("mqtt connected") }
                .observeOn(Schedulers.io())
                .flatMap { v: Int? -> mqttClient.subscribeToTopic() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(aBoolean: Boolean) {
                        if (!this@MainActivity.isFinishing && !this@MainActivity.isDestroyed) {
                            tvCall.visibility = View.VISIBLE
                        }
                    }

                    override fun onError(e: Throwable) {
                        if (!this@MainActivity.isFinishing && !this@MainActivity.isDestroyed) {
                            tvCall.visibility = View.GONE
                            if (!mqttClient.isConnected) {
                                Timber.w(e, "建立mqtt连接失败")
                            } else {
                                Timber.w(e, "订阅失败")
                            }
                        }
                    }

                    override fun onComplete() {}
                })
        }
    }
}