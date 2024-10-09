package com.reeman.agv.fragments.setting;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kotlin.Pair;


import com.google.gson.Gson;
import com.kyleduo.switchbutton.SwitchButton;
import com.reeman.agv.R;
import com.reeman.agv.activities.CallingConfigActivity;
import com.reeman.agv.base.BaseActivity;
import com.reeman.agv.base.BaseFragment;
import com.reeman.agv.calling.CallingInfo;
import com.reeman.agv.calling.button.CallingHelper;
import com.reeman.agv.calling.model.MulticastSendInfo;
import com.reeman.commons.constants.Constants;
import com.reeman.agv.calling.setting.ModeCallingSetting;
import com.reeman.commons.event.AGVTagPoseEvent;
import com.reeman.commons.settings.ModeQRCodeSetting;
import com.reeman.commons.settings.ModeNormalSetting;
import com.reeman.commons.settings.ModeRouteSetting;
import com.reeman.commons.utils.AESUtil;
import com.reeman.commons.utils.AndroidInfoUtil;
import com.reeman.commons.utils.QrCodeUtil;
import com.reeman.commons.utils.WIFIUtils;
import com.reeman.agv.utils.MulticastMessageSender;
import com.reeman.agv.utils.PackageUtils;
import com.reeman.agv.widgets.EasyDialog;
import com.reeman.agv.widgets.QRCodePairingDialog;
import com.reeman.agv.widgets.TimePickerDialog;
import com.reeman.commons.utils.SpManager;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.widgets.ExpandableLayout;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import timber.log.Timber;


public class ModeSettingFragment extends BaseFragment implements ExpandableLayout.OnExpandListener, View.OnClickListener, OnSeekChangeListener {

    private ExpandableLayout elNormalMode;
    private ExpandableLayout elRouteMode;
    private ExpandableLayout elQRCodeMode;
    private ExpandableLayout elCallingMode;

    //普通模式
    private IndicatorSeekBar isbAdjustNormalModeSpeed;

    private TextView tvNormalModePickingHours, tvNormalModePickingMinutes, tvNormalModePickingSeconds;

    private Button btnNormalModePickingTimeSetting;

    private TextView tvNormalModePlayArrivedTipHours, tvNormalModePlayArrivedTipMinutes, tvNormalModePlayArrivedSeconds;

    private Button btnNormalModePlayArrivedTipTimeSetting;

    private RadioGroup rgNormalModeFinishActionControl, rgNormalModeWaitingTimeControl, rgNormalModeManualLiftControl;

    private LinearLayout layoutManualLiftSetting;

    private EditText etRobotLengthNormalMode;
    private EditText etRobotWidthNormalMode;

    private EditText etLidarWidthNormalMode;
    private RadioGroup rgRotateNormalMode;
    private RadioGroup rgStopNearbyNormalMode;
    private LinearLayout layoutStartNormalTaskCountDownTime;
    private RadioGroup rgStartNormalTaskCountDown;
    private IndicatorSeekBar isbStartNormalTaskCountDownTime;

    //路线模式

    private IndicatorSeekBar isbAdjustRouteModeSpeed;
    private LinearLayout layoutStartRouteTaskCountDownTime;
    private RadioGroup rgStartRouteTaskCountDown;
    private IndicatorSeekBar isbStartRouteTaskCountDownTime;

    //二维码模式
    private IndicatorSeekBar isbAdjustQRCodeModeSpeed;
    private RadioGroup rgQRCodeModeLiftControl;
    private EditText etRobotLength;
    private EditText etRobotWidth;
    private EditText etOrientationAndDistanceCalibration;
    private EditText etLidarWidth;
    private RadioGroup rgDirection;
    private RadioGroup rgRotate;
    private RadioGroup rgStopNearby;
    private RadioGroup rgCallingBind;
    private LinearLayout layoutStartQRCodeTaskCountDownTime;
    private RadioGroup rgStartQRCodeTaskCountDown;
    private IndicatorSeekBar isbStartQRCodeTaskCountDownTime;
    private TextView tvQRCodeData;


    //呼叫模式

    private IndicatorSeekBar isbAdjustCallingModeSpeed;
    private SwitchButton swEnableCallingQueue;
    private IndicatorSeekBar isbAdjustCallingModeWaitingTime;
    private IndicatorSeekBar isbAdjustCallingModeCacheTime;
    private EditText etCallingKey;
    private LinearLayout layoutStartCallingTaskCountDownTime;
    private RadioGroup rgStartCallingTaskCountDown;
    private IndicatorSeekBar isbStartCallingTaskCountDownTime;


    private ModeNormalSetting normalSetting;
    private ModeRouteSetting routeSetting;
    private ModeQRCodeSetting qrCodeSetting;
    private ModeCallingSetting callingSetting;
    private List<ExpandableLayout> list;

    private Gson gson;

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private CallingInfo callingInfo;

    private MulticastMessageSender sender;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callingInfo = CallingInfo.INSTANCE;
        gson = new Gson();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_mode_setting;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        elNormalMode = $(R.id.el_normal_mode);
        elRouteMode = $(R.id.el_route_mode);
        elQRCodeMode = $(R.id.el_qrcode_mode);
        elCallingMode = $(R.id.el_calling_mode);


        elNormalMode.setOnExpandListener(this);
        elRouteMode.setOnExpandListener(this);
        elQRCodeMode.setOnExpandListener(this);
        elCallingMode.setOnExpandListener(this);

        list = Arrays.asList(elNormalMode,
                elRouteMode,
                elQRCodeMode,
                elCallingMode
        );
        if (!robotInfo.isSpaceShip() || robotInfo.getRobotType() == 6) {
            elQRCodeMode.setVisibility(View.GONE);
        }
    }

    @Override
    public void onExpand(ExpandableLayout expandableLayout, boolean isExpand) {
        ImageButton ibExpandIndicator = expandableLayout.getHeaderLayout().findViewById(R.id.ib_expand_indicator);
        ibExpandIndicator.animate().rotation(isExpand ? 90 : 0).setDuration(200).start();

        for (ExpandableLayout layout : list) {
            if (layout != expandableLayout && layout.isOpened()) {
                layout.hide();
            }
        }

        initExpandLayout(expandableLayout);
    }

    private void initExpandLayout(ExpandableLayout expandableLayout) {
        if (expandableLayout == elNormalMode) {
            initNormalModeView(expandableLayout);
        } else if (expandableLayout == elRouteMode) {
            initRouteModeView(expandableLayout);
        } else if (expandableLayout == elQRCodeMode) {
            initQRCodeModeView(expandableLayout);
        } else if (expandableLayout == elCallingMode) {
            initCallingModeView(expandableLayout);
        }
    }

    private void initCallingModeView(ExpandableLayout root) {
        callingSetting = callingInfo.getCallingModeSetting();
        if (root.getTag() == null) {
            root.findViewById(R.id.ib_increase_calling_mode_speed).setOnClickListener(this);
            root.findViewById(R.id.ib_decrease_calling_mode_speed).setOnClickListener(this);
            root.findViewById(R.id.ib_increase_calling_waiting_time).setOnClickListener(this);
            root.findViewById(R.id.ib_decrease_calling_waiting_time).setOnClickListener(this);
            root.findViewById(R.id.ib_increase_calling_cache_time).setOnClickListener(this);
            root.findViewById(R.id.ib_decrease_calling_cache_time).setOnClickListener(this);
            isbAdjustCallingModeSpeed = root.findViewById(R.id.isb_adjust_calling_mode_speed);
            isbAdjustCallingModeSpeed.setOnSeekChangeListener(this);
            isbAdjustCallingModeWaitingTime = root.findViewById(R.id.isb_calling_waiting_time);
            isbAdjustCallingModeWaitingTime.setOnSeekChangeListener(this);
            isbAdjustCallingModeCacheTime = root.findViewById(R.id.isb_calling_cache_time);
            isbAdjustCallingModeCacheTime.setOnSeekChangeListener(this);
            swEnableCallingQueue = root.findViewById(R.id.sw_enable_calling_queue);
            etCallingKey = root.findViewById(R.id.et_calling_key);
            etCallingKey.setFilters(new InputFilter[]{new AlphaNumericInputFilter(8)});
            root.findViewById(R.id.btn_calling_config).setOnClickListener(this);
            root.findViewById(R.id.btn_save_calling_key).setOnClickListener(this);
            root.findViewById(R.id.btn_calling_pairing_config_by_multicast).setOnClickListener(this);
            root.findViewById(R.id.btn_calling_pairing_config_by_qrcode).setOnClickListener(this);
            rgStartCallingTaskCountDown = root.findViewById(R.id.rg_start_calling_task_count_down);
            layoutStartCallingTaskCountDownTime = root.findViewById(R.id.layout_start_calling_task_count_down_time);
            root.findViewById(R.id.ib_decrease_start_calling_task_count_down_time).setOnClickListener(this);
            root.findViewById(R.id.ib_increase_start_calling_task_count_down_time).setOnClickListener(this);
            isbStartCallingTaskCountDownTime = root.findViewById(R.id.isb_start_calling_task_count_down_time);
            isbStartCallingTaskCountDownTime.setOnSeekChangeListener(this);
            root.setTag("initialized");
        }
        Pair<String, List<String>> key = callingSetting.key;
        etCallingKey.setText(key.getFirst());
        isbAdjustCallingModeSpeed.setProgress(callingSetting.speed);
        isbAdjustCallingModeWaitingTime.setProgress(callingSetting.waitingTime);
        isbAdjustCallingModeCacheTime.setProgress(callingSetting.cacheTime);
        swEnableCallingQueue.setChecked(callingSetting.openCallingQueue);
        swEnableCallingQueue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            callingSetting.openCallingQueue = isChecked;
            callingInfo.setCallingModeSetting(callingSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
        });
        rgStartCallingTaskCountDown.check(callingSetting.startTaskCountDownSwitch ? R.id.rb_open_start_calling_task_count_down : R.id.rb_close_start_calling_task_count_down);
        rgStartCallingTaskCountDown.setOnCheckedChangeListener((group, checkedId) -> {
            callingSetting.startTaskCountDownSwitch = checkedId == R.id.rb_open_start_calling_task_count_down;
            layoutStartCallingTaskCountDownTime.setVisibility(callingSetting.startTaskCountDownSwitch ? View.VISIBLE : View.GONE);
            callingInfo.setCallingModeSetting(callingSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
        });
        if (!callingSetting.startTaskCountDownSwitch) {
            layoutStartCallingTaskCountDownTime.setVisibility(View.GONE);
        }
        isbStartCallingTaskCountDownTime.setProgress(callingSetting.startTaskCountDownTime);
    }

    private void initQRCodeModeView(ExpandableLayout root) {
        qrCodeSetting = robotInfo.getModeQRCodeSetting();
        if (root.getTag() == null) {
            root.findViewById(R.id.ib_increase_qrcode_mode_speed).setOnClickListener(this);
            root.findViewById(R.id.ib_decrease_qrcode_mode_speed).setOnClickListener(this);
            isbAdjustQRCodeModeSpeed = root.findViewById(R.id.isb_adjust_qrcode_mode_speed);
            isbAdjustQRCodeModeSpeed.setOnSeekChangeListener(this);
            etRobotLength = root.findViewById(R.id.et_length);
            etRobotWidth = root.findViewById(R.id.et_width);
            etOrientationAndDistanceCalibration = root.findViewById(R.id.et_orientation_and_distance_calibration);
            etLidarWidth = root.findViewById(R.id.et_lidar_width);
            rgDirection = root.findViewById(R.id.rg_direction);
            rgDirection.setOnCheckedChangeListener((radioGroup, i) -> {
                qrCodeSetting.direction = (i == R.id.rb_forward);
                SpManager.getInstance().edit().putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
            });
            rgRotate = root.findViewById(R.id.rg_rotate_control);
            rgRotate.setOnCheckedChangeListener((radioGroup, i) -> {
                qrCodeSetting.rotate = (i == R.id.rb_open);
                SpManager.getInstance().edit().putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
            });
            rgStopNearby = root.findViewById(R.id.rg_stop_nearby);
            rgStopNearby.setOnCheckedChangeListener((radioGroup, i) -> {
                qrCodeSetting.stopNearby = (i == R.id.rb_open_stop_nearby);
                SpManager.getInstance().edit().putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
            });
            rgCallingBind = root.findViewById(R.id.rg_calling_bind);
            rgCallingBind.setOnCheckedChangeListener((radioGroup, i) -> {
                qrCodeSetting.callingBind = (i == R.id.rb_open_calling_bind);
                CallingInfo.INSTANCE.setQRCodeTaskUseCallingButton(qrCodeSetting.callingBind);
                SpManager.getInstance().edit().putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
            });
            rgQRCodeModeLiftControl = root.findViewById(R.id.rg_qrcode_lift_control);
            rgQRCodeModeLiftControl.setOnCheckedChangeListener((group, checkedId) -> {
                qrCodeSetting.lift = checkedId == R.id.rb_open_lift_control;
                SpManager.getInstance().edit().putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
            });
            root.findViewById(R.id.btn_save_length_and_width).setOnClickListener(this);
            root.findViewById(R.id.btn_save_orientation_and_distance_calibration).setOnClickListener(this);
            root.findViewById(R.id.btn_save_lidar_width).setOnClickListener(this);
            if (!robotInfo.isLiftModelInstalled()) {
                root.findViewById(R.id.ll_qrcode_lift_control).setVisibility(View.GONE);
            }
            rgStartQRCodeTaskCountDown = root.findViewById(R.id.rg_start_qrcode_task_count_down);
            layoutStartQRCodeTaskCountDownTime = root.findViewById(R.id.layout_start_qrcode_task_count_down_time);
            root.findViewById(R.id.ib_decrease_start_qrcode_task_count_down_time).setOnClickListener(this);
            root.findViewById(R.id.ib_increase_start_qrcode_task_count_down_time).setOnClickListener(this);
            isbStartQRCodeTaskCountDownTime = root.findViewById(R.id.isb_start_qrcode_task_count_down_time);
            isbStartQRCodeTaskCountDownTime.setOnSeekChangeListener(this);
            tvQRCodeData = root.findViewById(R.id.tv_qrcode_data);
            root.setTag("initialized");
        }
        isbAdjustQRCodeModeSpeed.setProgress(qrCodeSetting.speed);
        etRobotLength.setText(String.format("%s", qrCodeSetting.length));
        etRobotWidth.setText(String.format("%s", qrCodeSetting.width));
        etOrientationAndDistanceCalibration.setText(String.format("%s", qrCodeSetting.orientationAndDistanceCalibration));
        etLidarWidth.setText(String.format("%s", qrCodeSetting.lidarWidth));
        rgDirection.check(qrCodeSetting.direction ? R.id.rb_forward : R.id.rb_behind);
        rgRotate.check(qrCodeSetting.rotate ? R.id.rb_open : R.id.rb_close);
        rgStopNearby.check(qrCodeSetting.stopNearby ? R.id.rb_open_stop_nearby : R.id.rb_close_stop_nearby);
        rgCallingBind.check(qrCodeSetting.callingBind ? R.id.rb_open_calling_bind : R.id.rb_close_calling_bind);
        rgQRCodeModeLiftControl.check(qrCodeSetting.lift ? R.id.rb_open_lift_control : R.id.rb_close_lift_control);
        rgStartQRCodeTaskCountDown.check(qrCodeSetting.startTaskCountDownSwitch ? R.id.rb_open_start_qrcode_task_count_down : R.id.rb_close_start_qrcode_task_count_down);
        rgStartQRCodeTaskCountDown.setOnCheckedChangeListener((group, checkedId) -> {
            qrCodeSetting.startTaskCountDownSwitch = checkedId == R.id.rb_open_start_qrcode_task_count_down;
            layoutStartQRCodeTaskCountDownTime.setVisibility(qrCodeSetting.startTaskCountDownSwitch ? View.VISIBLE : View.GONE);
            robotInfo.setModeQRCodeSetting(qrCodeSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
        });
        if (!qrCodeSetting.startTaskCountDownSwitch) {
            layoutStartQRCodeTaskCountDownTime.setVisibility(View.GONE);
        }
        isbStartQRCodeTaskCountDownTime.setProgress(qrCodeSetting.startTaskCountDownTime);
    }

    private void initRouteModeView(ExpandableLayout root) {
        routeSetting = robotInfo.getModeRouteSetting();
        if (root.getTag() == null) {
            root.findViewById(R.id.ib_increase_route_mode_speed).setOnClickListener(this);
            root.findViewById(R.id.ib_decrease_route_mode_speed).setOnClickListener(this);
            isbAdjustRouteModeSpeed = root.findViewById(R.id.isb_adjust_route_mode_speed);
            isbAdjustRouteModeSpeed.setOnSeekChangeListener(this);
            rgStartRouteTaskCountDown = root.findViewById(R.id.rg_start_route_task_count_down);
            layoutStartRouteTaskCountDownTime = root.findViewById(R.id.layout_start_route_task_count_down_time);
            root.findViewById(R.id.ib_decrease_start_route_task_count_down_time).setOnClickListener(this);
            root.findViewById(R.id.ib_increase_start_route_task_count_down_time).setOnClickListener(this);
            isbStartRouteTaskCountDownTime = root.findViewById(R.id.isb_start_route_task_count_down_time);
            isbStartRouteTaskCountDownTime.setOnSeekChangeListener(this);
            root.setTag("initialized");
        }
        isbAdjustRouteModeSpeed.setProgress(routeSetting.speed);
        rgStartRouteTaskCountDown.check(routeSetting.startTaskCountDownSwitch ? R.id.rb_open_start_route_task_count_down : R.id.rb_close_start_route_task_count_down);
        rgStartRouteTaskCountDown.setOnCheckedChangeListener((group, checkedId) -> {
            routeSetting.startTaskCountDownSwitch = checkedId == R.id.rb_open_start_route_task_count_down;
            layoutStartRouteTaskCountDownTime.setVisibility(routeSetting.startTaskCountDownSwitch ? View.VISIBLE : View.GONE);
            robotInfo.setModeRouteSetting(routeSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_ROUTE_MODE_CONFIG, gson.toJson(routeSetting)).apply();
        });
        if (!routeSetting.startTaskCountDownSwitch) {
            layoutStartRouteTaskCountDownTime.setVisibility(View.GONE);
        }
        isbStartRouteTaskCountDownTime.setProgress(routeSetting.startTaskCountDownTime);
    }

    private void initNormalModeView(ExpandableLayout root) {
        normalSetting = robotInfo.getModeNormalSetting();
        if (root.getTag() == null) {
            root.findViewById(R.id.ib_increase_normal_mode_speed).setOnClickListener(this);
            root.findViewById(R.id.ib_decrease_normal_mode_speed).setOnClickListener(this);
            isbAdjustNormalModeSpeed = root.findViewById(R.id.isb_adjust_normal_mode_speed);
            isbAdjustNormalModeSpeed.setOnSeekChangeListener(this);

            tvNormalModePickingHours = root.findViewById(R.id.tv_picking_hours);
            tvNormalModePickingMinutes = root.findViewById(R.id.tv_picking_minutes);
            tvNormalModePickingSeconds = root.findViewById(R.id.tv_picking_seconds);

            tvNormalModePlayArrivedTipHours = root.findViewById(R.id.tv_play_arrived_tip_hours);
            tvNormalModePlayArrivedTipMinutes = root.findViewById(R.id.tv_play_arrived_tip_minutes);
            tvNormalModePlayArrivedSeconds = root.findViewById(R.id.tv_play_arrived_tip_seconds);
            btnNormalModePlayArrivedTipTimeSetting = root.findViewById(R.id.btn_play_arrived_tip_time_setting);
            btnNormalModePlayArrivedTipTimeSetting.setOnClickListener(this);

            rgNormalModeWaitingTimeControl = root.findViewById(R.id.rg_waiting_time_control);
            rgNormalModeFinishActionControl = root.findViewById(R.id.rg_finish_action_control);
            rgNormalModeManualLiftControl = root.findViewById(R.id.rg_manual_lift_control);
            btnNormalModePickingTimeSetting = root.findViewById(R.id.btn_picking_time_setting);
            btnNormalModePickingTimeSetting.setOnClickListener(this);
            layoutManualLiftSetting = root.findViewById(R.id.layout_manual_lift_setting);
            etRobotLengthNormalMode = root.findViewById(R.id.et_length_normal_mode);
            etRobotWidthNormalMode = root.findViewById(R.id.et_width_normal_mode);
            etLidarWidthNormalMode = root.findViewById(R.id.et_lidar_width_normal_mode);
            rgRotateNormalMode = root.findViewById(R.id.rg_rotate_control_normal_mode);
            rgStopNearbyNormalMode = root.findViewById(R.id.rg_stop_nearby_normal_mode);
            root.findViewById(R.id.btn_save_length_and_width_normal_mode).setOnClickListener(this);
            root.findViewById(R.id.btn_save_lidar_width_normal_mode).setOnClickListener(this);
            if (!robotInfo.isSpaceShip() || !robotInfo.isLiftModelInstalled()) {
                root.findViewById(R.id.layout_manual_lift_control).setVisibility(View.GONE);
            }
            rgStartNormalTaskCountDown = root.findViewById(R.id.rg_start_normal_task_count_down);
            layoutStartNormalTaskCountDownTime = root.findViewById(R.id.layout_start_normal_task_count_down_time);
            root.findViewById(R.id.ib_decrease_start_normal_task_count_down_time).setOnClickListener(this);
            root.findViewById(R.id.ib_increase_start_normal_task_count_down_time).setOnClickListener(this);
            isbStartNormalTaskCountDownTime = root.findViewById(R.id.isb_start_normal_task_count_down_time);
            isbStartNormalTaskCountDownTime.setOnSeekChangeListener(this);

            root.setTag("initialized");
        }

        isbAdjustNormalModeSpeed.setProgress(normalSetting.speed);
        rgNormalModeFinishActionControl.check(normalSetting.finishAction == 0 ? R.id.rb_return_product_point : R.id.rb_stay);
        rgNormalModeWaitingTimeControl.check(normalSetting.waitingCountDownTimerOpen ? R.id.rb_open_count_down_timer : R.id.rb_close_count_down_timer);
        rgNormalModeManualLiftControl.check(robotInfo.isNormalModeWithManualLiftControl() ? R.id.rb_open_manual_lift_control : R.id.rb_close_manual_lift_control);
        layoutManualLiftSetting.setVisibility(robotInfo.isNormalModeWithManualLiftControl() ? View.VISIBLE : View.GONE);
        etRobotLengthNormalMode.setText(String.format("%s", normalSetting.length));
        etRobotWidthNormalMode.setText(String.format("%s", normalSetting.width));
        etLidarWidthNormalMode.setText(String.format("%s", normalSetting.lidarWidth));
        rgRotateNormalMode.check(normalSetting.rotate ? R.id.rb_open_normal_mode : R.id.rb_close_normal_mode);
        rgRotateNormalMode.setOnCheckedChangeListener((group, checkedId) -> {
            normalSetting.rotate = checkedId == R.id.rb_open_normal_mode;
            robotInfo.setModeNormalSetting(normalSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
        });
        rgStopNearbyNormalMode.check(normalSetting.stopNearBy ? R.id.rb_open_stop_nearby_normal_mode : R.id.rb_close_stop_nearby_normal_mode);
        rgStopNearbyNormalMode.setOnCheckedChangeListener((group, checkedId) -> {
            normalSetting.stopNearBy = checkedId == R.id.rb_open_stop_nearby_normal_mode;
            robotInfo.setModeNormalSetting(normalSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
        });
        btnNormalModePickingTimeSetting.setBackgroundResource(normalSetting.waitingCountDownTimerOpen ? R.drawable.bg_common_button_active : R.drawable.bg_common_button_inactive);
        rgNormalModeWaitingTimeControl.setOnCheckedChangeListener((radioGroup, i) -> {
            boolean countDownTimerOpened = i == R.id.rb_open_count_down_timer;
            if (countDownTimerOpened && robotInfo.isNormalModeWithManualLiftControl()) {
                EasyDialog.getInstance(requireActivity()).confirm(getString(R.string.text_count_down_and_manual_lift_cannot_both_open_if_open_count_down), (dialog, id) -> {
                    if (id == R.id.btn_confirm) {
                        normalSetting.waitingCountDownTimerOpen = true;
                        normalSetting.manualLiftControlOpen = false;
                        rgNormalModeManualLiftControl.check(R.id.rb_close_manual_lift_control);
                        btnNormalModePickingTimeSetting.setBackgroundResource(R.drawable.bg_common_button_active);
                        layoutManualLiftSetting.setVisibility(View.GONE);
                        robotInfo.setModeNormalSetting(normalSetting);
                        SpManager.getInstance().edit().putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                    } else {
                        rgNormalModeWaitingTimeControl.check(R.id.rb_close_count_down_timer);
                    }
                    dialog.dismiss();
                });
                return;
            }
            normalSetting.waitingCountDownTimerOpen = countDownTimerOpened;
            btnNormalModePickingTimeSetting.setBackgroundResource(normalSetting.waitingCountDownTimerOpen ? R.drawable.bg_common_button_active : R.drawable.bg_common_button_inactive);
            robotInfo.setModeNormalSetting(normalSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
        });
        rgNormalModeFinishActionControl.setOnCheckedChangeListener((radioGroup, i) -> {
            normalSetting.finishAction = (i == R.id.rb_return_product_point ? 0 : 1);
            robotInfo.setModeNormalSetting(normalSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
        });
        rgNormalModeManualLiftControl.setOnCheckedChangeListener((group, checkedId) -> {
            boolean manualLiftControlOpened = checkedId == R.id.rb_open_manual_lift_control;
            if (manualLiftControlOpened && normalSetting.waitingCountDownTimerOpen) {
                EasyDialog.getInstance(requireActivity()).confirm(getString(R.string.text_manual_lift_and_count_down_cannot_both_open_if_open_manual_lift), (dialog, id) -> {
                    if (id == R.id.btn_confirm) {
                        normalSetting.waitingCountDownTimerOpen = false;
                        normalSetting.manualLiftControlOpen = true;
                        rgNormalModeWaitingTimeControl.check(R.id.rb_close_count_down_timer);
                        btnNormalModePickingTimeSetting.setBackgroundResource(R.drawable.bg_common_button_inactive);
                        layoutManualLiftSetting.setVisibility(View.VISIBLE);
                        robotInfo.setModeNormalSetting(normalSetting);
                        SpManager.getInstance().edit().putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                    } else {
                        rgNormalModeManualLiftControl.check(R.id.rb_close_manual_lift_control);
                    }
                    dialog.dismiss();
                });
                return;
            }
            normalSetting.manualLiftControlOpen = manualLiftControlOpened;
            layoutManualLiftSetting.setVisibility(manualLiftControlOpened ? View.VISIBLE : View.GONE);
            robotInfo.setModeNormalSetting(normalSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
        });
        updateWaitingTime(normalSetting.waitingTime);
        updatePlayArrivedTipTime(normalSetting.playArrivedTipTime);
        rgStartNormalTaskCountDown.check(normalSetting.startTaskCountDownSwitch ? R.id.rb_open_start_normal_task_count_down : R.id.rb_close_start_normal_task_count_down);
        rgStartNormalTaskCountDown.setOnCheckedChangeListener((group, checkedId) -> {
            normalSetting.startTaskCountDownSwitch = checkedId == R.id.rb_open_start_normal_task_count_down;
            layoutStartNormalTaskCountDownTime.setVisibility(normalSetting.startTaskCountDownSwitch ? View.VISIBLE : View.GONE);
            robotInfo.setModeNormalSetting(normalSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
        });
        if (!normalSetting.startTaskCountDownSwitch) {
            layoutStartNormalTaskCountDownTime.setVisibility(View.GONE);
        }
        isbStartNormalTaskCountDownTime.setProgress(normalSetting.startTaskCountDownTime);

    }

    @Override
    public void onAGVTagPoseEvent(AGVTagPoseEvent event) {
        if (tvQRCodeData != null) {
            tvQRCodeData.setText(event.getData());
        }
    }

    /**
     * 进入配对模式
     */
    private void enterPairingMode() {
        try {
            sender = new MulticastMessageSender();
        } catch (Exception e) {
            sender = null;
            Timber.w(e, "加入组播组失败");
            EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.text_enter_multicast_group_failed));
            return;
        }
        String pairingInfo = createPairingInfo();
        if (TextUtils.isEmpty(pairingInfo)) return;
        sender.startSendingMulticast(pairingInfo);
        EasyDialog.getCancelableLoadingInstance(requireActivity())
                .loadingCancelable(
                        getString(R.string.text_robot_already_enter_pairing_mode_please_check_your_phone, robotInfo.getROSHostname(), robotInfo.getRobotAlias()),
                        getString(R.string.text_exit_calling_pairing),
                        (dialog, id) -> dialog.dismiss(), dialog -> {
                            if (sender != null) {
                                ToastUtils.showShortToast(getString(R.string.text_already_exit_calling_pairing));
                                sender.stopSendingMulticast();
                            }
                        });

    }

    private String createPairingInfo() {
        try {
            String encrypt = AESUtil.encrypt("a123456", PackageUtils.getVersion(requireActivity()) + AndroidInfoUtil.getSerialNumber() + System.currentTimeMillis());
            String currentToken = encrypt.substring(0, 8);
            callingSetting.key.getSecond().add(currentToken);
            callingInfo.setCallingModeSetting(callingSetting);
            SpManager.getInstance().edit().putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
            String s = new Gson().toJson(new MulticastSendInfo(robotInfo.getROSHostname(), robotInfo.getRobotAlias(), callingSetting.key.getFirst(), currentToken, robotInfo.getRobotType()));
            Timber.w("生成配对信息: %s", s);
            return s;
        } catch (GeneralSecurityException e) {
            Timber.w(e, "加密失败");
            ToastUtils.showShortToast(getString(R.string.text_create_pairing_info_failed));
            return null;
        }
    }

    private static class AlphaNumericInputFilter implements InputFilter {

        private final int length;

        public AlphaNumericInputFilter(int length) {
            this.length = length;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern acceptedChars = Pattern.compile("[a-zA-Z0-9]+");

            if (source == null || source.length() == 0) {
                return null;
            }

            String input = source.toString();
            if (input.matches(acceptedChars.pattern()) && dest.length() + input.length() <= length) {
                return null;
            } else {
                return "";
            }
        }
    }


    @Override
    protected void onCustomClickResult(int id, View v) {
        SharedPreferences.Editor edit = SpManager.getInstance().edit();
        switch (id) {
            case R.id.btn_calling_pairing_config_by_multicast:
                EasyDialog.getInstance(requireActivity())
                        .confirm(getString(R.string.text_please_confirm_wifi_before_pairing, WIFIUtils.getConnectWifiSSID(requireActivity())), new EasyDialog.OnViewClickListener() {
                            @Override
                            public void onViewClick(Dialog dialog, int id) {
                                dialog.dismiss();
                                if (id == R.id.btn_confirm) {
                                    enterPairingMode();
                                }
                            }
                        });
                break;
            case R.id.btn_calling_pairing_config_by_qrcode:
//                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_remote_control);
                String pairingInfo = createPairingInfo();
                if (TextUtils.isEmpty(pairingInfo)) return;
                Bitmap qrCodeBitmap = QrCodeUtil.INSTANCE.createQRCodeBitmap(
                        pairingInfo,
                        300,
                        300,
                        "UTF-8",
                        "H"
                );
                if (qrCodeBitmap == null) {
                    ToastUtils.showShortToast(getString(R.string.text_create_qrcode_failed));
                    return;
                }
                new QRCodePairingDialog(requireActivity(), qrCodeBitmap).show();
                break;
            case R.id.btn_save_calling_key:
                String keyInput = etCallingKey.getText().toString();
                if (TextUtils.isEmpty(keyInput) || keyInput.length() != 8) {
                    etCallingKey.setError(getString(R.string.text_please_input_num_and_letters_and_length_8));
                    return;
                }
                Pair<String, List<String>> key = callingSetting.key;
                if (!key.getFirst().equals(keyInput)) {
                    try {
                        String encrypt = AESUtil.encrypt("a123456", PackageUtils.getVersion(requireActivity()) + AndroidInfoUtil.getSerialNumber() + System.currentTimeMillis());
                        String token = encrypt.substring(8);
                        List<String> tokens = new ArrayList<>();
                        tokens.add(token);
                        Timber.w("key : %s ,token : %s", keyInput, token);
                        callingSetting.key = new Pair<>(keyInput, tokens);
                        callingInfo.setCallingModeSetting(callingSetting);
                        edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                        ToastUtils.showShortToast(getString(R.string.text_save_key_success));
                    } catch (GeneralSecurityException e) {
                        Timber.w(e, "生成key失败");
                        ToastUtils.showShortToast(getString(R.string.text_create_token_and_save_key_failed));
                    }
                    return;
                }
                ToastUtils.showShortToast(getString(R.string.text_calling_key_same));
                break;
            case R.id.btn_save_length_and_width_normal_mode:
                float inputLengthFloatNormalMode = getInputContentToFloat(etRobotLengthNormalMode, 0, 1.5f);
                float inputWidthFloatNormalMode = getInputContentToFloat(etRobotWidthNormalMode, 0, 1.5f);
                if (inputWidthFloatNormalMode == Float.MAX_VALUE || inputLengthFloatNormalMode == Float.MAX_VALUE)
                    return;
                normalSetting.length = Float.parseFloat(decimalFormat.format(inputLengthFloatNormalMode));
                normalSetting.width = Float.parseFloat(decimalFormat.format(inputWidthFloatNormalMode));
                robotInfo.setModeNormalSetting(normalSetting);
                edit.putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                ToastUtils.showShortToast(getString(R.string.text_save_success));
                break;
            case R.id.btn_save_lidar_width_normal_mode:
                float inputLidarWidthFloatNormalMode = getInputContentToFloat(etLidarWidthNormalMode, 0.05f, 1);
                if (inputLidarWidthFloatNormalMode == Float.MAX_VALUE) return;
                normalSetting.lidarWidth = Float.parseFloat(decimalFormat.format(inputLidarWidthFloatNormalMode));
                robotInfo.setModeNormalSetting(normalSetting);
                edit.putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                ToastUtils.showShortToast(getString(R.string.text_save_success));
                break;
            case R.id.btn_save_length_and_width:
                float inputLengthFloat = getInputContentToFloat(etRobotLength, 0, 1.5f);
                float inputWidthFloat = getInputContentToFloat(etRobotWidth, 0, 1.5f);
                if (inputWidthFloat == Float.MAX_VALUE || inputLengthFloat == Float.MAX_VALUE)
                    return;
                qrCodeSetting.length = Float.parseFloat(decimalFormat.format(inputLengthFloat));
                qrCodeSetting.width = Float.parseFloat(decimalFormat.format(inputWidthFloat));
                robotInfo.setModeQRCodeSetting(qrCodeSetting);
                edit.putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
                ToastUtils.showShortToast(getString(R.string.text_save_success));
                break;
            case R.id.btn_save_orientation_and_distance_calibration:
                float inputOrientationAndDistanceCalibrationFloat = getInputContentToFloat(etOrientationAndDistanceCalibration, -2, 2);
                if (inputOrientationAndDistanceCalibrationFloat == Float.MAX_VALUE) return;
                qrCodeSetting.orientationAndDistanceCalibration = Float.parseFloat(decimalFormat.format(inputOrientationAndDistanceCalibrationFloat));
                robotInfo.setModeQRCodeSetting(qrCodeSetting);
                edit.putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
                ToastUtils.showShortToast(getString(R.string.text_save_success));
                break;
            case R.id.btn_save_lidar_width:
                float inputLidarWidthFloat = getInputContentToFloat(etLidarWidth, 0.05f, 1);
                if (inputLidarWidthFloat == Float.MAX_VALUE) return;
                qrCodeSetting.lidarWidth = Float.parseFloat(decimalFormat.format(inputLidarWidthFloat));
                robotInfo.setModeQRCodeSetting(qrCodeSetting);
                edit.putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
                ToastUtils.showShortToast(getString(R.string.text_save_success));
                break;
            case R.id.ib_decrease_start_calling_task_count_down_time:
                callingSetting.startTaskCountDownTime = onIntValueChange(v, false);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
            case R.id.ib_increase_start_calling_task_count_down_time:
                callingSetting.startTaskCountDownTime = onIntValueChange(v, true);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
            case R.id.ib_decrease_start_qrcode_task_count_down_time:
                qrCodeSetting.startTaskCountDownTime = onIntValueChange(v, false);
                robotInfo.setModeQRCodeSetting(qrCodeSetting);
                edit.putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
                break;
            case R.id.ib_increase_start_qrcode_task_count_down_time:
                qrCodeSetting.startTaskCountDownTime = onIntValueChange(v, true);
                robotInfo.setModeQRCodeSetting(qrCodeSetting);
                edit.putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
                break;
            case R.id.ib_decrease_start_route_task_count_down_time:
                routeSetting.startTaskCountDownTime = onIntValueChange(v, false);
                robotInfo.setModeRouteSetting(routeSetting);
                edit.putString(Constants.KEY_ROUTE_MODE_CONFIG, gson.toJson(routeSetting)).apply();
                break;
            case R.id.ib_increase_start_route_task_count_down_time:
                routeSetting.startTaskCountDownTime = onIntValueChange(v, true);
                robotInfo.setModeRouteSetting(routeSetting);
                edit.putString(Constants.KEY_ROUTE_MODE_CONFIG, gson.toJson(routeSetting)).apply();
                break;
            case R.id.ib_decrease_start_normal_task_count_down_time:
                normalSetting.startTaskCountDownTime = onIntValueChange(v, false);
                robotInfo.setModeNormalSetting(normalSetting);
                edit.putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                break;
            case R.id.ib_increase_start_normal_task_count_down_time:
                normalSetting.startTaskCountDownTime = onIntValueChange(v, true);
                robotInfo.setModeNormalSetting(normalSetting);
                edit.putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                break;
            case R.id.ib_increase_normal_mode_speed:
                normalSetting.speed = onFloatValueChange(v, true);
                robotInfo.setModeNormalSetting(normalSetting);
                edit.putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                break;
            case R.id.ib_decrease_normal_mode_speed:
                normalSetting.speed = onFloatValueChange(v, false);
                robotInfo.setModeNormalSetting(normalSetting);
                edit.putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                break;
            case R.id.ib_increase_route_mode_speed:
                routeSetting.speed = onFloatValueChange(v, true);
                robotInfo.setModeRouteSetting(routeSetting);
                edit.putString(Constants.KEY_ROUTE_MODE_CONFIG, gson.toJson(routeSetting)).apply();
                break;
            case R.id.ib_decrease_route_mode_speed:
                routeSetting.speed = onFloatValueChange(v, false);
                robotInfo.setModeRouteSetting(routeSetting);
                edit.putString(Constants.KEY_ROUTE_MODE_CONFIG, gson.toJson(routeSetting)).apply();
                break;
            case R.id.ib_increase_qrcode_mode_speed:
                qrCodeSetting.speed = onFloatValueChange(v, true);
                robotInfo.setModeQRCodeSetting(qrCodeSetting);
                edit.putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
                break;
            case R.id.ib_decrease_qrcode_mode_speed:
                qrCodeSetting.speed = onFloatValueChange(v, false);
                edit.putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
                break;
            case R.id.ib_increase_calling_mode_speed:
                callingSetting.speed = onFloatValueChange(v, true);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
            case R.id.ib_decrease_calling_mode_speed:
                callingSetting.speed = onFloatValueChange(v, false);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
            case R.id.ib_increase_calling_waiting_time:
                callingSetting.waitingTime = onIntValueChange(v, true);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
            case R.id.ib_decrease_calling_waiting_time:
                callingSetting.waitingTime = onIntValueChange(v, false);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
            case R.id.ib_increase_calling_cache_time:
                callingSetting.cacheTime = onIntValueChange(v, true);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
            case R.id.ib_decrease_calling_cache_time:
                callingSetting.cacheTime = onIntValueChange(v, false);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
            case R.id.btn_picking_time_setting:
                if (!normalSetting.waitingCountDownTimerOpen) {
                    ToastUtils.showShortToast(getString(R.string.text_check_not_open_waiting_time));
                    return;
                }
                new TimePickerDialog(requireActivity(), normalSetting.waitingTime, new TimePickerDialog.OnTimePickerConfirmListener() {
                    @Override
                    public void onConfirm(Dialog dialog, int waitingTime) {
                        normalSetting.waitingTime = waitingTime;
                        if (waitingTime <= 10) {
                            normalSetting.playArrivedTipTime = 0;
                            updatePlayArrivedTipTime(normalSetting.playArrivedTipTime);
                        } else if (waitingTime < normalSetting.playArrivedTipTime + 10) {
                            normalSetting.playArrivedTipTime = waitingTime - 10;
                            updatePlayArrivedTipTime(normalSetting.playArrivedTipTime);
                        }
                        updateWaitingTime(waitingTime);
                        robotInfo.setModeNormalSetting(normalSetting);
                        SpManager.getInstance().edit().putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                        dialog.dismiss();
                    }
                }).show();
                break;
            case R.id.btn_play_arrived_tip_time_setting:
                if (!normalSetting.waitingCountDownTimerOpen) {
                    ToastUtils.showShortToast(getString(R.string.text_check_not_open_waiting_time_cannot_set_play_arrived_tip_time));
                    return;
                }
                if (normalSetting.waitingTime <= 10) {
                    ToastUtils.showShortToast(getString(R.string.text_check_waiting_time_too_short_cannot_set_play_arrived_time));
                    return;
                }
                new TimePickerDialog(requireActivity(), normalSetting.playArrivedTipTime, 0, new TimePickerDialog.OnTimePickerConfirmListener() {
                    @Override
                    public void onConfirm(Dialog dialog, int waitingTime) {
                        if (waitingTime > normalSetting.waitingTime - 10) {
                            ToastUtils.showShortToast(getString(R.string.text_play_arrived_tip_time_must_smaller_than_waiting_time));
                            return;
                        }
                        normalSetting.playArrivedTipTime = waitingTime;
                        updatePlayArrivedTipTime(waitingTime);
                        robotInfo.setModeNormalSetting(normalSetting);
                        SpManager.getInstance().edit().putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                        dialog.dismiss();
                    }
                }).show();
                break;
            case R.id.btn_calling_config:
                if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
                if (robotInfo.isElevatorMode()) {
                    if (robotInfo.getProductionPointMap() == null) {
                        EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.text_please_choose_production_point_map));
                        return;
                    }
                }
                if (CallingHelper.INSTANCE.isStart()) {
                    CallingHelper.INSTANCE.stop();
                }
                EasyDialog.getLoadingInstance(requireActivity()).loading(getString(R.string.text_enter_calling_config));
                mHandler.postDelayed(() -> {
                    if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
                    BaseActivity.startup(getContext(), CallingConfigActivity.class);
                }, 2000);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

    }

    public void updateWaitingTime(int seconds) {
        int millisSeconds = seconds * 1000;
        int hour = (int) TimeUnit.MILLISECONDS.toHours(millisSeconds);
        int minute = (int) TimeUnit.MILLISECONDS.toMinutes(millisSeconds) % 60;
        int second = (int) TimeUnit.MILLISECONDS.toSeconds(millisSeconds) % 60;
        tvNormalModePickingHours.setText(String.valueOf(hour));
        tvNormalModePickingMinutes.setText(String.valueOf(minute));
        tvNormalModePickingSeconds.setText(String.valueOf(second));
    }

    public void updatePlayArrivedTipTime(int seconds) {
        int millisSeconds = seconds * 1000;
        int hour = (int) TimeUnit.MILLISECONDS.toHours(millisSeconds);
        int minute = (int) TimeUnit.MILLISECONDS.toMinutes(millisSeconds) % 60;
        int second = (int) TimeUnit.MILLISECONDS.toSeconds(millisSeconds) % 60;
        tvNormalModePlayArrivedTipHours.setText(String.valueOf(hour));
        tvNormalModePlayArrivedTipMinutes.setText(String.valueOf(minute));
        tvNormalModePlayArrivedSeconds.setText(String.valueOf(second));
    }

    private float getInputContentToFloat(EditText editText, float min, float max) {
        String inputStr = editText.getText().toString();
        if (TextUtils.isEmpty(inputStr)) {
            editText.setError(getString(R.string.text_please_input_within_range, String.valueOf(min), String.valueOf(max)));
            return Float.MAX_VALUE;
        }
        try {
            float inputFloat = Float.parseFloat(inputStr);
            if (inputFloat < min || inputFloat > max) {
                editText.setError(getString(R.string.text_please_input_within_range, String.valueOf(min), String.valueOf(max)));
                return Float.MAX_VALUE;
            }
            return inputFloat;
        } catch (NumberFormatException e) {
            editText.setError(getString(R.string.text_please_input_within_range, String.valueOf(min), String.valueOf(max)));
            return Float.MAX_VALUE;
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
            case R.id.isb_start_calling_task_count_down_time:
                callingSetting.startTaskCountDownTime = onIntValueChange(seekBar, false);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
            case R.id.isb_start_qrcode_task_count_down_time:
                qrCodeSetting.startTaskCountDownTime = onIntValueChange(seekBar, false);
                robotInfo.setModeQRCodeSetting(qrCodeSetting);
                edit.putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
                break;
            case R.id.isb_start_route_task_count_down_time:
                routeSetting.startTaskCountDownTime = onIntValueChange(seekBar, false);
                robotInfo.setModeRouteSetting(routeSetting);
                edit.putString(Constants.KEY_ROUTE_MODE_CONFIG, gson.toJson(routeSetting)).apply();
                break;
            case R.id.isb_start_normal_task_count_down_time:
                normalSetting.startTaskCountDownTime = onIntValueChange(seekBar, false);
                robotInfo.setModeNormalSetting(normalSetting);
                edit.putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                break;
            case R.id.isb_adjust_normal_mode_speed:
                normalSetting.speed = onFloatValueChange(seekBar, false);
                robotInfo.setModeNormalSetting(normalSetting);
                edit.putString(Constants.KEY_NORMAL_MODE_CONFIG, gson.toJson(normalSetting)).apply();
                break;
            case R.id.isb_adjust_route_mode_speed:
                routeSetting.speed = onFloatValueChange(seekBar, false);
                robotInfo.setModeRouteSetting(routeSetting);
                edit.putString(Constants.KEY_ROUTE_MODE_CONFIG, gson.toJson(routeSetting)).apply();
                break;
            case R.id.isb_adjust_qrcode_mode_speed:
                qrCodeSetting.speed = onFloatValueChange(seekBar, false);
                robotInfo.setModeQRCodeSetting(qrCodeSetting);
                edit.putString(Constants.KEY_QRCODE_MODE_CONFIG, gson.toJson(qrCodeSetting)).apply();
                break;
            case R.id.isb_adjust_calling_mode_speed:
                callingSetting.speed = onFloatValueChange(seekBar, false);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
            case R.id.isb_calling_waiting_time:
                callingSetting.waitingTime = onIntValueChange(seekBar, false);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
            case R.id.isb_calling_cache_time:
                callingSetting.cacheTime = onIntValueChange(seekBar, false);
                callingInfo.setCallingModeSetting(callingSetting);
                edit.putString(Constants.KEY_CALLING_MODE_CONFIG, gson.toJson(callingSetting)).apply();
                break;
        }
    }
}
