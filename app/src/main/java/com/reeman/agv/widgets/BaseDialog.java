package com.reeman.agv.widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.reeman.agv.R;

public class BaseDialog extends Dialog {
    public BaseDialog(@NonNull Context context) {
        super(context, R.style.common_dialog_style);
    }


    @Override
    public void show() {
        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | 0x00004000;
        window.getDecorView().setSystemUiVisibility(uiOptions);
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }
}
