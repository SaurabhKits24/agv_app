package com.reeman.agv.utils;


import static com.reeman.agv.base.BaseApplication.mApp;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageUtils {
    public static String getVersion(Context context) {
        String versionName;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "1.0.0";
        }
        return versionName;
    }

    public static String getAppName(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.loadLabel(context.getPackageManager()).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static String getAppName() {
        try {
            ApplicationInfo appInfo = mApp.getPackageManager().getApplicationInfo(mApp.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.loadLabel(mApp.getPackageManager()).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
