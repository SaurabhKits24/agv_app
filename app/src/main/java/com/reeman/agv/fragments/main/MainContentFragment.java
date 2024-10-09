package com.reeman.agv.fragments.main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reeman.agv.R;
import com.reeman.agv.activities.SettingActivity;
import com.reeman.agv.activities.WiFiConnectActivity;
import com.reeman.agv.base.BaseFragment;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.event.CurrentMapEvent;
import com.reeman.commons.event.VersionInfoEvent;
import com.reeman.commons.state.TaskMode;
import com.reeman.agv.utils.PackageUtils;
import com.reeman.commons.utils.SpManager;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.utils.ViewUtils;
import com.reeman.commons.utils.WIFIUtils;
import com.reeman.agv.widgets.EasyDialog;
import com.reeman.agv.widgets.RobotInfoDialog;

public class MainContentFragment extends BaseFragment implements EasyDialog.OnViewClickListener{
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_main_content;
    }

    private final OnMainContentClickListener listener;

    public MainContentFragment(OnMainContentClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        $(R.id.btn_mode_normal).setOnClickListener(this);
        $(R.id.btn_mode_route).setOnClickListener(this);
        $(R.id.btn_mode_qrcode).setOnClickListener(this);
        $(R.id.ibtn_connect_wifi).setOnClickListener(this);
        $(R.id.ibtn_charge).setOnClickListener(this);
        $(R.id.ibtn_product).setOnClickListener(this);
        $(R.id.ibtn_setting).setOnClickListener(this);
        $(R.id.ibtn_more_info).setOnClickListener(this);
        ViewUtils.resetViewHeight($(R.id.fragment_main_content),
                R.id.ibtn_connect_wifi,
                R.id.ibtn_charge,
                R.id.ibtn_product,
                R.id.ibtn_setting,
                R.id.ibtn_more_info);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        switch (id){
            case R.id.btn_mode_normal:
                listener.onModeClick(TaskMode.MODE_NORMAL);
                break;
            case R.id.btn_mode_route:
                listener.onModeClick(TaskMode.MODE_ROUTE);
                break;
            case R.id.btn_mode_qrcode:
                listener.onModeClick(TaskMode.MODE_QRCODE);
                break;
            case  R.id.ibtn_charge:
                listener.onModeClick(TaskMode.MODE_CHARGE);
                break;
            case  R.id.ibtn_product:
                listener.onModeClick(TaskMode.MODE_START_POINT);
                break;
            case R.id.ibtn_connect_wifi:
                startActivity(new Intent(requireActivity(), WiFiConnectActivity.class));
                break;
            case R.id.ibtn_setting:

                onGotoSetting();
                break;
            case R.id.ibtn_more_info:

                CurrentMapEvent currentMapEvent = robotInfo.getCurrentMapEvent();
                VersionInfoEvent versionEvent = robotInfo.getVersionEvent();
                String connectWifiSSID = WIFIUtils.getConnectWifiSSID(requireActivity());
                if ("".equals(connectWifiSSID))connectWifiSSID = getString(R.string.text_not_connected);
                new RobotInfoDialog(requireActivity(),
                        robotInfo.getROSWifi(),
                        robotInfo.getROSIPAddress(),
                        connectWifiSSID,
                        WIFIUtils.getIpAddress(requireActivity()),
                        versionEvent == null ? "unknown" : versionEvent.getSoftVer(),
                        versionEvent == null ? "unknown" : versionEvent.getHardwareVer(),
                        PackageUtils.getVersion(requireActivity()),
                        TextUtils.isEmpty(currentMapEvent.getAlias()) ? currentMapEvent.getMap() : currentMapEvent.getAlias()
                ).show();
                break;
        }
    }

    private void onGotoSetting() {
        int settingPassword = SpManager.getInstance().getInt(Constants.KEY_SETTING_PASSWORD_CONTROL, Constants.KEY_DEFAULT_SETTING_PASSWORD_CONTROL);
        if (settingPassword == 1) {
            EasyDialog.newCustomInstance(requireActivity(), R.layout.layout_input_setting_password).showInputPasswordDialog(this);
        } else {
            startActivity(new Intent(requireActivity(), SettingActivity.class));
        }
    }

    @Override
    public void onViewClick(Dialog dialog, int id) {
        if (id == R.id.btn_confirm) {
            EditText editText = (EditText) EasyDialog.getInstance().getView(R.id.et_password);
            String str = editText.getText().toString();
            if (TextUtils.equals(str, Constants.KEY_SETTING_PASSWORD)) {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), SettingActivity.class));
            } else {
                ToastUtils.showShortToast(getString(R.string.text_password_error));
            }
        } else {
            dialog.dismiss();
        }
    }

    public interface OnMainContentClickListener{
        void onModeClick(TaskMode mode);
    }
}
