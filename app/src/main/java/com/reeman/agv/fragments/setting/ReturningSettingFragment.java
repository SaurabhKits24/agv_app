package com.reeman.agv.fragments.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.reeman.agv.R;
import com.reeman.agv.base.BaseFragment;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.settings.ReturningSetting;
import com.reeman.commons.utils.SpManager;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

public class ReturningSettingFragment extends BaseFragment implements OnSeekChangeListener {

    private LinearLayout layoutReturningCountDownTime;
    private Gson gson;

    private ReturningSetting returningSetting;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_returning_setting;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gson = new Gson();
        returningSetting = robotInfo.getReturningSetting();
        IndicatorSeekBar isbAdjustGotoProductionPointSpeed = $(R.id.isb_adjust_goto_production_point_speed);
        $(R.id.ib_decrease_goto_production_point_speed).setOnClickListener(this);
        $(R.id.ib_increase_goto_production_point_speed).setOnClickListener(this);
        IndicatorSeekBar isbAdjustGotoChargingPileSpeed = $(R.id.isb_adjust_goto_charging_pile_speed);
        $(R.id.ib_decrease_goto_charging_pile_speed).setOnClickListener(this);
        $(R.id.ib_increase_goto_charging_pile_speed).setOnClickListener(this);
        RadioGroup rgReturningCountDown = $(R.id.rg_returning_count_down);
        layoutReturningCountDownTime = $(R.id.layout_returning_count_down_time);
        IndicatorSeekBar isbReturningCountDownTime = $(R.id.isb_returning_count_down_time);
        $(R.id.ib_decrease_returning_count_down_time).setOnClickListener(this);
        $(R.id.ib_increase_returning_count_down_time).setOnClickListener(this);
        isbAdjustGotoProductionPointSpeed.setProgress(returningSetting.gotoProductionPointSpeed);
        isbAdjustGotoChargingPileSpeed.setProgress(returningSetting.gotoChargingPileSpeed);
        rgReturningCountDown.check(returningSetting.startTaskCountDownSwitch ? R.id.rb_open_returning_count_down : R.id.rb_close_returning_count_down);
        rgReturningCountDown.setOnCheckedChangeListener((group, checkedId) -> {
            returningSetting.startTaskCountDownSwitch = checkedId == R.id.rb_open_returning_count_down;
            layoutReturningCountDownTime.setVisibility(returningSetting.startTaskCountDownSwitch ? View.VISIBLE : View.GONE);
            robotInfo.setReturningSetting(returningSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_RETURNING_CONFIG, gson.toJson(returningSetting)).apply();
        });
        layoutReturningCountDownTime.setVisibility(returningSetting.startTaskCountDownSwitch ? View.VISIBLE : View.GONE);
        isbReturningCountDownTime.setProgress(returningSetting.startTaskCountDownTime);
        isbAdjustGotoProductionPointSpeed.setOnSeekChangeListener(this);
        isbAdjustGotoChargingPileSpeed.setOnSeekChangeListener(this);
    }

    @Override
    protected void onCustomClickResult(int id, View v) {
        super.onCustomClickResult(id, v);
        SharedPreferences.Editor edit = SpManager.getInstance().edit();
        switch (id) {
            case R.id.ib_decrease_goto_production_point_speed:
                returningSetting.gotoProductionPointSpeed = onFloatValueChange(v,false);
                robotInfo.setReturningSetting(returningSetting);
                edit.putString(Constants.KEY_RETURNING_CONFIG,gson.toJson(returningSetting)).apply();
                break;
            case R.id.ib_increase_goto_production_point_speed:
                returningSetting.gotoProductionPointSpeed = onFloatValueChange(v,true);
                robotInfo.setReturningSetting(returningSetting);
                edit.putString(Constants.KEY_RETURNING_CONFIG,gson.toJson(returningSetting)).apply();
                break;
            case R.id.ib_decrease_goto_charging_pile_speed:
                returningSetting.gotoChargingPileSpeed = onFloatValueChange(v,false);
                robotInfo.setReturningSetting(returningSetting);
                edit.putString(Constants.KEY_RETURNING_CONFIG,gson.toJson(returningSetting)).apply();
                break;
            case R.id.ib_increase_goto_charging_pile_speed:
                returningSetting.gotoChargingPileSpeed = onFloatValueChange(v,true);
                robotInfo.setReturningSetting(returningSetting);
                edit.putString(Constants.KEY_RETURNING_CONFIG,gson.toJson(returningSetting)).apply();
                break;
            case R.id.ib_decrease_returning_count_down_time:
                returningSetting.startTaskCountDownTime = onIntValueChange(v,false);
                robotInfo.setReturningSetting(returningSetting);
                edit.putString(Constants.KEY_RETURNING_CONFIG,gson.toJson(returningSetting)).apply();
                break;
            case R.id.ib_increase_returning_count_down_time:
                returningSetting.startTaskCountDownTime = onIntValueChange(v,true);
                robotInfo.setReturningSetting(returningSetting);
                edit.putString(Constants.KEY_RETURNING_CONFIG,gson.toJson(returningSetting)).apply();
                break;
        }
    }

    @Override
    public void onSeeking(SeekParams seekParams) {

    }

    @Override
    public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
        int id = seekBar.getId();
        SharedPreferences.Editor edit = SpManager.getInstance().edit();
        switch (id) {
            case R.id.isb_returning_count_down_time:
                returningSetting.startTaskCountDownTime = onIntValueChange(seekBar,false);
                robotInfo.setReturningSetting(returningSetting);
                edit.putString(Constants.KEY_RETURNING_CONFIG,gson.toJson(returningSetting)).apply();
                break;
            case R.id.isb_adjust_goto_production_point_speed:
                returningSetting.gotoProductionPointSpeed = onFloatValueChange(seekBar,false);
                robotInfo.setReturningSetting(returningSetting);
                edit.putString(Constants.KEY_RETURNING_CONFIG,gson.toJson(returningSetting)).apply();
                break;
            case R.id.isb_adjust_goto_charging_pile_speed:
                returningSetting.gotoChargingPileSpeed = onFloatValueChange(seekBar,false);
                robotInfo.setReturningSetting(returningSetting);
                edit.putString(Constants.KEY_RETURNING_CONFIG,gson.toJson(returningSetting)).apply();
                break;
        }
    }
}
