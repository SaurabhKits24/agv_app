package com.reeman.agv.presenter.impl;


import static com.reeman.agv.base.BaseApplication.ros;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.reeman.agv.R;
import com.reeman.commons.board.Board;
import com.reeman.commons.board.BoardConstants;
import com.reeman.commons.board.BoardFactory;
import com.reeman.commons.constants.Constants;
import com.reeman.agv.contract.WiFiConnectContract;
import com.reeman.commons.utils.SpManager;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.utils.VoiceHelper;
import com.reeman.commons.utils.WIFIUtils;

public class WiFiConnectPresenter implements WiFiConnectContract.Presenter {

    private int currentPhase = PHASE_INIT;
    private static final int PHASE_INIT = 0;
    private static final int PHASE_ANDROID_CONNECTING = 1;
    private static final int PHASE_ROS_CONNECTING = 2;
    private int androidConnectSuccessCount;


    private final WiFiConnectContract.View view;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable connectTimeOutTask;
    private String wifiPass;
    private String wifiName;

    private int rosConnectCount;


    public WiFiConnectPresenter(WiFiConnectContract.View view) {
        this.view = view;
    }

    public void startScanWiFi(WifiManager manager) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean success = manager.startScan();
                if (!success) {
                    view.showRefreshFailedView();
                }
            }
        }, 100);
        view.showStartRefreshView();
    }


    @Override
    public void onRefresh(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiManager.getWifiState();
        if (wifiState == WifiManager.WIFI_STATE_DISABLED || wifiState == WifiManager.WIFI_STATE_DISABLING) {
            ToastUtils.showShortToast(context.getString(R.string.text_open_wifi_first));
            view.showRefreshFailedView();
            return;
        }
        startScanWiFi(wifiManager);
    }

    @Override
    public void auth(Context context, String name, String passwd, ScanResult hidden) {
        wifiName = name;
        wifiPass = passwd;
        String wifiSSID = WIFIUtils.getConnectWifiSSID(context);
        if ("".equals(wifiSSID)) wifiSSID = context.getString(R.string.text_not_connected);
        currentPhase = PHASE_ANDROID_CONNECTING;
        VoiceHelper.play("voice_connecting_wifi");
        String prompt = context.getString(R.string.voice_connecting_wifi);
        view.showConnectingView(prompt);
        if (wifiSSID != null && wifiSSID.equals(name)) {
            if (!Build.PRODUCT.startsWith("YF")) {
                androidConnectSuccessCount++;
            }
            mHandler.postDelayed(() -> onAndroidConnected(context), 2000);
            return;
        }
        Board board = BoardFactory.create(context, Build.PRODUCT);
        board.connectWiFi(context, name, passwd, hidden);
        connectTimeOutTask = new TimeOutTask(context.getString(R.string.text_android_wifi_connect_time_out));
        mHandler.postDelayed(connectTimeOutTask, 30_000);
    }

    @Override
    public void onAndroidConnected(Context context) {
        rosConnectCount = 0;
        if (currentPhase != PHASE_ANDROID_CONNECTING) return;
        androidConnectSuccessCount++;
        if (androidConnectSuccessCount >= BoardConstants.WIFI_CONNECT_THRESHOLD) {
            mHandler.removeCallbacks(connectTimeOutTask);
            boolean isNetworkGuide = SpManager.getInstance().getBoolean(Constants.KEY_IS_NETWORK_GUIDE, false);
            if (isNetworkGuide) {
                view.showAndroidConnectSuccess();
            } else {
                connectTimeOutTask = new TimeOutTask(context.getString(R.string.text_ros_wifi_connect_time_out));
                mHandler.postDelayed(connectTimeOutTask, 30_000);
                currentPhase = PHASE_ROS_CONNECTING;
                ros.connectROSWifi(wifiName, TextUtils.isEmpty(wifiPass)?"null":wifiPass);
                rosConnectCount++;
            }
        }
    }

    @Override
    public void connectROSWiFi(Context context) {
        rosConnectCount = 0;
        String prompt = context.getString(R.string.voice_connecting_wifi);
        VoiceHelper.play("voice_connecting_wifi");
        mHandler.postDelayed(() -> view.showConnectingView(prompt), 300);
        view.showConnectingView(prompt);
        connectTimeOutTask = new TimeOutTask(context.getString(R.string.text_ros_wifi_connect_time_out));
        mHandler.postDelayed(connectTimeOutTask, 30_000);
        currentPhase = PHASE_ROS_CONNECTING;
        ros.connectROSWifi(wifiName, wifiPass);
        rosConnectCount++;
    }

    @Override
    public void onWiFiPasswordError(Context context) {
        mHandler.removeCallbacks(connectTimeOutTask);
        WIFIUtils.disconnectWifi(context);

    }

    @Override
    public void onWiFiEvent(Context context, boolean isConnect) {
        mHandler.removeCallbacks(connectTimeOutTask);
        currentPhase = PHASE_INIT;
        androidConnectSuccessCount = 0;
        if (isConnect) {
            view.onConnectSuccess();
            return;
        }
        if (rosConnectCount < 2) {
            mHandler.postDelayed(() -> {
                ros.connectROSWifi(wifiName, wifiPass);
                rosConnectCount++;
            }, 5000);
            return;
        }
        rosConnectCount = 0;
        view.onConnectFailed();
    }

    private class TimeOutTask implements Runnable {
        private final String prompt;

        public TimeOutTask(String prompt) {
            this.prompt = prompt;
        }

        @Override
        public void run() {
            if (currentPhase == PHASE_ANDROID_CONNECTING) {
                view.showTryConnectROSFirst();
                return;
            }
            rosConnectCount = 0;
            currentPhase = PHASE_INIT;
            androidConnectSuccessCount = 0;
            view.showConnectTimeOutView(prompt);
        }
    }
}
