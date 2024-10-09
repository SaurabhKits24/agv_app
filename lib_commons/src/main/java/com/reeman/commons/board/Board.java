package com.reeman.commons.board;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;

public interface Board {

    void navigationBarControl(Activity activity, boolean enable);

    void gestureNavigationBarControl(Context context, boolean enable);

    void statusBarControl(Context context, boolean enable);

    void secondaryScreenControl(Context context, boolean enable);

    void primaryScreenControl(Context context, boolean enable);

    void shutdown(Context context);

    void wifiControl(Context context, boolean isChecked);

    void connectWiFi(Context context, String name, String passwd, ScanResult hidden);
}
