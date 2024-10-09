package com.reeman.agv.base;

import static com.reeman.agv.base.BaseApplication.dbRepository;
import static com.reeman.agv.base.BaseApplication.mApp;
import static com.reeman.agv.base.BaseApplication.ros;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import kotlin.Pair;
import timber.log.Timber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.reeman.agv.R;
import com.reeman.agv.activities.MainActivity;
import com.reeman.agv.activities.TaskExecutingActivity;
import com.reeman.agv.calling.CallingInfo;
import com.reeman.agv.calling.event.CallingTaskEvent;
import com.reeman.agv.calling.event.CallingTaskQueueUpdateEvent;
import com.reeman.agv.calling.event.ChargeTaskEvent;
import com.reeman.agv.calling.event.NormalTaskEvent;
import com.reeman.agv.calling.event.QRCodeTaskEvent;
import com.reeman.agv.calling.event.ReturnTaskEvent;
import com.reeman.agv.calling.event.RouteTaskEvent;
import com.reeman.agv.calling.event.StartTaskCountDownEvent;
import com.reeman.agv.calling.event.TaskEvent;
import com.reeman.agv.calling.model.TaskDetails;
import com.reeman.agv.calling.setting.ModeCallingSetting;
import com.reeman.agv.calling.utils.CallingStateManager;
import com.reeman.agv.calling.utils.TaskExecutingCode;
import com.reeman.agv.constants.Errors;
import com.reeman.commons.board.Board;
import com.reeman.commons.board.BoardFactory;
import com.reeman.commons.event.ApplyMapEvent;
import com.reeman.commons.event.CoreDataEvent;
import com.reeman.commons.event.CurrentMapEvent;
import com.reeman.commons.event.IPEvent;
import com.reeman.commons.event.InitPoseEvent;
import com.reeman.commons.event.InitiativeLiftingModuleStateEvent;
import com.reeman.commons.event.NavPoseEvent;
import com.reeman.commons.event.NavigationResultEvent;
import com.reeman.commons.event.PowerOffEvent;
import com.reeman.commons.event.ROSModelEvent;
import com.reeman.commons.event.SensorsEvent;
import com.reeman.commons.event.WheelStatusEvent;
import com.reeman.commons.event.TimeStampEvent;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.eventbus.EventBus;
import com.reeman.commons.settings.CommutingTimeSetting;
import com.reeman.commons.settings.ModeNormalSetting;
import com.reeman.commons.settings.ModeQRCodeSetting;
import com.reeman.commons.settings.ModeRouteSetting;
import com.reeman.commons.settings.ReturningSetting;
import com.reeman.commons.state.StartTaskCode;
import com.reeman.agv.constants.TaskResult;
import com.reeman.commons.exceptions.ClickFastException;
import com.reeman.agv.elevator.state.Code;
import com.reeman.commons.utils.PointUtils;
import com.reeman.dao.repository.entities.RouteWithPoints;
import com.reeman.commons.state.NavigationMode;
import com.reeman.commons.state.NavigationState;
import com.reeman.commons.state.State;
import com.reeman.commons.state.TaskMode;
import com.reeman.points.model.custom.GenericPoint;
import com.reeman.points.model.custom.GenericPointsWithMap;
import com.reeman.points.process.PointRefreshProcessingStrategy;
import com.reeman.points.process.PointRefreshProcessor;
import com.reeman.points.process.callback.RefreshPointDataCallback;
import com.reeman.points.process.impl.DeliveryPointsRefreshProcessingStrategy;
import com.reeman.points.process.impl.DeliveryPointsWithMapsRefreshProcessingStrategy;
import com.reeman.points.process.impl.FixedDeliveryPointsRefreshProcessingStrategy;
import com.reeman.points.process.impl.FixedDeliveryPointsWithMapsRefreshProcessingStrategy;
import com.reeman.points.utils.PointCacheInfo;
import com.reeman.ros.callback.ROSCallback;
import com.reeman.ros.filter.ROSFilter;
import com.reeman.dao.repository.entities.DeliveryRecord;
import com.reeman.agv.request.ServiceFactory;
import com.reeman.commons.model.request.ChargeRecord;
import com.reeman.commons.model.request.Msg;
import com.reeman.agv.request.notifier.Notifier;
import com.reeman.agv.request.notifier.NotifyConstant;
import com.reeman.agv.request.url.API;
import com.reeman.commons.state.RobotInfo;
import com.reeman.commons.utils.FileMapUtils;
import com.reeman.commons.utils.LocaleUtil;
import com.reeman.agv.utils.PackageUtils;
import com.reeman.agv.utils.ScreenUtils;
import com.reeman.commons.utils.SoftKeyboardStateWatcher;
import com.reeman.commons.utils.SpManager;
import com.reeman.commons.utils.TimeUtil;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.utils.VoiceHelper;
import com.reeman.agv.widgets.RemoteTaskListDialog;
import com.reeman.agv.widgets.ChargingWindows;
import com.reeman.agv.widgets.EasyDialog;
import com.reeman.agv.widgets.FloatingCountdown;
import com.reeman.agv.widgets.FloatingCallingListView;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class BaseActivity extends AppCompatActivity implements SoftKeyboardStateWatcher.SoftKeyboardStateListener, View.OnClickListener, FloatingCountdown.CountDownTimerCallback, FloatingCallingListView.OnFloatingCallingListViewClickListener, ROSCallback {

    protected final Gson gson = new GsonBuilder().serializeNulls().create();
    private SoftKeyboardStateWatcher softKeyboardStateWatcher;
    protected Map<Integer, View> views;
    protected final Handler mHandler = new Handler(Looper.getMainLooper());
    private long lastClickTimeMills;
    private int relocCount = 0;

    protected RobotInfo robotInfo;
    protected CallingInfo callingInfo;

    private Disposable disposable;

    private boolean isRouteTaskWaitingForStart = false;

    public boolean isRouteTaskWaitingForStart() {
        return isRouteTaskWaitingForStart;
    }

    public boolean restrictFrequency(long interval) {
        long currentTimeMillis = SystemClock.uptimeMillis();
        if (currentTimeMillis - lastClickTimeMills < interval) {
            lastClickTimeMills = currentTimeMillis;
            return true;
        }
        lastClickTimeMills = currentTimeMillis;
        return false;
    }

    protected void updateLastClickTimeMills() {
        lastClickTimeMills = SystemClock.uptimeMillis();
    }

    protected final Runnable chargeRunnable = new Runnable() {
        @Override
        public void run() {
            ArrayList<Activity> activityStack = robotInfo.getActivityStack();
            if (!ChargingWindows.getInstance().isShow()
                    && robotInfo.isCharging()
                    && !activityStack.isEmpty()
                    && activityStack.get(activityStack.size() - 1) instanceof MainActivity
                    && !EasyDialog.isShow()
            ) {
                showChargingView(robotInfo.getPowerLevel());
                return;
            }
            mHandler.postDelayed(chargeRunnable, 10000);
        }
    };

    protected synchronized void showChargingView(int powerLevel) {
        if (ChargingWindows.getInstance().isShow()) {
            ChargingWindows.getInstance().update(powerLevel);
        } else {
            ChargingWindows.getInstance().show(mApp, powerLevel, callback);
            robotInfo.setChargingScreenSaverShowTime(System.currentTimeMillis());
        }
    }

    private final ChargingWindows.ChargingWindowsCallback callback = () -> {
        hideChargingView();
        mHandler.postDelayed(chargeRunnable, 10000);
    };

    protected synchronized void hideChargingView() {
        if (ChargingWindows.getInstance().isShow()) {
            ChargingWindows.getInstance().dismiss(mApp);
            robotInfo.setChargingScreenSaverShowTime(-1);
        }
    }

    public static void startup(Context context, Class<? extends Activity> clazz) {
        Intent intent = new Intent(context, clazz);
        context.startActivity(intent);
    }

    public static void startupAndClearStack(Context context, Class<? extends Activity> clazz) {
        Intent intent = new Intent(context, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        robotInfo = RobotInfo.INSTANCE;
        callingInfo = CallingInfo.INSTANCE;
        int languageType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
        Timber.tag(getClass().getSimpleName()).w("语言 %d", languageType);
        LocaleUtil.changeAppLanguage(getResources(), languageType);
        softKeyboardStateWatcher = new SoftKeyboardStateWatcher(getWindow().getDecorView());
        softKeyboardStateWatcher.addSoftKeyboardStateListener(this);
        robotInfo.addTOActivityStack(this);
        ScreenUtils.hideBottomUIMenu(this);
        initData();
        setContentView(getLayoutRes());
        initView();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.v("onTouch", ev.getX() + "," + ev.getY());
        mHandler.removeCallbacks(chargeRunnable);
        mHandler.postDelayed(chargeRunnable, 10000);
        return super.dispatchTouchEvent(ev);
    }

    public <T extends View> T $(@IdRes int id) {
        View view = views.get(id);
        if (view != null) return (T) view;
        View targetView = findViewById(id);
        views.put(id, targetView);
        return (T) targetView;
    }

    protected void setOnClickListeners(int... ids) {
        for (int id : ids) {
            View view = views.get(id);
            if (view == null) {
                view = findViewById(id);
                views.put(id, view);
            }
            if (view != null) view.setOnClickListener(this);
        }
    }

    protected void setOnClickListeners(ViewGroup viewGroup, int... ids) {
        for (int id : ids) {
            View view = views.get(id);
            if (view == null) {
                view = viewGroup.findViewById(id);
                views.put(id, view);
            }
            if (view != null) view.setOnClickListener(this);
        }
    }

    private void initView() {
        views = new HashMap<>();
        initCustomView();
    }

    protected abstract int getLayoutRes();

    protected abstract void initCustomView();

    protected void initData() {
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.w("%s onResume", getClass().getSimpleName());
        if (ros != null) {
            ros.registerListener(this);
        }
        registerObservers();
        mHandler.postDelayed(chargeRunnable, 10000);
        CallingStateManager.INSTANCE.setCurrentActivityCanTakeRemoteTaskEvent(shouldResponseCallingEvent());
        if (!shouldResponseCallingEvent()) {
            if (FloatingCallingListView.isShow()) FloatingCallingListView.getInstance().close();
            if (FloatingCountdown.isShow()) FloatingCountdown.getInstance().close();
            CallingStateManager.INSTANCE.setCountingDownAfterCallingTaskEvent(false);
        }
        if (ChargingWindows.getInstance().isShow()) {
            ChargingWindows.getInstance().setCallback(callback);
        }
    }

    private void registerObservers() {
        EventBus.INSTANCE.registerObserver(
                this,
                TimeStampEvent.class,
                this::onCustomTimeStamp,
                Schedulers.io(),
                AndroidSchedulers.mainThread());
        EventBus.INSTANCE.registerObserver(
                this,
                CallingTaskQueueUpdateEvent.class,
                this::onCallingTaskQueueUpdateEvent,
                Schedulers.io(),
                AndroidSchedulers.mainThread());
        EventBus.INSTANCE.registerObserver(
                this,
                CallingTaskEvent.class,
                this::onCustomCallingTask,
                Schedulers.io(),
                AndroidSchedulers.mainThread());
        EventBus.INSTANCE.registerObserver(
                this,
                NormalTaskEvent.class,
                this::onCustomNormalTask,
                Schedulers.io(),
                AndroidSchedulers.mainThread());
        EventBus.INSTANCE.registerObserver(
                this,
                RouteTaskEvent.class,
                this::onCustomRouteTask,
                Schedulers.io(),
                AndroidSchedulers.mainThread());
        EventBus.INSTANCE.registerObserver(
                this,
                QRCodeTaskEvent.class,
                this::onCustomQRCodeTask,
                Schedulers.io(),
                AndroidSchedulers.mainThread());
        EventBus.INSTANCE.registerObserver(
                this,
                ChargeTaskEvent.class,
                this::onCustomChargeTask,
                Schedulers.io(),
                AndroidSchedulers.mainThread());
        EventBus.INSTANCE.registerObserver(
                this,
                ReturnTaskEvent.class,
                this::onCustomReturnTask,
                Schedulers.io(),
                AndroidSchedulers.mainThread());
        EventBus.INSTANCE.registerObserver(
                this,
                TaskEvent.class,
                this::onTaskEvent,
                Schedulers.io(),
                AndroidSchedulers.mainThread());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.w("%s onPause", getClass().getSimpleName());
        if (ros != null) {
            ros.unregisterListener(this);
        }
        EventBus.INSTANCE.unregisterAll(this);
        mHandler.removeCallbacks(lowPowerRunnable);
        mHandler.removeCallbacks(chargeRunnable);
        if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable())
            EasyDialog.getInstance().dismiss();
        if (RemoteTaskListDialog.isShow()) RemoteTaskListDialog.getInstance().dismiss();
        if (ChargingWindows.getInstance().isShow()) {
            ChargingWindows.getInstance().setCallback(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.w("%s onDestroy", getClass().getSimpleName());
        views.clear();
        views = null;
        softKeyboardStateWatcher.removeSoftKeyboardStateListener(this);
        robotInfo.removeFromActivityStack(this);
    }

    @Override
    public void onSoftKeyboardOpened(int keyboardHeightInPx) {
    }

    @Override
    public void onSoftKeyboardClosed() {
        ScreenUtils.hideBottomUIMenu(this);
    }

    @Override
    public void onClick(View view) {
        destroyClickEvent();
        onCustomClick(view);
    }

    protected void onCustomAntiFallEvent() {
    }

    //子类自定义急停开关处理逻辑，默认关闭提示窗口
    protected void onCustomEmergencyStopStateChange(int emergencyStopState) {
        if (emergencyStopState == 0) {
            VoiceHelper.play("voice_scram_stop_turn_on");
        } else {
            VoiceHelper.play("voice_scram_stop_turn_off");
        }
    }

    //子类自定义断电处理逻辑
    protected void onCustomPowerDisconnected() {
        String hostname = robotInfo.getROSHostname();
        if (TextUtils.isEmpty(hostname)) return;
        String path = API.batteryLogAPI(hostname);
        long timeMills = System.currentTimeMillis();
        ServiceFactory
                .getRobotService()
                .reportChargeResult(path,
                        new ChargeRecord(
                                robotInfo.getLastPowerLevel(),
                                robotInfo.getPowerLevel(),
                                robotInfo.getLastChargeTime(),
                                timeMills,
                                robotInfo.getLastChargeType(),
                                0,
                                0,
                                0,
                                0,
                                timeMills,
                                BaseApplication.macAddress,
                                "v1.1",
                                BaseApplication.appVersion)
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(map -> Log.w("xuedong", "充电记录上传成功"), throwable -> Timber.w(throwable, "上传充电记录失败"));
    }

    //子类自定义充电处理逻辑，默认关闭充电对接提醒
    protected void onCustomPowerConnected() {
    }


    protected void onNavigationCancelResult(int code) {

    }

    protected void onNavigationCompleteResult(int code, String name, float mileage) {
    }

    protected void onNavigationStartResult(int code, String name) {
        if (code != 0) {
            robotInfo.setNavigating(false);
            callingInfo.getHeartBeatInfo().setNavigating(false);
        } else {
            callingInfo.getHeartBeatInfo().setNavigating(true);
        }
    }

    //子类自定义充电对接失败处理逻辑
    protected void onCustomDockFailed() {
    }


    protected void onCustomBatteryChange(int level) {

    }

    //是否应该响应电量低和定时任务事件
    protected boolean shouldResponse2TimeEvent() {
        return false;
    }

    protected boolean shouldResponseCallingEvent() {
        return false;
    }

    private final Runnable lowPowerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!shouldResponse2TimeEvent()) return;
            ReturningSetting returningSetting = robotInfo.getReturningSetting();
            if (!returningSetting.startTaskCountDownSwitch) {
                if (robotInfo.isEmergencyButtonDown()) {
                    EasyDialog.getInstance(BaseActivity.this).warnError(getString(R.string.voice_scram_stop_turn_on));
                    return;
                }
                refreshChargeModePoint();
                return;
            }
            EasyDialog.getInstance(BaseActivity.this).warnWithScheduledUpdateDetail(
                    getString(R.string.text_going_to_charge_for_low_power, returningSetting.startTaskCountDownTime),
                    R.string.text_start_right_now,
                    R.string.text_cancel,
                    (dialog, id) -> {
                        EasyDialog.getInstance().dismiss();
                        robotInfo.setCountdownToTask(false);
                        if (id == R.id.btn_confirm) {
                            if (robotInfo.isEmergencyButtonDown()) {
                                EasyDialog.getInstance(BaseActivity.this).warnError(getString(R.string.voice_scram_stop_turn_on));
                                return;
                            }
                            refreshChargeModePoint();
                        }
                    },
                    new EasyDialog.OnTimeStampListener() {
                        @Override
                        public void onTimestamp(TextView title, TextView content, Button cancelBtn, Button neutralBtn, Button confirmBtn, int current) {
                            content.setText(getString(R.string.text_going_to_charge_for_low_power, returningSetting.startTaskCountDownTime - current));
                        }

                        @Override
                        public void onTimeOut(EasyDialog dialog) {
                            dialog.dismiss();
                            robotInfo.setCountdownToTask(false);
                            if (robotInfo.isEmergencyButtonDown()) {
                                EasyDialog.getInstance(BaseActivity.this).warnError(getString(R.string.voice_scram_stop_turn_on));
                                return;
                            }
                            refreshChargeModePoint();
                        }
                    },
                    1000,
                    returningSetting.startTaskCountDownTime * 1000
            );
            robotInfo.setCountdownToTask(true);
        }
    };

    //子类自定义低电、定时任务处理逻辑
    protected void onCustomTimeStamp(TimeStampEvent event) {
        if (ROSFilter.isLastTimestampsDiff(event.getTime())) return;
        if (!shouldResponse2TimeEvent()) {
            return;
        }
        if (robotInfo.isCountdownToTask() || FloatingCountdown.isShow()) return;
        if (robotInfo.isEmergencyButtonDown() || robotInfo.isLifting() || robotInfo.getLiftModelState() == 1) {
            Timber.d("急停: %s , 顶升状态: %s , 顶升位置: %s", robotInfo.isEmergencyButtonDown(), robotInfo.isLifting(), robotInfo.getLiftModelState());
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();

  /*
        开机3天以上,
        充电半小时以上,
        正在充电,
        在充电桩位置(角度小于60度,距离小于0.7)
         */
//        long powerOnTime = 1000 * 60 * 5;
        long powerOnTime = 1000 * 60 * 60 * 24 * 3;
        long lastRebootTime = SpManager.getInstance().getLong(Constants.KEY_LAST_REBOOT_TIME, 0L);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (elapsedRealtime < powerOnTime && lastRebootTime != 0) {
            SpManager.getInstance().edit().putLong(Constants.KEY_LAST_REBOOT_TIME, 0L).apply();
            lastRebootTime = 0L;
        }
        boolean powerOnOver3Day;
        if (lastRebootTime == 0) {
            powerOnOver3Day = elapsedRealtime > powerOnTime;
        } else {
            powerOnOver3Day = elapsedRealtime - lastRebootTime > powerOnTime;
        }
        Timber.w("lastRebootTime : %s , elapsedRealtime : %s", lastRebootTime, elapsedRealtime);
        if (powerOnOver3Day
                && robotInfo.getChargingScreenSaverShowTime() != -1
//                && currentTimeMillis - robotInfo.getChargingScreenSaverShowTime() > 1000 * 60
                && currentTimeMillis - robotInfo.getChargingScreenSaverShowTime() > 1000 * 60 * 30
                && robotInfo.isWirelessCharging()
                && !robotInfo.isSelfChecking()) {
            Timber.w("距离上次导航重启超过3天");
            if (!PointCacheInfo.INSTANCE.isChargePointInitialized()) return;
            Pair<String, GenericPoint> chargePoint = PointCacheInfo.INSTANCE.getChargePoint();
            Timber.w("charge point : %s", chargePoint);
            double[] currentPosition = robotInfo.getCurrentPosition();
            if (currentPosition == null || currentPosition.length != 3) return;
            Timber.w("currentPosition : %s", Arrays.toString(currentPosition));
            if (!PointUtils.INSTANCE.isPositionError(chargePoint.getSecond().getPosition(), currentPosition, 0.7, 1.04f)) {
                if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable())
                    EasyDialog.getInstance().dismiss();
                hideChargingView();
                if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
                EasyDialog.getLoadingInstance(this).loading(getString(R.string.text_self_check));
                robotInfo.setSelfChecking(true);
                CallingStateManager.INSTANCE.setSelfCheckEvent(true);
                mHandler.postDelayed(selfCheckTimeOutRunnable, 5 * 60 * 1000);
                SpManager.getInstance().edit().putLong(Constants.KEY_LAST_REBOOT_TIME, elapsedRealtime).apply();
                ros.sysReboot();
                return;
            }
        }

        if (robotInfo.isLowPower()) {
            if (robotInfo.isEmergencyButtonDown()
                    || robotInfo.isNavigating()
                    || robotInfo.isChargeDocking()
                    || robotInfo.isCharging()
                    || robotInfo.isSelfChecking()
            ) return;

            if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable())
                EasyDialog.getInstance().dismiss();
            mHandler.postDelayed(lowPowerRunnable, 200);
        }
    }

    protected void startGotoCharge() {
        Timber.w("空闲状态触发低电");
        // TODO: 2023/5/29 去充电
        robotInfo.setMode(TaskMode.MODE_CHARGE);
        Intent intent = new Intent(this, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, Constants.TASK_CHARGE_LOW_POWER);
        startActivityForResult(this, intent, Constants.RESULT_CODE_OF_TASK);
    }


    protected void startActivityForResult(Activity activity, Intent intent, int code) {
        activity.startActivityForResult(intent, code);
    }

    public void refreshChargeModePoint() {
        PointRefreshProcessingStrategy processingStrategy;
        if (robotInfo.isElevatorMode()) {
            if (robotInfo.getNavigationMode() == NavigationMode.autoPathMode) {
                processingStrategy = new DeliveryPointsWithMapsRefreshProcessingStrategy();
            } else {
                processingStrategy = new FixedDeliveryPointsWithMapsRefreshProcessingStrategy();
            }
        } else {
            if (robotInfo.getNavigationMode() == NavigationMode.autoPathMode) {
                processingStrategy = new DeliveryPointsRefreshProcessingStrategy();
            } else {
                processingStrategy = new FixedDeliveryPointsRefreshProcessingStrategy();
            }
        }
        EasyDialog.getLoadingInstance(this).loading(getString(R.string.text_init_charging_point));
        new PointRefreshProcessor(processingStrategy, new RefreshPointDataCallback() {

            @Override
            public void onPointsLoadSuccess(@androidx.annotation.NonNull List<GenericPoint> pointList) {
                if (EasyDialog.isShow())
                    EasyDialog.getInstance().dismiss();
                startGotoCharge();
            }

            @Override
            public void onPointsWithMapsLoadSuccess(@androidx.annotation.NonNull List<GenericPointsWithMap> pointsWithMapList) {
                if (EasyDialog.isShow())
                    EasyDialog.getInstance().dismiss();
                startGotoCharge();
            }

            @Override
            public void onThrowable(Throwable throwable) {
                Timber.w(throwable, "拉取充电桩失败");
                ToastUtils.showShortToast(Errors.INSTANCE.getDataLoadFailedTip(BaseActivity.this, throwable));
            }
        }).process(robotInfo.getROSIPAddress(), false, robotInfo.supportEnterElevatorPoint(), Collections.singletonList(GenericPoint.CHARGE));
    }

    protected void onCustomPosition(double[] position) {

    }

    protected void onCustomInitPose(double[] currentPosition) {
        ros.resetAllROSParameter();
        double[] position = robotInfo.getCurrentPosition();
        if (robotInfo.isSelfChecking() && EasyDialog.isShow() && position != null) {
            mHandler.removeCallbacks(selfCheckTimeOutRunnable);
            if (relocCount++ < 3) {
                mHandler.postDelayed(() -> ros.cpuPerformance(), 100);
                if (!PointUtils.INSTANCE.isPositionError(position, currentPosition, 0.7, 1.04f)) {
                    if (robotInfo.isCharging()) {
                        showChargingView(robotInfo.getPowerLevel());
                    }
                    EasyDialog.getInstance().dismiss();
                    robotInfo.setSelfChecking(false);
                    CallingStateManager.INSTANCE.setSelfCheckEvent(false);
                    ros.currentInfoControl(true);
                    ros.positionAutoUploadControl(true);
                    relocCount = 0;
                } else {
                    ros.relocateByCoordinate(position);
                }
                return;
            }
            relocCount = 0;
            robotInfo.setSelfChecking(false);
            CallingStateManager.INSTANCE.setSelfCheckEvent(false);
            EasyDialog.getInstance().dismiss();
            ros.currentInfoControl(true);
            ros.positionAutoUploadControl(true);
            Notifier.notify(new Msg(NotifyConstant.LOCATE_NOTIFY, "定位异常", "自动重启导航后定位失败", robotInfo.getROSHostname()));
            mHandler.postDelayed(() -> EasyDialog.getInstance(this).warnError(getString(R.string.text_check_position_error)), 300);
        } else {
            ros.currentInfoControl(true);
            ros.positionAutoUploadControl(true);
        }
    }

    private void uploadLocalTask() {
        dbRepository.getAllDeliveryRecords()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleObserver<List<DeliveryRecord>>() {
                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<DeliveryRecord> deliveryRecords) {
                        if (deliveryRecords.isEmpty()) return;
                        try {
                            retrofit2.Response<Map<String, Object>> response = ServiceFactory.getRobotService().reportTaskListResult(API.taskListRecordAPI(robotInfo.getROSHostname()), deliveryRecords).execute();
                            if (response.code() == 0) {
                                dbRepository.deleteAllRecords();
                            }
                            Timber.w("上传结果%s", response.code());
                        } catch (Exception e) {
                            Timber.w(e, "上传失败");
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        e.printStackTrace();

                    }

                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                    }
                });
    }

    protected void detailSensorState(SensorsEvent event) {

    }

    protected void detailWheelState(WheelStatusEvent event) {

    }


    private void detailPowerLevelUpdateEvent(int powerLevel, int chargeState) {
        try {
            Timber.w("更新电量 : %s , 充电显示 : %s", powerLevel, ChargingWindows.getInstance().isShow());
            if (ChargingWindows.getInstance().isShow()) {
                ChargingWindows.getInstance().update(powerLevel);
            }
        } catch (Exception e) {
            Timber.w(e, "更新电量失败");
        }
        onCustomBatteryChange(powerLevel);
    }

    private void detailChargeStateUpdateEvent(int chargeState, int powerLevel) {
        if (chargeState == 1) {
            hideChargingView();
            if (robotInfo.isCharging()) {
                ArrayList<Activity> activityStack = robotInfo.getActivityStack();
                if (!(activityStack.get(activityStack.size() - 1) instanceof TaskExecutingActivity)) {
                    robotInfo.setState(State.IDLE);
                    onCustomPowerDisconnected();
                }
            }
        } else if (chargeState == 2 || chargeState == 3) {
            if (!(this instanceof TaskExecutingActivity)) {
                showChargingView(powerLevel);
            }
            if (!robotInfo.isCharging()) {
                robotInfo.setLastChargeType(4);
                robotInfo.setLastPowerLevel(powerLevel);
                robotInfo.setLastChargeTime(System.currentTimeMillis());
                if (chargeState == 3) {
                    robotInfo.setTaskAbnormalFinishPrompt(null);
                    if ((!robotInfo.isSelfChecking() || !robotInfo.isMapping()) && EasyDialog.isShow()) {
                        EasyDialog.getInstance().dismiss();
                    }
                } else {
                    if (robotInfo.getChargeState() == 8 && EasyDialog.isShow()) {
                        EasyDialog.getInstance().dismiss();
                    }
                    mHandler.postDelayed(() -> ros.getCurrentPosition(), 1000);
                }
            }
            robotInfo.setState(State.CHARGING);
            onCustomPowerConnected();
            uploadLocalTask();
        } else if (chargeState > 8) {
            onCustomDockFailed();
        }
        Timber.w("充电状态 : %s", chargeState);
    }

    private void detailEmergencyButtonUpdateEvent(int emergencyButton) {
        robotInfo.setTaskAbnormalFinishPrompt(null);
        Timber.w("急停开关状态 %s", emergencyButton);
        if (emergencyButton == 0) {
            mHandler.removeCallbacks(chargeRunnable);
        }
        if (robotInfo.getEmergencyButton() == -1) return;
        onCustomEmergencyStopStateChange(emergencyButton);
    }

    public void detailAntiFallUpdateEvent(int antiFall) {
        if (antiFall == 1) {
            onCustomAntiFallEvent();
        }
        Timber.w("防跌 %s", antiFall);
    }


    @SuppressLint("CheckResult")
    protected void updateCallingMap() {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    if (robotInfo.isElevatorMode()) {
                        HashMap<String, String> all = FileMapUtils.getAll(Environment.getExternalStorageDirectory() + File.separator + PackageUtils.getAppName() + File.separator + Constants.KEY_BUTTON_MAP_WITH_ELEVATOR_PATH);
                        Timber.d("梯控模式下呼叫按钮绑定点位: %s", all);
                        HashMap<String, Pair<String, String>> pointsWithFloorMap = new HashMap<>();
                        if (!all.isEmpty()) {
                            for (Map.Entry<String, String> entry : all.entrySet()) {
                                pointsWithFloorMap.put(entry.getKey(), gson.fromJson(entry.getValue(), new TypeToken<Pair<String, String>>() {
                                }.getType()));
                            }
                        }
                        callingInfo.setCallingButtonMapWithElevator(pointsWithFloorMap);
                    } else {
                        HashMap<String, String> allButtonMap = FileMapUtils.getAll(Environment.getExternalStorageDirectory() + File.separator + PackageUtils.getAppName() + File.separator + Constants.KEY_BUTTON_MAP_PATH);
                        Timber.d("呼叫按钮绑定点位: %s", allButtonMap);
                        callingInfo.setCallingButtonMap(allButtonMap);
                        HashMap<String, String> allButtonMapWithQRCodeTask = FileMapUtils.getAll(Environment.getExternalStorageDirectory() + File.separator + PackageUtils.getAppName() + File.separator + Constants.KEY_BUTTON_MAP_WITH_QRCODE_TASK_PATH);
                        Timber.d("呼叫按钮绑定二维码任务: %s", allButtonMapWithQRCodeTask);
                        HashMap<String, List<Pair<Pair<String, String>, Pair<String, String>>>> qrCodeTaskMap = new HashMap<>();
                        if (!allButtonMapWithQRCodeTask.isEmpty()) {
                            for (Map.Entry<String, String> entry : allButtonMapWithQRCodeTask.entrySet()) {
                                qrCodeTaskMap.put(entry.getKey(), gson.fromJson(entry.getValue(), new TypeToken<List<Pair<Pair<String, String>, Pair<String, String>>>>() {
                                }.getType()));
                            }
                        }
                        callingInfo.setCallingButtonWithQRCodeModelTaskMap(qrCodeTaskMap);
                    }
                    emitter.onNext(true);
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(map -> Timber.v("已更新呼叫按钮"), throwable -> Timber.w(throwable, "呼叫按钮更新失败"));
    }

    @SuppressLint("CheckResult")
    private void onCustomClick(View view) {
        disposable = Observable.create(emitter -> {
                    if (restrictFrequency(400)) {
                        emitter.onError(new ClickFastException());
                    }
                    if (view instanceof TextView)
                        Timber.tag(getClass().getSimpleName()).w("点击[" + ((TextView) view).getText().toString() + "]");
                    int id = view.getId();
                    emitter.onNext(id);
                })
                .debounce(150, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(eventId -> {
                    updateLastClickTimeMills();
                    int mId = (int) eventId;
                    onCustomClickResult(mId);
                }, throwable -> {
                    updateLastClickTimeMills();
                    Timber.tag(getClass().getSimpleName()).w(throwable, "点击事件");
                    if (throwable instanceof ClickFastException) {
                        ToastUtils.showShortToast(getString(R.string.text_click_too_fast));
                    } else {
                        throw throwable;
                    }
                });
    }

    protected void onCustomClickResult(int id) {

    }

    private void destroyClickEvent() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private void onCallingTaskQueueUpdateEvent(CallingTaskQueueUpdateEvent event) {
        List<TaskDetails> taskDetailsList = event.getTaskDetailsList();
        Timber.w("update calling task queue : %s", taskDetailsList);
        if (!shouldResponseCallingEvent()) {
            if (FloatingCallingListView.isShow()) FloatingCallingListView.getInstance().close();
            return;
        }
        if (FloatingCallingListView.isShow()) {
            if (taskDetailsList.isEmpty()) {
                FloatingCallingListView.getInstance().close();
            } else {
                FloatingCallingListView.getInstance().updateQueueSize(taskDetailsList.size());
                FloatingCallingListView.getInstance().setListener(this);
            }
        } else if (!taskDetailsList.isEmpty()) {
            FloatingCallingListView.getInstance(mApp, taskDetailsList.size(), this).show();
        }
    }

    protected void onCustomChargeTask(ChargeTaskEvent event) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        if (RemoteTaskListDialog.isShow()) RemoteTaskListDialog.getInstance().dismiss();
        ReturningSetting returningSetting = robotInfo.getReturningSetting();
        if (!checkStartTask(event.getToken(), TaskMode.MODE_CHARGE)) {
            callingInfo.removeFirstCallingDetails();
            return;
        }
        if (!returningSetting.startTaskCountDownSwitch) {
            VoiceHelper.play("voice_will_start_charge_task");
            startChargeTask(event.getToken());
            return;
        }
        VoiceHelper.play("voice_will_start_charge_task_after_count_down");
        EasyDialog.getInstance(this).warnWithScheduledUpdateDetail(
                getString(R.string.text_receive_calling_count_down, returningSetting.startTaskCountDownTime, event.getPoint()),
                R.string.text_start_right_now,
                R.string.text_cancel,
                (dialog, id) -> {
                    EasyDialog.getInstance().dismiss();
                    robotInfo.setCountdownToTask(false);
                    if (id == R.id.btn_confirm) {
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_CHARGE)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startChargeTask(event.getToken());
                    } else {
                        callingInfo.removeFirstCallingDetails();
                        FloatingCountdown.getInstance(mApp, callingInfo.getCallingModeSetting().waitingTime, this).show();
                        CallingStateManager.INSTANCE.setCountingDownAfterCallingTaskEvent(true);
                        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(event.getToken(), TaskMode.MODE_CHARGE, StartTaskCode.CANCEL_MANUAL));
                        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                    }
                },
                new EasyDialog.OnTimeStampListener() {
                    @Override
                    public void onTimestamp(TextView title, TextView content, Button cancelBtn, Button neutralBtn, Button confirmBtn, int current) {
                        content.setText(getString(R.string.text_receive_calling_count_down, returningSetting.startTaskCountDownTime - current, event.getPoint()));
                    }

                    @Override
                    public void onTimeOut(EasyDialog dialog) {
                        dialog.dismiss();
                        robotInfo.setCountdownToTask(false);
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_CHARGE)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startChargeTask(event.getToken());
                    }
                },
                1000,
                returningSetting.startTaskCountDownTime * 1000
        );
        robotInfo.setCountdownToTask(true);
        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN);
    }

    protected void onCustomReturnTask(ReturnTaskEvent event) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        if (RemoteTaskListDialog.isShow()) RemoteTaskListDialog.getInstance().dismiss();
        ReturningSetting returningSetting = robotInfo.getReturningSetting();
        if (!checkStartTask(event.getToken(), TaskMode.MODE_START_POINT)) {
            callingInfo.removeFirstCallingDetails();
            return;
        }
        if (!returningSetting.startTaskCountDownSwitch) {
            VoiceHelper.play("voice_will_start_return_task");
            startReturnTask(event.getToken());
            return;
        }
        VoiceHelper.play("voice_will_start_return_task_after_count_down");
        EasyDialog.getInstance(this).warnWithScheduledUpdateDetail(
                getString(R.string.text_receive_calling_count_down, returningSetting.startTaskCountDownTime, event.getPoint()),
                R.string.text_start_right_now,
                R.string.text_cancel,
                (dialog, id) -> {
                    EasyDialog.getInstance().dismiss();
                    robotInfo.setCountdownToTask(false);
                    if (id == R.id.btn_confirm) {
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_START_POINT)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startReturnTask(event.getToken());
                    } else {
                        callingInfo.removeFirstCallingDetails();
                        FloatingCountdown.getInstance(mApp, callingInfo.getCallingModeSetting().waitingTime, this).show();
                        CallingStateManager.INSTANCE.setCountingDownAfterCallingTaskEvent(true);
                        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(event.getToken(), TaskMode.MODE_START_POINT, StartTaskCode.CANCEL_MANUAL));
                        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                    }
                },
                new EasyDialog.OnTimeStampListener() {
                    @Override
                    public void onTimestamp(TextView title, TextView content, Button cancelBtn, Button neutralBtn, Button confirmBtn, int current) {
                        content.setText(getString(R.string.text_receive_calling_count_down, returningSetting.startTaskCountDownTime - current, event.getPoint()));
                    }

                    @Override
                    public void onTimeOut(EasyDialog dialog) {
                        dialog.dismiss();
                        robotInfo.setCountdownToTask(false);
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_START_POINT)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startReturnTask(event.getToken());
                    }
                },
                1000,
                returningSetting.startTaskCountDownTime * 1000
        );
        robotInfo.setCountdownToTask(true);
        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN);
    }

    protected void onCustomCallingTask(CallingTaskEvent event) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        if (RemoteTaskListDialog.isShow()) RemoteTaskListDialog.getInstance().dismiss();
        Pair<String, String> point = event.getPoint();
        String target = point.getSecond();
        if (!TextUtils.isEmpty(point.getFirst())) {
            target = getString(R.string.text_floor_with_point, point.getFirst(), target);
        }
        if (!checkStartTask(event.getToken(), TaskMode.MODE_CALLING)) {
            callingInfo.removeFirstCallingDetails();
            return;
        }
        String finalTarget = target;
        ModeCallingSetting callingModeSetting = callingInfo.getCallingModeSetting();
        if (!callingModeSetting.startTaskCountDownSwitch) {
            VoiceHelper.play("voice_will_start_calling_task");
            startCallingTask(event.getToken(), event);
            return;
        }
        VoiceHelper.play("voice_will_start_calling_task_after_count_down");
        EasyDialog.getInstance(this).warnWithScheduledUpdateDetail(
                getString(R.string.text_receive_calling_count_down, callingModeSetting.startTaskCountDownTime, target),
                R.string.text_start_right_now,
                R.string.text_cancel,
                (dialog, id) -> {
                    EasyDialog.getInstance().dismiss();
                    robotInfo.setCountdownToTask(false);
                    if (id == R.id.btn_confirm) {
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_CALLING)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startCallingTask(event.getToken(), event);
                    } else {
                        callingInfo.removeFirstCallingDetails();
                        FloatingCountdown.getInstance(mApp, callingModeSetting.waitingTime, this).show();
                        CallingStateManager.INSTANCE.setCountingDownAfterCallingTaskEvent(true);
                        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(event.getToken(), TaskMode.MODE_CALLING, StartTaskCode.CANCEL_MANUAL));
                        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                    }
                },
                new EasyDialog.OnTimeStampListener() {
                    @Override
                    public void onTimestamp(TextView title, TextView content, Button cancelBtn, Button neutralBtn, Button confirmBtn, int current) {
                        content.setText(getString(R.string.text_receive_calling_count_down, callingModeSetting.startTaskCountDownTime - current, finalTarget));
                    }

                    @Override
                    public void onTimeOut(EasyDialog dialog) {
                        dialog.dismiss();
                        robotInfo.setCountdownToTask(false);
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_CALLING)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startCallingTask(event.getToken(), event);
                    }
                },
                1000,
                callingModeSetting.startTaskCountDownTime * 1000
        );
        robotInfo.setCountdownToTask(true);
        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN);
    }

    protected void onCustomNormalTask(NormalTaskEvent event) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        if (RemoteTaskListDialog.isShow()) RemoteTaskListDialog.getInstance().dismiss();
        ModeNormalSetting modeNormalSetting = robotInfo.getModeNormalSetting();
        if (!checkStartTask(event.getToken(), TaskMode.MODE_NORMAL)) {
            callingInfo.removeFirstCallingDetails();
            return;
        }
        if (!modeNormalSetting.startTaskCountDownSwitch) {
            VoiceHelper.play("voice_will_start_normal_task");
            startNormalTask(event.getToken(), event.getPointList());
            return;
        }
        VoiceHelper.play("voice_will_start_normal_task_after_count_down");
        EasyDialog.getInstance(this).warnWithScheduledUpdateDetail(
                getString(R.string.text_receive_normal_count_down, modeNormalSetting.startTaskCountDownTime),
                R.string.text_start_right_now,
                R.string.text_cancel,
                (dialog, id) -> {
                    EasyDialog.getInstance().dismiss();
                    robotInfo.setCountdownToTask(false);
                    if (id == R.id.btn_confirm) {
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_NORMAL)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startNormalTask(event.getToken(), event.getPointList());
                    } else {
                        callingInfo.removeFirstCallingDetails();
                        FloatingCountdown.getInstance(mApp, callingInfo.getCallingModeSetting().waitingTime, this).show();
                        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(event.getToken(), TaskMode.MODE_NORMAL, StartTaskCode.CANCEL_MANUAL));
                        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                    }
                },
                new EasyDialog.OnTimeStampListener() {
                    @Override
                    public void onTimestamp(TextView title, TextView content, Button cancelBtn, Button neutralBtn, Button confirmBtn, int current) {
                        content.setText(getString(R.string.text_receive_normal_count_down, modeNormalSetting.startTaskCountDownTime - current));
                    }

                    @Override
                    public void onTimeOut(EasyDialog dialog) {
                        dialog.dismiss();
                        robotInfo.setCountdownToTask(false);
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_NORMAL)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startNormalTask(event.getToken(), event.getPointList());
                    }
                },
                1000,
                modeNormalSetting.startTaskCountDownTime * 1000
        );
        robotInfo.setCountdownToTask(true);
        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN);
    }

    private void onTaskEvent(TaskEvent event) {
        if (event instanceof CallingTaskEvent) {
            onCustomCallingTask((CallingTaskEvent) event);
        } else if (event instanceof NormalTaskEvent) {
            onCustomNormalTask((NormalTaskEvent) event);
        } else if (event instanceof RouteTaskEvent) {
            onCustomRouteTask((RouteTaskEvent) event);
        } else if (event instanceof QRCodeTaskEvent) {
            onCustomQRCodeTask((QRCodeTaskEvent) event);
        } else if (event instanceof ReturnTaskEvent) {
            onCustomReturnTask((ReturnTaskEvent) event);
        } else if (event instanceof ChargeTaskEvent) {
            onCustomChargeTask((ChargeTaskEvent) event);
        }
    }

    protected void onCustomRouteTask(RouteTaskEvent event) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        if (RemoteTaskListDialog.isShow()) RemoteTaskListDialog.getInstance().dismiss();
        ModeRouteSetting modeRouteSetting = robotInfo.getModeRouteSetting();
        if (!checkStartTask(event.getToken(), TaskMode.MODE_ROUTE)) {
            callingInfo.removeFirstCallingDetails();
            return;
        }
        if (!modeRouteSetting.startTaskCountDownSwitch) {
            VoiceHelper.play("voice_will_start_route_task");
            startRouteTask(event);
            return;
        }
        VoiceHelper.play("voice_will_start_route_task_after_count_down");
        EasyDialog.getInstance(this).warnWithScheduledUpdateDetail(
                getString(R.string.text_receive_route_count_down, modeRouteSetting.startTaskCountDownTime),
                R.string.text_start_right_now,
                R.string.text_cancel,
                (dialog, id) -> {
                    EasyDialog.getInstance().dismiss();
                    robotInfo.setCountdownToTask(false);
                    if (id == R.id.btn_confirm) {
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_ROUTE)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startRouteTask(event);
                    } else {
                        callingInfo.removeFirstCallingDetails();
                        FloatingCountdown.getInstance(mApp, callingInfo.getCallingModeSetting().waitingTime, this).show();
                        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(event.getToken(), TaskMode.MODE_ROUTE, StartTaskCode.CANCEL_MANUAL));
                        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                    }
                },
                new EasyDialog.OnTimeStampListener() {
                    @Override
                    public void onTimestamp(TextView title, TextView content, Button cancelBtn, Button neutralBtn, Button confirmBtn, int current) {
                        content.setText(getString(R.string.text_receive_route_count_down, modeRouteSetting.startTaskCountDownTime - current));
                    }

                    @Override
                    public void onTimeOut(EasyDialog dialog) {
                        dialog.dismiss();
                        robotInfo.setCountdownToTask(false);
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_ROUTE)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startRouteTask(event);
                    }
                },
                1000,
                modeRouteSetting.startTaskCountDownTime * 1000
        );
        robotInfo.setCountdownToTask(true);
        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN);
    }

    protected void onCustomQRCodeTask(QRCodeTaskEvent event) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        if (RemoteTaskListDialog.isShow()) RemoteTaskListDialog.getInstance().dismiss();
        ModeQRCodeSetting modeQRCodeSetting = robotInfo.getModeQRCodeSetting();
        if (!modeQRCodeSetting.startTaskCountDownSwitch) {
            if (!checkStartTask(event.getToken(), TaskMode.MODE_QRCODE)) {
                callingInfo.removeFirstCallingDetails();
                return;
            }
            VoiceHelper.play("voice_will_start_qr_code_task");
            startQRCodeTask(event);
            return;
        }
        VoiceHelper.play("voice_will_start_qr_code_task_after_count_down");
        EasyDialog.getInstance(this).warnWithScheduledUpdateDetail(
                getString(R.string.text_receive_qrcode_count_down, modeQRCodeSetting.startTaskCountDownTime),
                R.string.text_start_right_now,
                R.string.text_cancel,
                (dialog, id) -> {
                    EasyDialog.getInstance().dismiss();
                    robotInfo.setCountdownToTask(false);
                    if (id == R.id.btn_confirm) {
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_QRCODE)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startQRCodeTask(event);
                    } else {
                        callingInfo.removeFirstCallingDetails();
                        FloatingCountdown.getInstance(mApp, callingInfo.getCallingModeSetting().waitingTime, this).show();
                        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(event.getToken(), TaskMode.MODE_QRCODE, StartTaskCode.CANCEL_MANUAL));
                        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                    }
                },
                new EasyDialog.OnTimeStampListener() {
                    @Override
                    public void onTimestamp(TextView title, TextView content, Button cancelBtn, Button neutralBtn, Button confirmBtn, int current) {
                        content.setText(getString(R.string.text_receive_qrcode_count_down, modeQRCodeSetting.startTaskCountDownTime - current));
                    }

                    @Override
                    public void onTimeOut(EasyDialog dialog) {
                        dialog.dismiss();
                        robotInfo.setCountdownToTask(false);
                        if (!checkStartTask(event.getToken(), TaskMode.MODE_QRCODE)) {
                            callingInfo.removeFirstCallingDetails();
                            return;
                        }
                        startQRCodeTask(event);
                    }
                },
                1000,
                modeQRCodeSetting.startTaskCountDownTime * 1000
        );
        robotInfo.setCountdownToTask(true);
        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN);
    }

    private boolean checkStartTask(String token, TaskMode taskMode) {
        if (!shouldResponseCallingEvent()) {
            ToastUtils.showShortToast(getString(R.string.voice_current_activity_cannot_response_online_task));
            CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(token, taskMode, StartTaskCode.NOT_SUPPORT_ONLINE_TASK));
            CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
            return false;
        }
        if (robotInfo.isEmergencyButtonDown()) {
            ToastUtils.showShortToast(getString(R.string.voice_scram_stop_turn_on));
            CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(token, taskMode, StartTaskCode.EMERGENCY_STOP_DOWN));
            CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
            return false;
        }
        if (robotInfo.isACCharging()) {
            ToastUtils.showShortToast(getString(R.string.voice_charging_and_can_not_move));
            CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(token, taskMode, StartTaskCode.AC_CHARGING));
            CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
            return false;
        }
        return true;
    }

    private void startQRCodeTask(QRCodeTaskEvent event) {
        robotInfo.setMode(TaskMode.MODE_QRCODE);
        Intent intent = new Intent(this, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, gson.toJson(event.getQrCodePointPairList()));
        intent.putExtra(Constants.TASK_TOKEN, event.getToken());
        startActivityForResult(this, intent, Constants.RESULT_CODE_OF_TASK);
        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(event.getToken(), TaskMode.MODE_QRCODE, StartTaskCode.TASK_START));
    }

    private void startRouteTask(RouteTaskEvent event) {
        robotInfo.setMode(TaskMode.MODE_ROUTE);
        Intent intent = new Intent(BaseActivity.this, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, event.getRoute());
        intent.putExtra(Constants.TASK_TOKEN, event.getToken());
        startActivityForResult(BaseActivity.this, intent, Constants.RESULT_CODE_OF_TASK);
        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(event.getToken(), TaskMode.MODE_ROUTE, StartTaskCode.TASK_START));
    }

    private void startNormalTask(String token, List<Pair<String, String>> points) {
        robotInfo.setMode(TaskMode.MODE_NORMAL);
        Intent intent = new Intent(this, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, gson.toJson(points));
        intent.putExtra(Constants.TASK_TOKEN, token);
        startActivityForResult(this, intent, Constants.RESULT_CODE_OF_TASK);
        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(token, TaskMode.MODE_NORMAL, StartTaskCode.TASK_START));
    }

    private void startCallingTask(String token, CallingTaskEvent event) {
        robotInfo.setMode(TaskMode.MODE_CALLING);
        Intent intent = new Intent(this, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, gson.toJson(event));
        startActivityForResult(this, intent, Constants.RESULT_CODE_OF_TASK);
        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(token, TaskMode.MODE_QRCODE, StartTaskCode.TASK_START));
    }

    private void startChargeTask(String token) {
        robotInfo.setMode(TaskMode.MODE_CHARGE);
        Intent intent = new Intent(this, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TOKEN, token);
        startActivityForResult(this, intent, Constants.RESULT_CODE_OF_TASK);
        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(token, TaskMode.MODE_CHARGE, StartTaskCode.TASK_START));
    }

    private void startReturnTask(String token) {
        robotInfo.setMode(TaskMode.MODE_START_POINT);
        Intent intent = new Intent(this, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TOKEN, token);
        startActivityForResult(this, intent, Constants.RESULT_CODE_OF_TASK);
        CallingStateManager.INSTANCE.setStartTaskCountDownEvent(new StartTaskCountDownEvent(token, TaskMode.MODE_START_POINT, StartTaskCode.TASK_START));
    }

    private void countdownToStartRouteTask(RouteWithPoints routeWithPoints) {
        Timber.w("路线任务循环执行 : %s", routeWithPoints.toString());
        EasyDialog.getInstance(this).warnWithScheduledUpdateDetail(
                getString(R.string.text_task_execute_again, TimeUtil.formatTimeHourMinSec(routeWithPoints.getExecuteAgainTime() * 1000L), routeWithPoints.getRouteName()),
                R.string.text_start_right_now,
                R.string.text_cancel,
                (dialog, id) -> {
                    isRouteTaskWaitingForStart = false;
                    dialog.dismiss();
                    robotInfo.setCountdownToTask(false);
                    if (id == R.id.btn_confirm) {
                        if (robotInfo.isEmergencyButtonDown()) {
                            EasyDialog.getInstance(this).warnError(getString(R.string.voice_scram_stop_turn_on));
                            CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                            return;
                        }
                        startRouteTask(routeWithPoints);
                    } else {
                        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                    }
                },
                new EasyDialog.OnTimeStampListener() {
                    @Override
                    public void onTimestamp(TextView title, TextView content, Button cancelBtn, Button neutralBtn, Button confirmBtn, int current) {
                        content.setText(getString(R.string.text_task_execute_again, TimeUtil.formatTimeHourMinSec((routeWithPoints.getExecuteAgainTime() - current) * 1000L), routeWithPoints.getRouteName()));
                    }

                    @Override
                    public void onTimeOut(EasyDialog dialog) {
                        isRouteTaskWaitingForStart = false;
                        dialog.dismiss();
                        robotInfo.setCountdownToTask(false);
                        if (robotInfo.isEmergencyButtonDown()) {
                            EasyDialog.getInstance(BaseActivity.this).warnError(getString(R.string.voice_scram_stop_turn_on));
                            CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                            return;
                        }
                        startRouteTask(routeWithPoints);
                    }
                },
                1000,
                routeWithPoints.getExecuteAgainTime() * 1000
        );
        robotInfo.setCountdownToTask(true);
        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.COUNTING_DOWN);
    }

    private void startRouteTask(RouteWithPoints routeWithPoints) {
        robotInfo.setMode(TaskMode.MODE_ROUTE);
        Intent intent = new Intent(this, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, routeWithPoints);
        startActivityForResult(this, intent, Constants.RESULT_CODE_OF_TASK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RESULT_CODE_OF_TASK) {
            String taskResultStr = data.getStringExtra(Constants.TASK_RESULT);
            Timber.w("呼叫点位 : %s resultCode : %s taskResult : %s ", callingInfo.getTaskDetailsList().toString(), resultCode, taskResultStr);
            if (taskResultStr != null) {
                TaskResult taskResult = gson.fromJson(taskResultStr, TaskResult.class);
                if (taskResult.getTaskMode() == TaskMode.MODE_CALLING) {
                    FloatingCountdown.getInstance(mApp, callingInfo.getCallingModeSetting().waitingTime, this).show();
                    CallingStateManager.INSTANCE.setCountingDownAfterCallingTaskEvent(true);
                }
                if (resultCode == 0) {
                    RouteWithPoints routeWithPoints = taskResult.getRouteWithPoints();
                    CommutingTimeSetting commutingTimeSetting = robotInfo.getCommutingTimeSetting();
                    if (routeWithPoints != null
                            && routeWithPoints.isExecuteAgainSwitch()
                            && !robotInfo.isLowPower()
                            && (!commutingTimeSetting.open || TimeUtil.isCurrentInTimeScope(commutingTimeSetting.workingTime, commutingTimeSetting.afterWorkTime))) {
                        isRouteTaskWaitingForStart = true;
                        countdownToStartRouteTask(routeWithPoints);
                    } else {
                        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                    }
                } else {
                    if (resultCode == -1 || resultCode == -3) {
                        if (!TextUtils.isEmpty(taskResult.getVoice())) {
                            VoiceHelper.play(taskResult.getVoice());
                        }
                        if (!TextUtils.isEmpty(taskResult.getPrompt())) {
                            CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.TASK_FINISHED_CAUSE_EXCEPTION);
                            EasyDialog.getInstance(this)
                                    .setAutoDismissEnable(false)
                                    .warn(taskResult.getPrompt(), (dialog, id) -> {
                                        dialog.dismiss();
                                        if (robotInfo.isSpaceShip() && robotInfo.isLiftModelInstalled()) {
                                            ros.getAltitudeState();
                                        }
                                    }, dialog -> {
                                        robotInfo.setTaskAbnormalFinishPrompt(null);
                                        Timber.w("任务失败提示消失");
                                        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                                    });
                        } else {
                            CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                        }
                    } else if (resultCode == 2) {
                        String code = taskResult.getPrompt();
                        String tip;
                        if (code.equals(Code.ELEVATOR_OFFLINE)) {
                            tip = getString(R.string.exception_elevator_offline);
                        } else if (code.equals(Code.ELEVATOR_NOT_FOUND)) {
                            tip = getString(R.string.exception_elevator_not_found);
                        } else if (code.equals(Code.NO_ELEVATOR_AVAILABLE)) {
                            tip = getString(R.string.exception_no_elevator_available);
                        } else if (code.equals(Code.PARSE_MQTT_HOST_FAILED)) {
                            tip = getString(R.string.exception_parse_mqtt_host_failed);
                        } else if (code.equals(Code.CONNECT_MQTT_FAILED)) {
                            tip = getString(R.string.exception_connect_mqtt_failed);
                        } else if (code.equals(Code.SUBSCRIBE_FAILED)) {
                            tip = getString(R.string.exception_subscribe_failed);
                        } else if (code.equals(Code.RESPONSE_BODY_NULL) || code.equals(Code.REQUEST_FAIL)) {
                            tip = getString(R.string.exception_response_body_null);
                        } else if (code.equals(Code.IO_EXCEPTION)) {
                            tip = getString(R.string.exception_request_io_exception);
                        } else if (code.equals(Code.JSON_SYNTAX_EXCEPTION)) {
                            tip = getString(R.string.exception_json_syntax_exception);
                        } else if (code.equals(Code.SUCCESS)) {
                            tip = getString(R.string.exception_get_thing_id_failed);
                        } else if (code.equals(Code.THING_NOT_ONLINE_EXCEPTION)) {
                            tip = getString(R.string.exception_thing_not_online);
                        } else {
                            tip = getString(R.string.exception_unknown_exception);
                        }
                        if (!TextUtils.isEmpty(tip)) {
                            CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.TASK_FINISHED_CAUSE_EXCEPTION);
                            EasyDialog.getInstance(this)
                                    .setAutoDismissEnable(false)
                                    .warn(tip, (dialog, id) -> {
                                        dialog.dismiss();
                                        if (robotInfo.isSpaceShip() && robotInfo.isLiftModelInstalled()) {
                                            ros.getAltitudeState();
                                        }
                                    }, dialog -> {
                                        robotInfo.setTaskAbnormalFinishPrompt(null);
                                        Timber.w("任务失败提示消失");
                                        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                                    });
                        } else {
                            CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);

                        }
                    } else {
                        CallingStateManager.INSTANCE.setTaskExecutingEvent(TaskExecutingCode.FREE);
                    }
                }
            }
        }
    }

    @Override
    public void onFinish() {
        Timber.w("count down finish after calling task");
        CallingStateManager.INSTANCE.setCountingDownAfterCallingTaskEvent(false);
    }

    @Override
    public void onFloatingCallingListClick() {
        if (EasyDialog.isShow() || RemoteTaskListDialog.isShow())
            return;
        RemoteTaskListDialog.getInstance(robotInfo.getLastActivity(), callingInfo.getRemoteTaskModelList(), new RemoteTaskListDialog.OnClickListener() {
            @Override
            public void onClear() {
                mHandler.postDelayed(() -> EasyDialog.getInstance(robotInfo.getLastActivity()).confirm(getString(R.string.text_if_delete_all_calling_points), (dialog, id) -> {
                    if (id == R.id.btn_confirm) {
                        callingInfo.removeAllCallingPoints();
                        if (FloatingCallingListView.isShow()) {
                            FloatingCallingListView.getInstance().close();
                        }
                    }
                    dialog.dismiss();
                }), 100);
            }

            @Override
            public void onDismiss() {

            }
        }).show();

    }

    private final Runnable selfCheckTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (robotInfo.isSelfChecking() && relocCount == 0) {
                robotInfo.setSelfChecking(false);
                CallingStateManager.INSTANCE.setSelfCheckEvent(false);
                ros.currentInfoControl(true);
                ros.positionAutoUploadControl(true);
                if (EasyDialog.isShow())
                    EasyDialog.getInstance().dismiss();
                Notifier.notify(new Msg(NotifyConstant.LOCATE_NOTIFY, "自检失败", "超时未检测到ROS开机", robotInfo.getROSHostname()));
                mHandler.postDelayed(() -> EasyDialog.getInstance(BaseActivity.this).warnError(getString(R.string.text_check_time_out_error)), 300);
            }
        }
    };

    @Override
    public void onCurrentMapEvent(CurrentMapEvent event) {
        robotInfo.setCurrentMapEvent(event);
    }

    @Override
    public void onIPEvent(@androidx.annotation.NonNull IPEvent event) {
        ros.setROSIP(event.getIpAddress());
        robotInfo.setROSWifi(event.getWifiName());
        robotInfo.setROSIPAddress(event.getIpAddress());
    }

    @Override
    public void onInitPoseEvent(@androidx.annotation.NonNull InitPoseEvent event) {
        onCustomInitPose(event.getPosition());
    }

    @Override
    public void onNavPoseEvent(@androidx.annotation.NonNull NavPoseEvent event) {
        if (!PointUtils.INSTANCE.isPositionCharged(robotInfo.getCurrentPosition(), event.getPosition())) {
            return;
        }
        Timber.v("坐标更新 : %s", Arrays.toString(event.getPosition()));
        robotInfo.setCurrentPosition(event.getPosition());
        onCustomPosition(event.getPosition());
    }

    @Override
    public void onApplyMapEvent(@androidx.annotation.NonNull ApplyMapEvent event) {
        if (event.isSuccess()) {
            robotInfo.setCurrentMapEvent(new CurrentMapEvent(event.getMap(), event.getAlias()));
        }
    }

    @Override
    public void onROSModelEvent(@androidx.annotation.NonNull ROSModelEvent event) {
        CallingStateManager.INSTANCE.setMappingModeEvent(event.getModel());
        if (ros.isModelRequest()) {
            ros.setModelRequest(false);
            robotInfo.setRosModel(event.getModel());
            if (event.getModel() == ROSModelEvent.NAVIGATION_MODEL) {
                return;
            }
        }
        if (robotInfo.isRepositioning() || robotInfo.isSwitchingMap()) {
            return;
        }
        if (event.getModel() != ROSModelEvent.NAVIGATION_MODEL) {
            if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable()) {
                EasyDialog.getInstance().dismiss();
            }

        } else if (robotInfo.getRosModel() != 1) {
            ros.currentInfoControl(true);
            ros.positionAutoUploadControl(true);
            if (EasyDialog.isShow() && EasyDialog.getInstance().isAutoDismissEnable()) {
                EasyDialog.getInstance().dismiss();
            }
        }
        robotInfo.setRosModel(event.getModel());
    }

    @Override
    public void onCoreDataEvent(@androidx.annotation.NonNull CoreDataEvent event) {
        if (!ROSFilter.isCoreDataDiff(event.getBaseData())) return;
        Timber.w(event.getBaseData());
        if (ROSFilter.isLevelDiff(event.getPowerData())) {
            detailPowerLevelUpdateEvent(event.getPowerData(), event.getChargeState());
            CallingStateManager.INSTANCE.setLowPowerEvent(robotInfo.isLowPower());
        }
        if (ROSFilter.isChargeStateDiff(event.getChargeState())) {
            detailChargeStateUpdateEvent(event.getChargeState(), event.getPowerData());
        }
        if (ROSFilter.isScramStateDiff(event.getEmergencyButton())) {
            detailEmergencyButtonUpdateEvent(event.getEmergencyButton());
        }
        if (ROSFilter.isAntiFallStateDiff(event.getAntiFallData())) {
            detailAntiFallUpdateEvent(event.getAntiFallData());
        }
        robotInfo.setLastCoreDataEvent(event);
        CallingStateManager.INSTANCE.setCoreDataEvent(event);
    }

    @Override
    public void onNavResultEvent(@androidx.annotation.NonNull NavigationResultEvent event) {
        int state = event.getState();
        if (state == NavigationState.FAILURE.getValue()
                || state == NavigationState.COMPLETE.getValue()
        ) {
            robotInfo.setNavigating(false);
            robotInfo.setNavigationCancelable(true);
            callingInfo.getHeartBeatInfo().setNavigating(false);
        }
        if (state == NavigationState.RECEIVE.getValue()) {
            robotInfo.setNavigationCancelable(true);
            onNavigationStartResult(event.getCode(), event.getName());
            return;
        }
        if (state == NavigationState.COMPLETE.getValue()) {
            onNavigationCompleteResult(event.getCode(), event.getName(), event.getMileage());
            return;
        }
        if (state == NavigationState.CANCEL.getValue()) {
            onNavigationCancelResult(event.getCode());
        }
    }

    @Override
    public void onPowerOffEvent(@androidx.annotation.NonNull PowerOffEvent event) {
        ScreenUtils.setImmersive(this);
        Board board = BoardFactory.create(this, Build.PRODUCT);
        board.shutdown(this);
        ros.stopListen();
    }

    @Override
    public void onSensorsEvent(@androidx.annotation.NonNull SensorsEvent event) {
        if (ROSFilter.isSensorDataDiff(event.getBaseData())) {
            Timber.w("硬件状态异常 : %s", event.getBaseData());
            robotInfo.setLastSensorsData(event);
            detailSensorState(event);
        }
    }

    @Override
    public void onWheelStatusEvent(@androidx.annotation.NonNull WheelStatusEvent event) {
        if (ROSFilter.isWheelWorkStateDiff(event.getState())) {
            detailWheelState(event);
        }
    }

    @Override
    public void onInitiativeLiftingModuleStateEvent(@androidx.annotation.NonNull InitiativeLiftingModuleStateEvent event) {
        CallingStateManager.INSTANCE.setInitiativeLiftingModuleStateEvent(event);
    }

}
