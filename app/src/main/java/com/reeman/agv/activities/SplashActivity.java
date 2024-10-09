package com.reeman.agv.activities;


import static com.reeman.agv.base.BaseApplication.isFirstEnter;
import static com.reeman.agv.base.BaseApplication.mApp;
import static com.reeman.agv.base.BaseApplication.ros;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Pair;

import com.google.gson.Gson;
import com.reeman.agv.BuildConfig;
import com.reeman.agv.R;
import com.reeman.agv.base.BaseActivity;
import com.reeman.agv.calling.CallingInfo;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.event.HostnameEvent;
import com.reeman.commons.event.SensorsEvent;
import com.reeman.commons.event.VersionInfoEvent;
import com.reeman.commons.settings.CommutingTimeSetting;
import com.reeman.commons.settings.DoorControlSetting;
import com.reeman.agv.calling.setting.ModeCallingSetting;
import com.reeman.commons.settings.ModeNormalSetting;
import com.reeman.commons.settings.ModeQRCodeSetting;
import com.reeman.commons.settings.ModeRouteSetting;
import com.reeman.commons.settings.ReturningSetting;
import com.reeman.commons.state.NavigationMode;
import com.reeman.commons.utils.AESUtil;
import com.reeman.commons.utils.AndroidInfoUtil;
import com.reeman.agv.contract.SplashContract;
import com.reeman.ros.ROSController;
import com.reeman.agv.presenter.impl.SplashPresenter;
import com.reeman.commons.settings.ElevatorSetting;
import com.reeman.commons.utils.MMKVManager;
import com.reeman.ros.callback.ROSCallback;
import com.reeman.ros.filter.ROSFilter;
import com.reeman.agv.utils.PackageUtils;
import com.reeman.agv.utils.ScreenUtils;
import com.reeman.commons.utils.SpManager;
import com.reeman.agv.widgets.EasyDialog;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class SplashActivity extends BaseActivity implements SplashContract.View {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private SplashPresenter presenter;
    private volatile boolean receiveDataFormROS = false;
    private volatile boolean receiveSensorData = true;
    private volatile boolean receiveHostname = false;
    private static final int REQUEST_CODE = 1001;

    private boolean isFirst = true;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initCustomView() {
    }

    @Override
    protected void initData() {
        presenter = new SplashPresenter(this);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isFirst) return;
            isFirst = false;
            if (!Settings.canDrawOverlays(this)) {
                ScreenUtils.setImmersive(this);
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return;
            }
            if (!Settings.System.canWrite(this)) {
                ScreenUtils.setImmersive(this);
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            }
        }
        ScreenUtils.hideBottomUIMenu(this);
        checkPermission();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            checkROS();
            return;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException();
        }
        String[] permissions = packageInfo.requestedPermissions;
        List<String> unGrantedPerms = new ArrayList<>();
        for (String perm : permissions) {
            if ("android.permission.SYSTEM_OVERLAY_WINDOW".equals(perm) ||
                    "android.permission.WRITE_SETTINGS".equals(perm) ||
                    "android.permission.REQUEST_INSTALL_PACKAGES".equals(perm)) continue;
            if (checkSelfPermission(perm) == PackageManager.PERMISSION_DENIED) {
                unGrantedPerms.add(perm);
            }
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            unGrantedPerms.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (unGrantedPerms.isEmpty()) {
            checkROS();
            return;
        }
        String[] unGrantedPermsArr = new String[unGrantedPerms.size()];
        for (int i = 0; i < unGrantedPerms.size(); i++) {
            unGrantedPermsArr[i] = unGrantedPerms.get(i);
        }
        requestPermissions(unGrantedPermsArr, REQUEST_CODE);
    }

    private void checkROS() {
        if (isFirstEnter) {
            isFirstEnter = false;
            ros = ROSController.INSTANCE;
            try {
                ros.init(BuildConfig.APP_LOG_DIR, BuildConfig.CRASH_LOG_DIR, BuildConfig.ELEVATOR_DIR, com.reeman.serialport.BuildConfig.LOG_POWER_BOARD);
                ros.registerListener(this);
                ros.heartBeat();
                ros.getHostname();
                ros.closeLaserReport();
                mHandler.postDelayed(mCommunicationRunnable, 10_000);
                mApp.startCallingService();
            } catch (Exception e) {
                e.printStackTrace();
                Timber.e(e, "串口初始化失败");
                EasyDialog.getInstance(SplashActivity.this).warn(getString(R.string.text_communicate_failed_with_ros), (dialog, id) -> {
                    dialog.dismiss();
                    ScreenUtils.setImmersive(this);
                    mApp.exit();
                });
            }
        } else {
            ros.heartBeat();
            ros.getHostname();
            ROSFilter.isHFLSVersionDataDiff("resetData");
            ROSFilter.isWheelWorkStateDiff(-1);
            ROSFilter.isSensorDataDiff("resetData");
            mHandler.postDelayed(mCommunicationRunnable, 10_000);
            mApp.startCallingService();
        }
    }

    private final Runnable mCommunicationRunnable = () -> {
        EasyDialog.getInstance(SplashActivity.this).warn(getString(R.string.text_communicate_failed_with_ros), (dialog, id) -> {
            dialog.dismiss();
            ScreenUtils.setImmersive(this);
            mApp.exit();
        });
    };

    private void showInsufficientPermissionDialog() {
        EasyDialog.getInstance(this).confirm(getString(R.string.text_insufficient_permission_detail), (dialog, id) -> {
            dialog.dismiss();
            if (id == R.id.btn_confirm) {
                isFirst = true;
                navigateToGrantPermission();
            } else {
                ScreenUtils.setImmersive(this);
                mApp.exit();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Timber.w("onRequestPermissionsResult");
        if (requestCode != REQUEST_CODE) return;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) continue;
            showInsufficientPermissionDialog();
            return;
        }
        checkROS();
    }

    /**
     * 跳转应用详情界面授权
     */
    private void navigateToGrantPermission() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    @Override
    public void onHostNameEvent(@NonNull HostnameEvent event) {
        robotInfo.setROSHostname(event.getHostname());
        callingInfo.getHeartBeatInfo().setHostname(event.getHostname());
        String alias = SpManager.getInstance().getString(Constants.KEY_ROBOT_ALIAS, event.getHostname());
        callingInfo.getHeartBeatInfo().setAlias(alias);
        robotInfo.setRobotAlias(alias);
        receiveHostname = true;
        startUp();
    }

    @Override
    public void onVersionEvent(@NonNull VersionInfoEvent event) {
        if (ROSFilter.isHFLSVersionDataDiff(event.getBaseData())) {
            Observable.create((ObservableOnSubscribe<Void>) emitter -> {
                        Log.w("======", event.getBaseData());
                        ros.cpuPerformance();
                        Gson gson = new Gson();
                        SharedPreferences sharedPreferences = SpManager.getInstance();
                        robotInfo.setVersionEvent(event);
                        int currentNavigationModel = sharedPreferences.getInt(Constants.KEY_NAVIGATION_MODEL, NavigationMode.autoPathMode);
                        String normalModeConfigStr = sharedPreferences.getString(Constants.KEY_NORMAL_MODE_CONFIG, null);
                        String routeModeConfigStr = sharedPreferences.getString(Constants.KEY_ROUTE_MODE_CONFIG, null);
                        String qrCodeModeConfigStr = sharedPreferences.getString(Constants.KEY_QRCODE_MODE_CONFIG, null);
                        String returningConfigStr = sharedPreferences.getString(Constants.KEY_RETURNING_CONFIG, null);
                        String elevatorSettingJsonStr = sharedPreferences.getString(Constants.KEY_ELEVATOR_SETTING, null);
                        String doorControlSettingJsonStr = sharedPreferences.getString(Constants.KEY_DOOR_CONTROL, null);
                        String commutingTimeSettingStr = sharedPreferences.getString(Constants.KEY_COMMUTING_TIME_SETTING, null);
                        String callingSettingConfigStr = SpManager.getInstance().getString(Constants.KEY_CALLING_MODE_CONFIG, null);
                        robotInfo.setAutoChargePowerLevel(sharedPreferences.getInt(Constants.KEY_LOW_POWER, Constants.DEFAULT_LOW_POWER));
                        robotInfo.setStopNearbyOpen(sharedPreferences.getBoolean(Constants.KEY_STOP_NEARBY_CONTROL, true));
                        robotInfo.setPointScrollShow(sharedPreferences.getBoolean(Constants.KEY_POINT_SHOW_MODE, true));
                        if (!TextUtils.isEmpty(normalModeConfigStr)) {
                            robotInfo.setModeNormalSetting(gson.fromJson(normalModeConfigStr, ModeNormalSetting.class));
                        }
                        if (!TextUtils.isEmpty(routeModeConfigStr)) {
                            robotInfo.setModeRouteSetting(gson.fromJson(routeModeConfigStr, ModeRouteSetting.class));
                        }
                        if (!TextUtils.isEmpty(qrCodeModeConfigStr)) {
                            ModeQRCodeSetting modeQRCodeSetting = gson.fromJson(qrCodeModeConfigStr, ModeQRCodeSetting.class);
                            robotInfo.setModeQRCodeSetting(modeQRCodeSetting);
                            CallingInfo.INSTANCE.setQRCodeTaskUseCallingButton(modeQRCodeSetting.callingBind);
                        }
                        if (!TextUtils.isEmpty(returningConfigStr)) {
                            robotInfo.setReturningSetting(gson.fromJson(returningConfigStr, ReturningSetting.class));
                        }
                        if (!TextUtils.isEmpty(elevatorSettingJsonStr)) {
                            robotInfo.setElevatorSetting(gson.fromJson(elevatorSettingJsonStr, ElevatorSetting.class));
                            callingInfo.getHeartBeatInfo().setElevatorMode(robotInfo.getElevatorSetting().open);
                        }
                        if (!TextUtils.isEmpty(doorControlSettingJsonStr)) {
                            robotInfo.setDoorControlSetting(gson.fromJson(doorControlSettingJsonStr, DoorControlSetting.class));
                        }
                        if (!TextUtils.isEmpty(commutingTimeSettingStr)) {
                            robotInfo.setCommutingTimeSetting(gson.fromJson(commutingTimeSettingStr, CommutingTimeSetting.class));
                        }
                        if (TextUtils.isEmpty(callingSettingConfigStr)) {
                            ModeCallingSetting modeCallingSetting = ModeCallingSetting.getDefault();
                            try {
                                String encrypt = AESUtil.encrypt("a123456", PackageUtils.getVersion(this) + AndroidInfoUtil.getSerialNumber() + System.currentTimeMillis());
                                String mKey = encrypt.replaceAll("[^A-Za-z0-9]", "").substring(0, 8);
                                String token = encrypt.substring(8);
                                List<String> tokens = new ArrayList<>();
                                tokens.add(token);
                                modeCallingSetting.key = new Pair<>(mKey, tokens);
                                sharedPreferences.edit().putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(modeCallingSetting)).apply();
                            } catch (GeneralSecurityException e) {
                                Timber.w(e, "生成key失败");
                            }
                            callingInfo.setCallingModeSetting(modeCallingSetting);
                        } else {
                            ModeCallingSetting modeCallingSetting = gson.fromJson(callingSettingConfigStr, ModeCallingSetting.class);
                            Timber.w("key : %s ", modeCallingSetting.key);
                            callingInfo.setCallingModeSetting(modeCallingSetting);
                        }
                        robotInfo.setNavigationMode(currentNavigationModel);
                        emitter.onComplete();
                    })
                    .delay(3, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Void>() {
                        @Override
                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@io.reactivex.rxjava3.annotations.NonNull Void unused) {

                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            receiveDataFormROS = true;
                            updateCallingMap();
                            startUp();
                        }
                    });
        }
    }

    @Override
    protected void detailSensorState(SensorsEvent event) {
        receiveSensorData = true;
        startUp();
    }


    private synchronized void startUp() {
        if (receiveHostname && receiveDataFormROS && receiveSensorData) {
            if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
            mHandler.removeCallbacks(mCommunicationRunnable);
            presenter.startup(SplashActivity.this);
        }
    }
}