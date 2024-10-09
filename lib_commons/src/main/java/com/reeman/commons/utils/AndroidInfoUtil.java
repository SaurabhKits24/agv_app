package com.reeman.commons.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class AndroidInfoUtil {

    @SuppressLint("HardwareIds")
    public static String getSerialNumber(){
        return Build.SERIAL;
    }

    @SuppressLint("HardwareIds")
    public static String getMacAddress(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return getMacAddressByInterface();
        }else {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return wifiManager.getConnectionInfo().getMacAddress();
        }
    }

    private static String getMacAddressByInterface() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (intf.getName().equalsIgnoreCase("wlan0")) {
                    byte[] mac = intf.getHardwareAddress();
                    if (mac != null) {
                        StringBuilder sb = new StringBuilder();
                        for (byte b : mac) {
                            sb.append(String.format("%02X:", b));
                        }
                        if (sb.length() > 0) {
                            sb.deleteCharAt(sb.length() - 1);
                        }
                        return sb.toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context context){
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
