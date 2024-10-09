package com.reeman.agv.widgets;


import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.reeman.agv.R;

import timber.log.Timber;

public class ChargingWindows {

    private static ChargingWindows instance;

    private boolean isShow;

    public View mView;

    private TextView chargingView;

    private ChargingWindowsCallback callback;

    public boolean isShow() {
        return isShow;
    }

    public void setCallback(ChargingWindowsCallback callback) {
        this.callback = callback;
    }

    public static ChargingWindows getInstance() {
        if (instance == null) {
            synchronized (ChargingWindows.class) {
                if (instance == null) {
                    instance = new ChargingWindows();
                }
            }
        }
        return instance;
    }

    public void show(Application application, int powerLevel, ChargingWindowsCallback callback) {
        try {
            if (mView != null) dismiss(application);
            this.callback = callback;
            WindowManager manager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams para = new WindowManager.LayoutParams();
            para.height = WindowManager.LayoutParams.MATCH_PARENT;
            para.width = WindowManager.LayoutParams.MATCH_PARENT;
            para.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            para.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;

            if (mView == null) {
                mView = LayoutInflater.from(application).inflate(R.layout.layout_charging, null);
                mView.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
                chargingView = mView.findViewById(R.id.tv_power_level);
            }
            chargingView.setText(String.format("%s%%", powerLevel));
            manager.addView(mView, para);
            mView.setOnClickListener(v -> {
                if (this.callback != null) {
                    this.callback.onClick();
                }
            });
            isShow = true;
        } catch (Exception e) {
            Timber.w(e, "显示充电提示失败");
            isShow = false;
        }
    }

    public void update(int powerLevel) {
        if (chargingView != null) {
            chargingView.setText(String.format("%s%%", powerLevel));
        }
    }

    public void dismiss(Application application) {
        Timber.d("移除电量显示");
        try {
            instance = null;
            if (mView == null) return;
            WindowManager manager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
            manager.removeView(mView);
            mView = null;
            isShow = false;
        } catch (Exception e) {
            Timber.w(e, "移除充电屏保失败");
        }
    }

    public interface ChargingWindowsCallback {
        void onClick();
    }


}
