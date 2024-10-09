package com.reeman.agv.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.reeman.agv.R;


public class ToastUtils {
    private static Toast sToast;
    private static Context sContext;
    private static TextView tvToastContent;

    public static void init(Context context) {
        sContext = context;
    }

    public static void showShortToast(String content) {
        if (sToast == null) {
            sToast = new Toast(sContext);
            sToast.setDuration(Toast.LENGTH_SHORT);
            View rootToast = LayoutInflater.from(sContext).inflate(R.layout.layout_simple_toast, null);
            sToast.setView(rootToast);
            tvToastContent = rootToast.findViewById(R.id.tv_toast_content);
        }
        tvToastContent.setText(content);
        sToast.show();
    }

    public static void showLongToast(String content) {
        if (sToast == null) {
            sToast = new Toast(sContext);
            sToast.setDuration(Toast.LENGTH_LONG);
            View rootToast = LayoutInflater.from(sContext).inflate(R.layout.layout_simple_toast, null);
            sToast.setView(rootToast);
            tvToastContent = rootToast.findViewById(R.id.tv_toast_content);
        }
        tvToastContent.setText(content);
        sToast.show();
    }
}
