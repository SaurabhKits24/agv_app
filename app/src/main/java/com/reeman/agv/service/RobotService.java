package com.reeman.agv.service;


import static com.reeman.agv.base.BaseApplication.ros;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import com.reeman.agv.base.BaseApplication;
import com.reeman.agv.request.ServiceFactory;
import com.reeman.commons.event.VersionInfoEvent;
import com.reeman.commons.event.AndroidNetWorkEvent;
import com.reeman.commons.event.GreenButtonEvent;
import com.reeman.commons.eventbus.EventBus;
import com.reeman.commons.model.request.StateRecord;
import com.reeman.agv.request.url.API;
import com.reeman.commons.state.RobotInfo;
import com.youngfeel.yf_gpio_manager;


import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Response;
import timber.log.Timber;

public class RobotService extends Service {

    private final RobotInfo robotInfo = RobotInfo.INSTANCE;

    private int lastResult = 1;

    Runnable task = () -> {
        if (ros != null) {
            ros.heartBeat();
        }
        String hostname = robotInfo.getROSHostname();
        if (TextUtils.isEmpty(hostname)) return;
        try {
            VersionInfoEvent versionEvent = robotInfo.getVersionEvent();
            if (versionEvent == null) return;
            if (ros != null) {
                ros.heartBeat();
            }
            int chargePlug = 0;
            if (robotInfo.isCharging()) {
                chargePlug = 4;
            }
            StateRecord record = new StateRecord(0,
                    robotInfo.getPowerLevel(),
                    chargePlug,
                    robotInfo.getEmergencyButton(),
                    robotInfo.getState().ordinal(),
                    versionEvent.getSoftVer(),
                    BaseApplication.appVersion,
                    3,
                    "",
                    BaseApplication.macAddress,
                    System.currentTimeMillis(),
                    "v1.1",
                    "");
            Response<Map<String, Object>> execute = ServiceFactory.getRobotService().heartbeat(API.heartbeatAPI(hostname), record).execute();
            Log.w("上传状态：", record + "\n" + execute);

        } catch (Exception e) {
            e.printStackTrace();
        }
    };


    @SuppressLint("CheckResult")
    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = createNotification();
            startForeground(1001, notification);
        }
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(task, 10, 15, TimeUnit.SECONDS);
        registerNetworkReceiver();
        if (Build.PRODUCT.startsWith("YF3566")) {
            int fd = yf_gpio_manager.open();
            Timber.d("3566,单独处理绿色按钮");
            Observable.interval(100, 100, TimeUnit.MILLISECONDS)
                    .subscribe(tick -> {
                        int ret = yf_gpio_manager.get_gpio1_value(fd);
                        if (ret == lastResult) return;
                        lastResult = ret;
                        if (ret != 1) {
                            Timber.d("3566绿色按钮按下");
                            EventBus.INSTANCE.sendEvent(GreenButtonEvent.class,new GreenButtonEvent());
                        }
                    });
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "robot server";
            String channelName = "robot server";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "robot server")
                .setContentTitle("robot server")
                .setContentText("Service is running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }

    private void registerNetworkReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        WifiBroadcastReceiver receiver = new WifiBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
    }

    public static class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.INSTANCE.sendEvent(AndroidNetWorkEvent.class, new AndroidNetWorkEvent(intent));
            String action = intent.getAction();
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    Timber.w("DISCONNECTED");
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    Timber.w("CONNECTED");
                }
            }
        }
    }
}
