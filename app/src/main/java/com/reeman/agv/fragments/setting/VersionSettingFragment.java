package com.reeman.agv.fragments.setting;


import static com.reeman.agv.base.BaseApplication.ros;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reeman.agv.R;
import com.reeman.agv.base.BaseFragment;
import com.reeman.agv.utils.PackageUtils;
import com.reeman.commons.event.VersionInfoEvent;


public class VersionSettingFragment extends BaseFragment implements View.OnClickListener {

    private TextView tvNavigationVersion;

    private TextView tvPowerBoardVersion;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_version_setting;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvAppVersion = $(R.id.tv_app_version);
        tvAppVersion.setText(PackageUtils.getVersion(requireContext()));
        tvNavigationVersion = $(R.id.tv_navigation_version);
        tvNavigationVersion.setOnClickListener(this);
        tvPowerBoardVersion = $(R.id.tv_power_board_version);
        tvPowerBoardVersion.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ros.heartBeat();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void onCustomClickResult(int id) {
        switch (id){
            case R.id.tv_navigation_version:
                tvNavigationVersion.setText("");
                ros.heartBeat();
                break;
            case R.id.tv_power_board_version:
                tvPowerBoardVersion.setText("");
                ros.heartBeat();
                break;
        }
    }

    @Override
    public void onVersionEvent(VersionInfoEvent event) {
        tvNavigationVersion.setText(event.getSoftVer());
        tvPowerBoardVersion.setText(event.getHardwareVer());
    }

}
