package com.reeman.commons.board.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;

import com.reeman.commons.board.Board;
import com.reeman.commons.utils.WIFIUtils;


public class Board3568 implements Board {
    @Override
    public void navigationBarControl(Activity activity, boolean enable) {
        Intent intent = new Intent("com.android.yf_set_navigation_bar");
        intent.putExtra("value", enable ? 1 : 0);
        activity.sendBroadcast(intent);
    }

    @Override
    public void gestureNavigationBarControl(Context context, boolean enable) {
        Intent intent = new Intent("com.android.yf_set_swipe_navigation_bar");
        intent.putExtra("value", enable ? 1 : 0);
        context.sendBroadcast(intent);
    }

    @Override
    public void statusBarControl(Context context, boolean enable) {
        Intent intent = new Intent("com.android.yf_set_status_bar");
        intent.putExtra("value", enable ? 1 : 0);
        context.sendBroadcast(intent);
    }

    @Override
    public void secondaryScreenControl(Context context, boolean enable) {
        Intent intent = new Intent("com.android.set_lcd_bl2");
        intent.putExtra("value", enable ? 1 : 0);
        context.sendBroadcast(intent);
    }

    @Override
    public void primaryScreenControl(Context context, boolean enable) {
        Intent intent = new Intent("com.android.set_lcd_bl1");
        intent.putExtra("value", enable ? 1 : 0);
        context.sendBroadcast(intent);
    }

    @Override
    public void wifiControl(Context context, boolean isChecked) {
        Intent intent = new Intent("com.android.yf_set_wifi_switch");
        intent.putExtra("enable", isChecked ? "true" : "false");
        context.sendBroadcast(intent);
    }

    @Override
    public void connectWiFi(Context context, String name, String passwd, ScanResult hidden) {
        if (hidden == null){
            WIFIUtils.connectToHiddenWifi(context,name,passwd);
        }else {
            Intent intent = new Intent("com.android.yf_set_link_wifi");
            intent.putExtra("name", name);
            intent.putExtra("password", passwd);
            context.sendBroadcast(intent);
        }
    }

    @Override
    public void shutdown(Context context) {
        Intent intent = new Intent("com.android.yf_shutdown");
        context.sendBroadcast(intent);
    }
}
