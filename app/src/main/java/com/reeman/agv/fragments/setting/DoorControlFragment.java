package com.reeman.agv.fragments.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.gson.Gson;
import com.reeman.agv.R;
import com.reeman.agv.activities.DoorControlTestActivity;
import com.reeman.agv.base.BaseFragment;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.utils.SpManager;
import com.reeman.commons.utils.TimeUtil;

import java.util.Calendar;

public class DoorControlFragment extends BaseFragment {

    private TextView tvAirShowerDoorCountDownTime;
    private LinearLayout layoutDoorControlSettings;

    private final Gson gson = new Gson();

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_door_control_setting;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initDoorControlView();

    }

    private void initDoorControlView() {
        RadioGroup rgDoorControl = $(R.id.rg_door_control);
        RadioGroup rgCloseDoorControl = $(R.id.rg_close_door_control);
        tvAirShowerDoorCountDownTime = $(R.id.tv_air_shower_door_count_down_time);
        $(R.id.btn_start_door_control_test).setOnClickListener(this);
        $(R.id.btn_air_shower_door_count_down_time_setting).setOnClickListener(this);
        layoutDoorControlSettings = $(R.id.layout_door_control_settings);
        rgDoorControl.check(robotInfo.getDoorControlSetting().open ? R.id.rb_open_door_control : R.id.rb_close_door_control);
        int checkId;
        if (robotInfo.getDoorControlSetting().closeDoorAction == 0) {
            checkId = R.id.rb_close_door_and_navigation;
        } else {
            checkId = R.id.rb_close_door_first;
        }
        rgCloseDoorControl.check(checkId);
        updateViewVisibility();
        rgDoorControl.setOnCheckedChangeListener((radioGroup, i) -> {
            robotInfo.getDoorControlSetting().open = i == R.id.rb_open_door_control;
            updateViewVisibility();
            SpManager.getInstance().edit().putString(Constants.KEY_DOOR_CONTROL, gson.toJson(robotInfo.getDoorControlSetting())).apply();
        });
        rgCloseDoorControl.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.rb_close_door_and_navigation) {
                robotInfo.getDoorControlSetting().closeDoorAction = 0;
            } else {
                robotInfo.getDoorControlSetting().closeDoorAction = 1;
            }
            updateViewVisibility();
            SpManager.getInstance().edit().putString(Constants.KEY_DOOR_CONTROL, gson.toJson(robotInfo.getDoorControlSetting())).apply();
        });
        tvAirShowerDoorCountDownTime.setText(TimeUtil.formatTimeHourMinSec(robotInfo.getDoorControlSetting().waitingTime * 1000L));
    }

    private void updateViewVisibility() {
        layoutDoorControlSettings.setVisibility(robotInfo.getDoorControlSetting().open ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
    }

    @Override
    protected void onCustomClickResult(int id) {
        switch (id) {
            case R.id.btn_start_door_control_test:
                requireActivity().startActivity(new Intent(requireActivity(), DoorControlTestActivity.class));
                break;
            case R.id.btn_air_shower_door_count_down_time_setting:
                showTimePicker(robotInfo.getDoorControlSetting().waitingTime);
                break;
        }

    }

    private void showTimePicker(long seconds) {
        Calendar date = Calendar.getInstance();
        int hours = (int) (seconds / 3600);
        date.set(Calendar.HOUR_OF_DAY, hours);
        int minutes = (int) ((seconds - hours * 3600) / 60);
        date.set(Calendar.MINUTE, minutes);
        date.set(Calendar.SECOND, (int) (seconds - hours * 3600 - minutes * 60));
        TimePickerView timePicker = new TimePickerBuilder(requireContext(),
                (date1, v) -> {
                    int waitingTime = date1.getSeconds() + date1.getMinutes() * 60 + date1.getHours() * 60 * 60;
                    if (waitingTime < 3) {
                        ToastUtils.showShortToast(getString(R.string.text_air_shower_door_count_down_time_must_longer_than_three));
                        return;
                    }
                    String cycleTime = TimeUtil.formatHourAndMinuteAndSecond(date1);
                    tvAirShowerDoorCountDownTime.setText(cycleTime);
                    robotInfo.getDoorControlSetting().waitingTime = waitingTime;
                    SpManager.getInstance().edit().putString(Constants.KEY_DOOR_CONTROL, gson.toJson(robotInfo.getDoorControlSetting())).apply();
                })
                .setSubmitText(getString(R.string.text_confirm))
                .setCancelText(getString(R.string.text_cancel))
                .setType(new boolean[]{false, false, false, true, true, true})
                .setLabel("", "", "", "h", "m", "s")
                .setTitleSize(20)
                .setDate(date)
                .isCyclic(true)
                .isDialog(true)
                .setSubCalSize(24)
                .setContentTextSize(24)
                .setItemVisibleCount(9) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
                .setLineSpacingMultiplier(2.0f)
                .isAlphaGradient(true)
                .setOutSideCancelable(false)
                .build();
        timePicker.show();
    }
}
