package com.reeman.agv.fragments.setting;


import static com.reeman.agv.base.BaseApplication.ros;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reeman.agv.R;
import com.reeman.agv.activities.WiFiConnectActivity;
import com.reeman.agv.base.BaseActivity;
import com.reeman.agv.base.BaseFragment;
import com.reeman.commons.event.IPEvent;
import com.reeman.commons.utils.WIFIUtils;
import com.reeman.agv.widgets.ExpandableLayout;

public class NetSettingFragment extends BaseFragment implements ExpandableLayout.OnExpandListener, View.OnClickListener {

    private TextView tvAndroidWlanName;
    private TextView tvAndroidWlanIp;
    private TextView tvNavigationWlanName;
    private TextView tvNavigationWlanIp;
    private ExpandableLayout expandableLayout;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_net_setting;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expandableLayout = $(R.id.el_wlan_status);
        expandableLayout.setOnExpandListener(this);
        tvAndroidWlanName = $(R.id.tv_android_wlan_name);
        tvAndroidWlanIp = $(R.id.tv_android_wlan_ip);
        tvNavigationWlanName = $(R.id.tv_navigation_wlan_name);
        tvNavigationWlanIp = $(R.id.tv_navigation_wlan_ip);
        Button btnSwitchNetwork = $(R.id.btn_switch_network);
        btnSwitchNetwork.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshNetworkState();
        expandableLayout.show();
    }

    private void refreshNetworkState() {
        String connectWifiSSID = WIFIUtils.getConnectWifiSSID(requireContext());
        if ("".equals(connectWifiSSID))connectWifiSSID = getString(R.string.text_not_connected);
        tvAndroidWlanName.setText(connectWifiSSID);
        tvAndroidWlanIp.setText(WIFIUtils.getIpAddress(requireContext()));
    }

    @Override
    public void onIPEvent(@NonNull IPEvent event) {
        super.onIPEvent(event);
        tvNavigationWlanIp.setText(event.getIpAddress());
        tvNavigationWlanName.setText(TextUtils.isEmpty(event.getWifiName()) ? getString(R.string.text_not_connected) : event.getWifiName());
    }

    @Override
    public void onExpand(ExpandableLayout expandableLayout, boolean isExpand) {
        ImageButton ibExpandIndicator = expandableLayout.getHeaderLayout().findViewById(R.id.ib_expand_indicator);
        ibExpandIndicator.animate().rotation(isExpand ? 90 : 0).setDuration(200).start();

        if (isExpand) {
            refreshNetworkState();
            ros.getHostIP();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void onCustomClickResult(int id) {
        if (id == R.id.btn_switch_network) {
            BaseActivity.startup(requireContext(), WiFiConnectActivity.class);
        }
    }
}
