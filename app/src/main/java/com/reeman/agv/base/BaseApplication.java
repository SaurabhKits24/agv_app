package com.reeman.agv.base;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.reeman.agv.calling.mqtt.MqttClient;
import com.reeman.agv.BuildConfig;
import com.reeman.agv.calling.service.CallingService;
import com.reeman.agv.utils.FileLoggingTree;
import com.reeman.commons.constants.Constants;
import com.reeman.agv.crash.CustomCrashCallback;
import com.reeman.ros.ROSController;
import com.reeman.agv.receiver.RobotReceiver;
import com.reeman.agv.service.RobotService;
import com.reeman.commons.state.RobotInfo;
import com.reeman.commons.utils.MMKVManager;
import com.reeman.agv.utils.PackageUtils;
import com.reeman.commons.utils.SpManager;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.commons.utils.WIFIUtils;
import com.reeman.agv.widgets.FloatingCallingListView;
import com.reeman.agv.widgets.FloatingCountdown;
import com.reeman.dao.repository.DbRepository;
import com.reeman.dao.repository.db.AppDataBase;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;
import xcrash.XCrash;

public class BaseApplication extends Application {

    public static BaseApplication mApp;
    public static ROSController ros;
    public static String appVersion;
    public static String macAddress;
    public static DbRepository dbRepository;
    public static boolean isFirstEnter = true;
    private Intent intent;

    private Intent callingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        MMKVManager.init(this);
        XLog.init();
        Timber.plant(new FileLoggingTree(
                        BuildConfig.DEBUG ? Log.VERBOSE : Log.WARN,
                        Environment.getExternalStorageDirectory().getPath(),
                        BuildConfig.APP_LOG_DIR,
                        BuildConfig.DEBUG,
                        Arrays.asList(
                                BuildConfig.APP_LOG_DIR,
                                BuildConfig.CRASH_LOG_DIR,
                                BuildConfig.ELEVATOR_DIR,
                                com.reeman.commons.BuildConfig.WHEEL_INFO_DIR,
                                com.reeman.commons.BuildConfig.BATTERY_REPORT_DIR,
                                com.reeman.serialport.BuildConfig.LOG_POWER_BOARD
                        )

                )
        );
        CustomCrashCallback customCrashCallback = new CustomCrashCallback();
        XCrash.init(
                this,
                new XCrash
                        .InitParameters()
                        .setAppVersion(BuildConfig.VERSION_NAME)
                        .setJavaCallback(customCrashCallback)
                        .setNativeCallback(customCrashCallback)
                        .setAnrCallback(customCrashCallback)
        );

        appVersion = PackageUtils.getVersion(this);
        macAddress = WIFIUtils.getMacAddress(this);
        dbRepository = DbRepository.getInstance(AppDataBase.getInstance(this));
        ToastUtils.init(this);
        SpManager.init(this, Constants.KEY_SP_NAME);
        registerReceiver(new RobotReceiver(), new RobotReceiver.RobotIntentFilter());
        intent = new Intent(this, RobotService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    public void startCallingService() {
        if (callingIntent == null) {
            callingIntent = new Intent(this, CallingService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(callingIntent);
            } else {
                startService(callingIntent);
            }
        }
    }

    public void stopCallingService() {
        if (callingIntent != null) {
            stopService(callingIntent);
            callingIntent = null;
        }
    }

    public void exit() {
        MqttClient mqttClient = MqttClient.getInstance();
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
        }
        if (FloatingCountdown.isShow()) {
            FloatingCountdown.getInstance().close();
        }
        if (FloatingCallingListView.isShow()) {
            FloatingCallingListView.getInstance().close();
        }
        if (ros != null) {
            ros.stopListen();
        }
        Completable.timer(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                    for (Activity activity : RobotInfo.INSTANCE.getActivityStack()) {
                        activity.finish();
                    }
                    RobotInfo.INSTANCE.clearActivityStack();
                    if (intent != null) {
                        stopService(intent);
                        intent = null;
                    }
                    stopCallingService();
                    MMKVManager.sync();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

}
