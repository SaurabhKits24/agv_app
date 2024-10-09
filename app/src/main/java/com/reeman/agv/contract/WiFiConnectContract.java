package com.reeman.agv.contract;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.reeman.agv.presenter.IPresenter;
import com.reeman.agv.view.IView;

public interface WiFiConnectContract {
    interface Presenter extends IPresenter {

        void startScanWiFi(WifiManager manager);

        void onRefresh(Context context);

        void auth(Context context, String name, String passwd, ScanResult hidden);

        void onWiFiEvent(Context context, boolean isConnect);

        /**
         * 安卓网络连接成功
         * @param context
         */
        void onAndroidConnected(Context context);

        /**
         * 连接导航网络
         * @param context
         */
        void connectROSWiFi(Context context);

        /**
         * 密码错误
         * @param context
         */
        void onWiFiPasswordError(Context context);

    }

    interface View extends IView {

        void showRefreshFailedView();

        void showStartRefreshView();

        void showConnectingView(String prompt);

        void showConnectTimeOutView(String prompt);

        void onConnectSuccess();

        void onConnectFailed();

        /**
         * 先连接导航网络
         */
        void showTryConnectROSFirst();

        /**
         * 安卓网络连接成功
         */
        void showAndroidConnectSuccess();
    }
}
