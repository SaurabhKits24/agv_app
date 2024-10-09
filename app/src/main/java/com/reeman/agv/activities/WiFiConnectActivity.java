package com.reeman.agv.activities;

import static com.reeman.agv.base.BaseApplication.mApp;
import static com.reeman.agv.base.BaseApplication.ros;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import kotlin.Pair;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kyleduo.switchbutton.SwitchButton;
import com.reeman.agv.R;
import com.reeman.agv.adapter.WifiItemAdapter;
import com.reeman.agv.base.BaseActivity;
import com.reeman.commons.board.Board;
import com.reeman.commons.board.BoardFactory;
import com.reeman.commons.constants.Constants;
import com.reeman.agv.contract.WiFiConnectContract;
import com.reeman.agv.presenter.impl.WiFiConnectPresenter;
import com.reeman.agv.utils.ScreenUtils;
import com.reeman.commons.event.IPEvent;
import com.reeman.commons.event.WifiConnectResultEvent;
import com.reeman.commons.utils.SpManager;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.utils.VoiceHelper;
import com.reeman.commons.utils.WIFIUtils;
import com.reeman.agv.widgets.EasyDialog;
import com.reeman.agv.widgets.WifiAuthDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WiFiConnectActivity extends BaseActivity implements
        WiFiConnectContract.View, SwipeRefreshLayout.OnRefreshListener,
        WifiItemAdapter.OnItemClickListener, WifiAuthDialog.OnViewClickListener {

    private SwitchButton swButton;
    private TextView tvRos;
    private TextView tvAndroid;
    private SwipeRefreshLayout refreshLayout;
    private WifiBroadcastReceiver receiver;
    private WiFiConnectContract.Presenter presenter;
    private TextView tvWiFiStatus;
    private WifiItemAdapter adapter;
    private WifiManager wifiManager;
    private WifiAuthDialog wifiAuthDialog;
    private String ssid, pwd;
    private Map<String, String> wifiMap = null;

    private Board mBoard;

    private final Intent intent = new Intent();

    private boolean startFromElevatorSettingFragment;

    private boolean isCheckConnectedWifiSSID = false;


    @Override
    protected int getLayoutRes() {
        return R.layout.activity_wifi_connect;
    }

    @Override
    protected void initData() {
        presenter = new WiFiConnectPresenter(this);
        startFromElevatorSettingFragment = getIntent().hasExtra(Constants.START_FROM_ELEVATOR_SETTING);
        mBoard = BoardFactory.create(this, Build.PRODUCT);
        ArrayList<Activity> activityStack = robotInfo.getActivityStack();
        if (activityStack.size() >= 2) {
            Activity activity = activityStack.get(activityStack.size() - 2);
            if (activity instanceof LanguageSelectActivity || activity instanceof SplashActivity) {
                VoiceHelper.play("voice_please_connect_wifi");
            }
        }
    }


    @Override
    protected void initCustomView() {
        setOnClickListeners(R.id.tv_back);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        $(R.id.iv_add).setOnClickListener(this);
        swButton = $(R.id.switch_wifi_status);
        swButton.setChecked(wifiManager.isWifiEnabled());
        swButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mBoard != null) {
                mBoard.wifiControl(this, isChecked);
            }
        });
        tvWiFiStatus = $(R.id.tv_wifi_status);
        tvRos = $(R.id.tv_ros_wifi_name);
        tvAndroid = $(R.id.tv_android_wifi_name);

        //下拉刷新
        refreshLayout = $(R.id.refresh_layout);
        Resources resources = getResources();
        refreshLayout.setColorSchemeColors(
                resources.getColor(R.color.purple_700),
                resources.getColor(R.color.purple_500),
                resources.getColor(R.color.purple_200));
        refreshLayout.setOnRefreshListener(this);

        //WIFI列表
        RecyclerView rvWiFiList = $(R.id.rv_wifi_list);
        rvWiFiList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration decor = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable drawable = ResourcesCompat.getDrawable(resources, R.drawable.drawable_divider, getTheme());
        decor.setDrawable(drawable);
        rvWiFiList.addItemDecoration(decor);
        adapter = new WifiItemAdapter();
        adapter.setOnItemClickListener(this);
        rvWiFiList.setAdapter(adapter);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            ScreenUtils.hideBottomUIMenu(this);
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        ros.getHostIP();
        registerNetworkReceiver();
        updateScanResults(wifiManager.getScanResults());
        String wpStr = SpManager.getInstance().getString(Constants.WIFI_PASSWORD, "");
        if (!"".equals(wpStr)) {
            wifiMap = new Gson().fromJson(wpStr, new TypeToken<Map<String, String>>() {
            }.getType());
        }
    }

    private void registerNetworkReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        receiver = new WifiBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onItemClick(ScanResult scanResult) {
        if (wifiAuthDialog == null) {
            wifiAuthDialog = new WifiAuthDialog(this);
            wifiAuthDialog.setOnViewClickListener(this);
        }
        wifiAuthDialog.setName(scanResult.SSID);
        wifiAuthDialog.setHidden(scanResult);
        String passwd;
        TextInputEditText wifiPassword = wifiAuthDialog.getWifiPassword();
        if ((wifiMap != null && (passwd = wifiMap.get(scanResult.SSID)) != null)) {
            wifiPassword.setText(passwd);
            wifiPassword.setSelection(passwd.length());
        } else {
            wifiPassword.setText("");
        }
        wifiPassword.requestFocus();
        wifiAuthDialog.show();
    }

    @Override
    public void onRefresh() {
        presenter.onRefresh(this);
    }

    @Override
    public void showRefreshFailedView() {
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void showStartRefreshView() {
        refreshLayout.setRefreshing(true);
    }

    @Override
    public void showConnectingView(String prompt) {
        mHandler.postDelayed(() -> EasyDialog.getLoadingInstance(this).loading(prompt), 200);
    }

    @Override
    public void showConnectTimeOutView(String prompt) {
        if (EasyDialog.isShow())
            EasyDialog.getInstance().dismiss();
        ToastUtils.showShortToast(prompt);
    }


    @Override
    public void onConnectSuccess() {
        if (wifiMap == null) wifiMap = new HashMap<>();
        wifiMap.put(ssid, pwd);
        SpManager.getInstance().edit().putString(Constants.WIFI_PASSWORD, new Gson().toJson(wifiMap)).apply();
        ToastUtils.showShortToast(getString(R.string.voice_wifi_connect_success));
        VoiceHelper.play("voice_wifi_connect_success", () -> {
            if (EasyDialog.isShow())
                EasyDialog.getInstance().dismiss();
            boolean isNetworkGuide = SpManager.getInstance().getBoolean(Constants.KEY_IS_NETWORK_GUIDE, false);
            if (isNetworkGuide) {
                finish();
            } else if (getCallingActivity() != null) {
                intent.putExtra(Constants.WIFI_RESULT, wifiManager.getConnectionInfo());
                setResult(0, intent);
                finish();
            } else {
                SpManager.getInstance().edit().putBoolean(Constants.KEY_IS_NETWORK_GUIDE, true).apply();
                BaseActivity.startupAndClearStack(WiFiConnectActivity.this, AliasSettingActivity.class);
            }
        });
    }


    @Override
    public void onConnectFailed() {
        ToastUtils.showShortToast(getString(R.string.text_wifi_connect_failed));
        if (EasyDialog.isShow())
            EasyDialog.getInstance().dismiss();
    }

    @Override
    public void showTryConnectROSFirst() {
        if (EasyDialog.isShow())
            EasyDialog.getInstance().dismiss();
        WIFIUtils.disconnectWifi(this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                EasyDialog.getInstance(WiFiConnectActivity.this).confirm(getString(R.string.text_connect_ros_wifi_first), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        if (id == R.id.btn_confirm) {
                            presenter.connectROSWiFi(WiFiConnectActivity.this);
                        }
                        dialog.dismiss();
                    }
                });
            }
        }, 200);
    }

    @Override
    public void showAndroidConnectSuccess() {
        if (EasyDialog.isShow())
            EasyDialog.getInstance().dismiss();
        if (startFromElevatorSettingFragment){
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.RESULT_WIFI_INFO,new Gson().toJson(new Pair<>(ssid,pwd)));
            setResult(Constants.RESULT_OF_SUCCESS, resultIntent);
            finish();
            return;
        }
        mHandler.postDelayed(() -> EasyDialog.getInstance(WiFiConnectActivity.this).confirm(getString(R.string.text_android_wifi_connect_success), new EasyDialog.OnViewClickListener() {
            @Override
            public void onViewClick(Dialog dialog, int id) {
                if (id == R.id.btn_confirm) {
                    presenter.connectROSWiFi(WiFiConnectActivity.this);
                }
                dialog.dismiss();
            }
        }), 200);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void onCustomClickResult(int id) {
        super.onCustomClickResult(id);
        if (id == R.id.tv_back) {
            if (getCallingActivity() != null) {
                intent.putExtra(Constants.WIFI_RESULT, WIFIUtils.isNetworkConnected(this));
                setResult(0, intent);
            }
            if (!SpManager.getInstance().getBoolean(Constants.KEY_IS_NETWORK_GUIDE, false)) {
                mApp.exit();
            } else {
                finish();
            }
        } else if (id == R.id.iv_add) {
            if (wifiAuthDialog == null) {
                wifiAuthDialog = new WifiAuthDialog(this);
                wifiAuthDialog.setOnViewClickListener(this);
            }
            wifiAuthDialog.setName("");
            wifiAuthDialog.setPassword("");
            wifiAuthDialog.show();
        }
    }

    @Override
    public void onWifiConnectEvent(@NonNull WifiConnectResultEvent event) {
        if (event.getResult() == WifiConnectResultEvent.CONNECTING)return;
        if (event.getResult() == WifiConnectResultEvent.CONNECTED) {
            isCheckConnectedWifiSSID = true;
            ros.getHostIP();
        }else {
            presenter.onWiFiEvent(this, false);
        }
    }

    @Override
    public void onIPEvent(@NonNull IPEvent event) {
        tvRos.setText(getString(R.string.text_current_ros_wifi, TextUtils.isEmpty(event.getWifiName()) ? getString(R.string.text_not_connected) : event.getWifiName()));
        if (isCheckConnectedWifiSSID){
            isCheckConnectedWifiSSID = false;
            presenter.onWiFiEvent(this, Objects.equals(event.getWifiName(), ssid));
        }
    }

    @Override
    public void onViewClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_cancel:
                if (wifiAuthDialog != null)
                    wifiAuthDialog.dismiss();
                break;
            case R.id.btn_login:
                ssid = wifiAuthDialog.getWifiName().getText().toString();
                pwd = wifiAuthDialog.getWifiPassword().getText().toString();
                if (TextUtils.isEmpty(ssid)) {
                    ToastUtils.showShortToast(getString(R.string.text_wifi_name_can_not_be_empty));
                    return;
                }
                wifiAuthDialog.dismiss();
                presenter.auth(this, ssid, pwd, wifiAuthDialog.getHidden());
                break;
        }
    }

    public class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    intent.putExtra(Constants.WIFI_RESULT, info);
                    if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                        tvAndroid.setText("");
                        Log.w("network", "DISCONNECTED");
                    } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String connectedSSID = wifiInfo.getSSID().replace("\"", "");
                        tvAndroid.setText(getString(R.string.text_current_android_wifi, connectedSSID));
                        if (Objects.equals(ssid,connectedSSID)){
                            presenter.onAndroidConnected(WiFiConnectActivity.this);
                        }
                        Log.w("network", "CONNECTED");
                    } else if (info.getState().equals(NetworkInfo.State.CONNECTING)) {
                        Log.w("network", "CONNECTING");
                    }
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_DISABLED:
                            ToastUtils.showShortToast(getString(R.string.text_closed));
                            refreshLayout.setRefreshing(false);
                            swButton.setEnabled(true);
                            swButton.setChecked(false);
                            tvWiFiStatus.setText(getString(R.string.text_closed));
                            adapter.setResult(null);
                            break;
                        case WifiManager.WIFI_STATE_DISABLING:
                            swButton.setEnabled(false);
                            tvWiFiStatus.setText(R.string.text_wifi_disabling);
                            break;
                        case WifiManager.WIFI_STATE_ENABLING:
                            swButton.setEnabled(false);
                            tvWiFiStatus.setText(R.string.text_wifi_enabling);
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            ToastUtils.showShortToast(getString(R.string.text_opened));
                            swButton.setEnabled(true);
                            tvWiFiStatus.setText(getString(R.string.text_opened));
                            presenter.startScanWiFi(wifiManager);
                            break;
                    }
                    break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    int supplicantError = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    if (supplicantState == SupplicantState.DISCONNECTED && supplicantError == WifiManager.ERROR_AUTHENTICATING) {
                        if (EasyDialog.isShow())
                            EasyDialog.getInstance().dismiss();
                        mHandler.postDelayed(() -> EasyDialog.getInstance(WiFiConnectActivity.this).warnError(getString(R.string.text_wifi_password_error)), 200);
                        presenter.onWiFiPasswordError(WiFiConnectActivity.this);
                    }
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    //ToastUtils.showShortToast(getString(R.string.text_already_update));
                    @SuppressLint("MissingPermission") List<ScanResult> scanResults = wifiManager.getScanResults();
                    updateScanResults(scanResults);
                    refreshLayout.setRefreshing(false);
                    break;
            }
        }
    }

    private void updateScanResults(List<ScanResult> scanResults){
        Collections.sort(scanResults, (r1, r2) -> r2.level - r1.level);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            scanResults.removeIf(scanResult -> TextUtils.isEmpty(scanResult.SSID));
        } else {
            Iterator<ScanResult> iterator = scanResults.iterator();
            while (iterator.hasNext()) {
                ScanResult next = iterator.next();
                if (TextUtils.isEmpty(next.SSID)) {
                    iterator.remove();
                }
            }
        }
        adapter.setResult(scanResults);
    }

    @Override
    protected void onCustomEmergencyStopStateChange(int emergencyStopState) {

    }

    @Override
    public void onSoftKeyboardClosed() {
        ScreenUtils.hideBottomUIMenu(this);
    }
}