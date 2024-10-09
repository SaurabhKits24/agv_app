package com.reeman.commons.board.impl;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.View;

import com.reeman.commons.board.Board;
import com.reeman.commons.utils.WIFIUtils;


public class Board3128 implements Board {
    private static final int SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED = 0x00004000;

    @Override
    public void navigationBarControl(Activity activity, boolean enable) {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions;
        if (enable) {
            uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        } else {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED;
        }
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void gestureNavigationBarControl(Context context, boolean enable) {

    }

    @Override
    public void statusBarControl(Context context, boolean enable) {

    }

    @Override
    public void secondaryScreenControl(Context context, boolean enable) {

    }

    @Override
    public void primaryScreenControl(Context context, boolean enable) {

    }

    @Override
    public void shutdown(Context context) {
        try {
            Runtime.getRuntime().exec("reboot -p");
        } catch (Exception e) {

        }
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
}
