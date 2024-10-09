package com.reeman.commons.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class WIFIUtils {

    @SuppressLint("HardwareIds")
    public static String getMacAddress(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        return wi.getMacAddress() == null ? "" : wi.getMacAddress().trim();
    }


    @SuppressLint("MissingPermission")
    public static WifiConfiguration configWifiInfo(Context context, String SSID, String password, int type) {
        WifiConfiguration config = null;
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                config = existingConfig;
                break;
            }
        }
        if (config == null) {
            config = new WifiConfiguration();
        }
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // 分为三种情况：0没有密码1用wep加密2用wpa加密
        if (type == 0) {// WIFICIPHER_NOPASSwifiCong.hiddenSSID = false;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == 1) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 2) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 获取热点的加密类型
     */
    private static int getType(ScanResult scanResult) {
        int type;
        if (scanResult.capabilities.contains("WPA"))
            type = 2;
        else if (scanResult.capabilities.contains("WEP"))
            type = 1;
        else
            type = 0;
        return type;
    }


    public static void connect(Context context, String ssid, String password, ScanResult scanResult) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = configWifiInfo(context, ssid, password, getType(scanResult));
        int netId = config.networkId;
        if (netId == -1) {
            netId = wifiManager.addNetwork(config);
        }
        wifiManager.disableNetwork(netId);
        wifiManager.enableNetwork(netId, true);
    }

    public static void connectToHiddenWifi(Context context,String ssid, String password) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", password);
        wifiConfig.hiddenSSID = true;
        wifiConfig.status = WifiConfiguration.Status.ENABLED;
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        int netId = -1;
        for (WifiConfiguration configuredNetwork : wifiManager.getConfiguredNetworks()) {
            if (configuredNetwork.SSID != null && configuredNetwork.SSID.equals(String.format("\"%s\"", ssid))) {
                netId = configuredNetwork.networkId;
                break;
            }
        }

        if (netId == -1) {
            netId = wifiManager.addNetwork(wifiConfig);
        }
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }



    public static String getConnectWifiSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) return null;
        String ssid = wifiInfo.getSSID();
        if (TextUtils.equals(ssid, WifiManager.UNKNOWN_SSID)) {
            return "";
        }
        return ssid.replace("\"", "");
    }

    public static void disconnectWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();

        @SuppressLint("MissingPermission") List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration config : configuredNetworks) {
                if (config.SSID != null && config.SSID.equals(ssid)) {
                    // 移除指定的网络配置
                    Log.w("network","remove :"+config.networkId);
                    wifiManager.removeNetwork(config.networkId);
                    wifiManager.disconnect();
                    break;
                }
            }
        }
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            // 获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空
            if (networkInfo != null)
                return networkInfo.isAvailable();
        }
        return false;
    }

    public static String getIpAddress(Context context) {

        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!manager.isWifiEnabled()) {
            return "127.0.0.1";
        }

        WifiInfo wifiinfo = manager.getConnectionInfo();

        return intToIp(wifiinfo.getIpAddress());
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
}
