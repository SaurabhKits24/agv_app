package com.reeman.commons.board.impl;

import android.app.Activity;
import android.app.smdt.SmdtManager;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.reeman.commons.board.Board;
import com.reeman.commons.utils.WIFIUtils;


public class Board3399 implements Board {

    private final SmdtManager mSmdtManager;

    public Board3399(Context context) {
        mSmdtManager = SmdtManager.create(context);
    }

    @Override
    public void navigationBarControl(Activity activity, boolean enable) {
        mSmdtManager.smdtSetStatusBar(activity, enable);
    }

    @Override
    public void gestureNavigationBarControl(Context context, boolean enable) {
        mSmdtManager.setGestureBar(enable);  //同时控制状态栏和导航栏
    }

    @Override
    public void statusBarControl(Context context, boolean enable) {

    }

    @Override
    public void secondaryScreenControl(Context context, boolean enable) {
        mSmdtManager.smdtSetEDPBackLight(enable ? 1 : 0);
    }

    @Override
    public void primaryScreenControl(Context context, boolean enable) {
        mSmdtManager.smdtSetLcdBackLight(enable ? 1 : 0);
    }

    @Override
    public void wifiControl(Context context, boolean isChecked) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(isChecked);
    }

    @Override
    public void connectWiFi(Context context, String name, String passwd, ScanResult hidden) {
        if (hidden == null){
            WIFIUtils.connectToHiddenWifi(context,name,passwd);
        }else {
            WIFIUtils.connect(context, name, passwd, hidden);
        }
    }

    @Override
    public void shutdown(Context context) {
        mSmdtManager.shutDown();
    }
}
