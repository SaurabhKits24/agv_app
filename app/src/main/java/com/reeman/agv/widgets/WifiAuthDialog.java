package com.reeman.agv.widgets;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.reeman.agv.R;
import com.reeman.commons.utils.SoftKeyboardStateWatcher;

public class WifiAuthDialog extends BaseDialog implements View.OnClickListener, SoftKeyboardStateWatcher.SoftKeyboardStateListener {

    private TextInputEditText wifiName;
    private TextInputEditText wifiPassword;
    private Button cancelBtn;
    private Button loginBtn;
    private SoftKeyboardStateWatcher watcher;

    public void setName(String name) {
        wifiName.setText(name);
        wifiName.setSelection(name.length());
    }

    public void setPassword(String password) {
        wifiPassword.setText(password);
        wifiName.setSelection(wifiPassword.length());
    }

    public TextInputEditText getWifiName() {
        return wifiName;
    }

    public TextInputEditText getWifiPassword() {
        return wifiPassword;
    }

    public Button getLoginBtn() {
        return loginBtn;
    }

    public Button getCancelBtn() {
        return cancelBtn;
    }

    public WifiAuthDialog(@NonNull Context context) {
        super(context);
        init(context);

    }

    private void init(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.layout_wifi_auth, null);
        wifiName = root.findViewById(R.id.et_wifi_name);
        wifiPassword = root.findViewById(R.id.et_wifi_password);
        cancelBtn = root.findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(this);
        loginBtn = root.findViewById(R.id.btn_login);
        loginBtn.setOnClickListener(this);

        setTitle(R.string.text_wifi_auth);
        setContentView(root);
        watcher = new SoftKeyboardStateWatcher(getWindow().getDecorView());
        watcher.addSoftKeyboardStateListener(this);
    }

    private OnViewClickListener onViewClickListener;

    public void setOnViewClickListener(OnViewClickListener onViewClickListener) {
        this.onViewClickListener = onViewClickListener;
    }

    @Override
    public void onClick(View v) {
        if (onViewClickListener != null) onViewClickListener.onViewClick(v);
    }

    public void setHidden(ScanResult scanResult) {
        this.wifiName.setTag(scanResult);
    }

    public ScanResult getHidden() {
        Object tag = this.wifiName.getTag();
        if (tag == null)return null;
        return (ScanResult) tag;
    }

    @Override
    public void onSoftKeyboardOpened(int keyboardHeightInPx) {

    }

    @Override
    public void onSoftKeyboardClosed() {
        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | 0x00004000;
        window.getDecorView().setSystemUiVisibility(uiOptions);
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public interface OnViewClickListener {
        /**
         * wifi连接弹窗
         * @param v
         */
        void onViewClick(View v);
    }

}
