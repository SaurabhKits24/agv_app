package com.bigkoo.pickerview.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class ScreenUtils {

    private static final int SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED = 0x00004000;

    /**
     * 获取屏幕对角线尺寸
     *
     * @param context
     * @return
     */
    public static double getDevicePhysicalSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        double x = Math.pow(width, 2);
        double y = Math.pow(height, 2);
        double diagonal = Math.sqrt(x + y);
        double size = diagonal / (double) dm.densityDpi;
        return size;
    }

    public static void setImmersive(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    public static void hideBottomUIMenu(Activity activity) {
        // 隐藏虚拟按键，并且全屏
        // for new api versions.
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED;
        decorView.setSystemUiVisibility(uiOptions);
    }



    public static void hideNavigationBar(Activity activity) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN;
        window.setAttributes(params);
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
        if (Build.VERSION.SDK_INT >= 19) {
            uiFlags |= 0x00001000;  //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower thatn 19, use magic number directly for higher API target level
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        activity.getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }
}
