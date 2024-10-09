package com.reeman.agv.fragments.setting;


import static com.reeman.agv.adapter.BroadcastItemAdapter.TYPE_OBSTACLE_PROMPT;
import static com.reeman.agv.base.BaseApplication.ros;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import timber.log.Timber;

import com.google.gson.Gson;
import com.kyleduo.switchbutton.SwitchButton;
import com.reeman.agv.R;
import com.reeman.agv.activities.AliasSettingActivity;
import com.reeman.agv.adapter.BroadcastItemAdapter;
import com.reeman.agv.base.BaseFragment;
import com.reeman.agv.calling.CallingInfo;
import com.reeman.agv.calling.utils.CallingStateManager;
import com.reeman.agv.constants.Errors;
import com.reeman.commons.event.ApplyMapEvent;
import com.reeman.commons.event.CurrentMapEvent;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.settings.BackgroundMusicSetting;
import com.reeman.commons.settings.CommutingTimeSetting;
import com.reeman.commons.state.NavigationMode;
import com.reeman.agv.contract.BasicSettingContract;
import com.reeman.agv.presenter.impl.BasicSettingPresenter;
import com.reeman.points.model.request.MapVO;
import com.reeman.commons.settings.ObstacleSetting;
import com.reeman.commons.utils.ClickRestrict;
import com.reeman.commons.utils.SpManager;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.utils.VoiceHelper;
import com.reeman.agv.widgets.EasyDialog;
import com.reeman.agv.widgets.EditorCallback;
import com.reeman.agv.widgets.EditorHolder;
import com.reeman.agv.widgets.ExpandableLayout;
import com.reeman.agv.widgets.FloatEditorActivity;
import com.reeman.agv.widgets.FloatingCallingListView;
import com.reeman.agv.widgets.MapChooseDialog;
import com.reeman.agv.widgets.ShowMusicDialog;
import com.reeman.agv.widgets.TimePickerDialog;
import com.reeman.points.utils.PointCacheInfo;
import com.reeman.points.utils.PointCacheUtil;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class BasicSettingFragment extends BaseFragment implements BasicSettingContract.View, BroadcastItemAdapter.onItemDeleteListener, EasyDialog.OnViewClickListener, MapChooseDialog.OnMapListItemSelectedListener, ExpandableLayout.OnExpandListener, ShowMusicDialog.ShowMusicDialogListener {

    private BasicSettingPresenter presenter;
    private IndicatorSeekBar isbPower;
    private IndicatorSeekBar isbBrightness;
    private IndicatorSeekBar isbVolume;
    private ObstacleSetting obstacleModeSetting;
    private BroadcastItemAdapter obstacleAdapter;
    private double[] lastRelocateCoordinate;
    private int currentCheckId;
    private RadioGroup rgSettingPasswordControl;

    private RadioGroup rgSettingCommutingTimeControl;

    private TextView tvWorkingTime;

    private TextView tvAfterWorkTime;

    private LinearLayout layoutCommutingTime;

    private LinearLayout layoutAutoWorkPower;

    private IndicatorSeekBar isbAutoWorkPower;

    private CommutingTimeSetting commutingTimeSetting;

    private RadioGroup rgNavigationModelControl;

    private RadioGroup rgLiftModelControl;

    private RadioGroup rgStopNearby;

    private RadioGroup.OnCheckedChangeListener listener;

    private TextView tvAlias;

    private RadioGroup rgPointShowSetting;
    private Gson gson;

    private Button btnNormalBackgroundMusicChoose;
    private SwitchButton btnNormalBackgroundMusic;
    private TextView btnNormalBackgroundMusicShow;

    private BackgroundMusicSetting backgroundMusicSetting;

    private boolean isFloatEditorShow = false;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_basic_setting;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new Gson();
        presenter = new BasicSettingPresenter(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMediaVolume();

        initBrightness();

        initPower();

        initObstacleSetting();

        initSettingPasswordControl();

        initNavigationModelSetting();

        initRelocate();

        initDockChargingPile();

        initSwitchMap();

        initSettingCommutingTimeControl();

        initLiftModelSetting();

        initStopNearbySetting();

        initAliasSetting();

        initBackgroundMusic();

        initPointShowSetting();
    }

    @Override
    public void onResume() {
        super.onResume();
        tvAlias.setText(robotInfo.getRobotAlias());
    }

    private void initPointShowSetting() {
        rgPointShowSetting = $(R.id.rg_point_show_setting);
        $(R.id.rb_scroll_show).setOnClickListener(this);
        $(R.id.rb_pagination_show).setOnClickListener(this);
        rgPointShowSetting.check(robotInfo.isPointScrollShow() ? R.id.rb_scroll_show : R.id.rb_pagination_show);
    }

    private void initAliasSetting() {
        tvAlias = $(R.id.tv_alias);
        $(R.id.btn_change).setOnClickListener(this);
    }

    private void initStopNearbySetting() {
        rgStopNearby = $(R.id.rg_setting_stop_nearby);
        $(R.id.rb_open_stop_nearby).setOnClickListener(this);
        $(R.id.rb_close_stop_nearby).setOnClickListener(this);
        rgStopNearby.check(robotInfo.isStopNearbyOpen() ? R.id.rb_open_stop_nearby : R.id.rb_close_stop_nearby);
    }

    private void initLiftModelSetting() {
        if (!robotInfo.isSpaceShip()) {
            $(R.id.layout_lift_model_setting).setVisibility(View.GONE);
            return;
        }
        rgLiftModelControl = $(R.id.rg_setting_with_lift_model_control);
        $(R.id.rb_with_lift_model).setOnClickListener(this);
        $(R.id.rb_without_lift_model).setOnClickListener(this);
        rgLiftModelControl.check(robotInfo.isLiftModelInstalled() ? R.id.rb_with_lift_model : R.id.rb_without_lift_model);
    }

    private void initBackgroundMusic() {
        if (backgroundMusicSetting == null) {
            String backgroundMusicSettingConfigStr = SpManager.getInstance().getString(Constants.KEY_BACKGROUND_MUSIC, null);
            if (TextUtils.isEmpty(backgroundMusicSettingConfigStr)) {
                backgroundMusicSetting = new BackgroundMusicSetting(false, new ArrayList<>(), new ArrayList<>());
            } else {
                backgroundMusicSetting = new Gson().fromJson(backgroundMusicSettingConfigStr, BackgroundMusicSetting.class);
            }
        }
        btnNormalBackgroundMusicChoose = root.findViewById(R.id.btn_music_choose_normal_mode);
        btnNormalBackgroundMusicChoose.setOnClickListener(this);
        btnNormalBackgroundMusic = root.findViewById(R.id.sw_route_mode_enable_background_music);

        String concatenatedFileNames = getMusic();
        //点击开关
        btnNormalBackgroundMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                backgroundMusicSetting.enableBackgroundMusic = b;
                if (b) {
                    btnNormalBackgroundMusicChoose.setVisibility(View.VISIBLE);
                    btnNormalBackgroundMusicShow.setText(getMusic().isEmpty() ? getString(R.string.text_do_not_play_background_music) : getMusic());
                } else {
                    btnNormalBackgroundMusicChoose.setVisibility(View.GONE);
                    btnNormalBackgroundMusicShow.setText(getString(R.string.text_do_not_play_background_music));
                }
                //保证数据
                SpManager.getInstance().edit().putString(Constants.KEY_BACKGROUND_MUSIC, gson.toJson(backgroundMusicSetting)).apply();
            }
        });
        btnNormalBackgroundMusicShow = root.findViewById(R.id.et_music_normal_mode);
        btnNormalBackgroundMusicShow.setText((!backgroundMusicSetting.enableBackgroundMusic || concatenatedFileNames.isEmpty()) ? getString(R.string.text_do_not_play_background_music) : concatenatedFileNames);
        btnNormalBackgroundMusic.setChecked(backgroundMusicSetting.enableBackgroundMusic);
        btnNormalBackgroundMusicChoose.setVisibility(backgroundMusicSetting.enableBackgroundMusic ? View.VISIBLE : View.GONE);
    }


    private void initSettingCommutingTimeControl() {
        rgSettingCommutingTimeControl = $(R.id.rg_setting_commuting_time_control);
        $(R.id.rb_open_commuting_time).setOnClickListener(this);
        $(R.id.rb_close_commuting_time).setOnClickListener(this);
        tvWorkingTime = $(R.id.tv_working_time);
        tvAfterWorkTime = $(R.id.tv_after_work_time);
        $(R.id.btn_working_time_setting).setOnClickListener(this);
        $(R.id.btn_after_work_time_setting).setOnClickListener(this);
        layoutCommutingTime = $(R.id.layout_commuting_time);
        layoutAutoWorkPower = $(R.id.layout_auto_work_power);
        isbAutoWorkPower = $(R.id.isb_auto_work_power);
        $(R.id.ib_decrease_auto_work_power).setOnClickListener(this);
        $(R.id.ib_increase_auto_work_power).setOnClickListener(this);
        commutingTimeSetting = robotInfo.getCommutingTimeSetting();
        layoutCommutingTime.setVisibility(commutingTimeSetting.open ? View.VISIBLE : View.GONE);
        layoutAutoWorkPower.setVisibility(commutingTimeSetting.open ? View.VISIBLE : View.GONE);
        rgSettingCommutingTimeControl.check(commutingTimeSetting.open ? R.id.rb_open_commuting_time : R.id.rb_close_commuting_time);
        tvWorkingTime.setText(getString(R.string.text_working_time, commutingTimeSetting.workingTime));
        tvAfterWorkTime.setText(getString(R.string.text_after_work_time, commutingTimeSetting.afterWorkTime));
        isbAutoWorkPower.setProgress(commutingTimeSetting.autoWorkPower);
        isbAutoWorkPower.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress <= robotInfo.getAutoChargePowerLevel() + 10) {
                    progress = robotInfo.getAutoChargePowerLevel() + 10;
                    ToastUtils.showShortToast(getString(R.string.text_working_power_level_must_bigger_than_auto_charge));
                }
                if (progress >= 90) progress = 90;
                seekBar.setProgress(progress);
                commutingTimeSetting.autoWorkPower = progress;
                robotInfo.setCommutingTimeSetting(commutingTimeSetting);
                SpManager.getInstance().edit().putString(Constants.KEY_COMMUTING_TIME_SETTING, gson.toJson(commutingTimeSetting)).apply();
            }
        });
    }

    private void initSettingPasswordControl() {
        rgSettingPasswordControl = $(R.id.rg_setting_password_control);
        int settingPasswordControl = SpManager.getInstance().getInt(Constants.KEY_SETTING_PASSWORD_CONTROL, Constants.KEY_DEFAULT_SETTING_PASSWORD_CONTROL);
        rgSettingPasswordControl.check(settingPasswordControl == 0 ? R.id.rb_close_password : R.id.rb_open_password);
        listener = (group, checkedId) -> {
            currentCheckId = checkedId;
            EasyDialog.newCustomInstance(requireContext(), R.layout.layout_input_setting_password).showInputPasswordDialog(getString(R.string.text_please_verify_password), BasicSettingFragment.this);
        };
        rgSettingPasswordControl.setOnCheckedChangeListener(listener);
    }

    @Override
    public void onViewClick(Dialog dialog, int id) {
        if (id == R.id.btn_confirm) {
            EditText editText = (EditText) EasyDialog.getInstance().getView(R.id.et_password);
            String str = editText.getText().toString();
            if (TextUtils.equals(str, Constants.KEY_SETTING_PASSWORD)) {
                dialog.dismiss();
                SpManager.getInstance().edit().putInt(Constants.KEY_SETTING_PASSWORD_CONTROL, currentCheckId == R.id.rb_close_password ? 0 : 1).apply();
            } else {
                ToastUtils.showShortToast(getString(R.string.text_password_error));
            }
        } else {
            dialog.dismiss();
            int settingPasswordControl = SpManager.getInstance().getInt(Constants.KEY_SETTING_PASSWORD_CONTROL, Constants.KEY_DEFAULT_SETTING_PASSWORD_CONTROL);
            rgSettingPasswordControl.setOnCheckedChangeListener(null);
            rgSettingPasswordControl.check(settingPasswordControl == 0 ? R.id.rb_close_password : R.id.rb_open_password);
            rgSettingPasswordControl.setOnCheckedChangeListener(listener);
        }
    }

    private void initNavigationModelSetting() {
        rgNavigationModelControl = $(R.id.rg_setting_navigation_model_control);
        RadioButton rbAutoModel = $(R.id.rb_auto_model);
        RadioButton rbFixModel = $(R.id.rb_fix_model);
        rbAutoModel.setOnClickListener(this);
        rbFixModel.setOnClickListener(this);
        rgNavigationModelControl.check(robotInfo.getNavigationMode() == NavigationMode.autoPathMode ? R.id.rb_auto_model : R.id.rb_fix_model);
    }

    private void initSwitchMap() {
        Button btnSwitchMap = $(R.id.btn_switch_map);
        btnSwitchMap.setOnClickListener(this);
    }


    private void initObstacleSetting() {
        if (obstacleModeSetting == null) {
            String obstacleSetting = SpManager.getInstance().getString(Constants.KEY_OBSTACLE_CONFIG, null);
            if (TextUtils.isEmpty(obstacleSetting)) {
                obstacleModeSetting = ObstacleSetting.getDefault();
            } else {
                obstacleModeSetting = gson.fromJson(obstacleSetting, ObstacleSetting.class);
            }
        }

        SwitchButton swEnableObstaclePrompt = $(R.id.sw_enable_obstacle_prompt);
        ImageButton ibAddBroadcastItem = $(R.id.ib_add_encounter_obstacle_prompt);
        ibAddBroadcastItem.setOnClickListener(v -> {
            if (obstacleModeSetting.obstaclePrompts != null && obstacleModeSetting.obstaclePrompts.size() >= Constants.MAX_AUDIO_FILE_COUNT) {
                ToastUtils.showShortToast(getString(R.string.text_too_many_audio_files));
                return;
            }
            if (isFloatEditorShow || ClickRestrict.restrictFrequency(500)) return;
            FloatEditorActivity.openEditor(requireContext(),
                    new EditorHolder(R.layout.fast_reply_floating_layout, R.id.tv_cancel, R.id.tv_submit, R.id.et_content),
                    new EditorCallback() {

                        @Override
                        public void onTryListen(Activity activity, String content, View cancel, View submit) {
                            presenter.tryListen(requireContext(), Constants.KEY_DEFAULT_OBSTACLE_ASSETS_PREFIX, content, "", cancel, submit);
                        }

                        @Override
                        public void onConfirm(Activity activity, String content, View cancel, View submit) {
                            obstacleModeSetting.obstaclePrompts.add(presenter.getLastTryListenText());
                            obstacleModeSetting.obstaclePromptAudioList.add(presenter.getLastGeneratedFile());
                            obstacleAdapter.setTextContentList(obstacleModeSetting.obstaclePrompts);
                            SpManager.getInstance().edit().putString(Constants.KEY_OBSTACLE_CONFIG, gson.toJson(obstacleModeSetting)).apply();
                            activity.finish();
                        }

                        @Override
                        public void onAttached(ViewGroup rootView) {
                            isFloatEditorShow = true;
                        }

                        @Override
                        public void onFinish() {
                            isFloatEditorShow = false;

                        }
                    });
        });
        swEnableObstaclePrompt.setChecked(obstacleModeSetting.enableObstaclePrompt);
        swEnableObstaclePrompt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            obstacleModeSetting.enableObstaclePrompt = isChecked;
            SpManager.getInstance().edit().putString(Constants.KEY_OBSTACLE_CONFIG, gson.toJson(obstacleModeSetting)).apply();
        });

        RecyclerView rvObstaclePrompt = $(R.id.rv_obstacle_prompt);
        obstacleAdapter = new BroadcastItemAdapter(TYPE_OBSTACLE_PROMPT);
        obstacleAdapter.setCheckedList(obstacleModeSetting.targetObstaclePrompts);
        obstacleAdapter.setListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvObstaclePrompt.setLayoutManager(layoutManager);
        rvObstaclePrompt.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
        rvObstaclePrompt.setAdapter(obstacleAdapter);

        if (obstacleModeSetting.obstaclePrompts != null && !obstacleModeSetting.obstaclePrompts.isEmpty()) {
            obstacleAdapter.setTextContentList(obstacleModeSetting.obstaclePrompts);
        }
    }

    private void initMediaVolume() {
        ImageButton ibDecreaseVolume = $(R.id.ib_decrease_volume);
        ImageButton ibIncreaseVolume = $(R.id.ib_increase_volume);
        ibDecreaseVolume.setOnClickListener(this);
        ibIncreaseVolume.setOnClickListener(this);
        isbVolume = $(R.id.isb_volume);
        int mediaVolume = SpManager.getInstance().getInt(Constants.KEY_MEDIA_VOLUME, Constants.DEFAULT_MEDIA_VOLUME);
        isbVolume.setProgress(mediaVolume);
        isbVolume.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                int progress = seekBar.getProgress();
                saveMediaVolume(progress);
            }
        });
    }

    private void saveMediaVolume(int progress) {
        SpManager.getInstance().edit().putInt(Constants.KEY_MEDIA_VOLUME, progress).apply();
    }

    private void initBrightness() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            $(R.id.layout_brightness).setVisibility(View.GONE);
            return;
        }
        ImageButton ibDecreaseBrightness = $(R.id.ib_decrease_brightness);
        ImageButton ibIncreaseBrightness = $(R.id.ib_increase_brightness);
        ibDecreaseBrightness.setOnClickListener(this);
        ibIncreaseBrightness.setOnClickListener(this);
        isbBrightness = $(R.id.isb_brightness);
        int screenBrightness = SpManager.getInstance().getInt(Constants.KEY_SCREEN_BRIGHTNESS, Constants.DEFAULT_SCREEN_BRIGHTNESS);
        isbBrightness.setProgress(screenBrightness);
        isbBrightness.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                int progress = seekBar.getProgress();
                saveBrightness(progress);
            }
        });
    }

    private void saveBrightness(int progress) {
        SpManager.getInstance().edit().putInt(Constants.KEY_SCREEN_BRIGHTNESS, progress).apply();
        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        Settings.System.putInt(requireContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) (progress / 100.0 * 255));
        requireActivity().getContentResolver().notifyChange(uri, null);
    }

    private void initPower() {
        ImageButton ibDecreasePower = $(R.id.ib_decrease_power);
        ImageButton ibIncreasePower = $(R.id.ib_increase_power);
        isbPower = $(R.id.isb_power);
        ibDecreasePower.setOnClickListener(this);
        ibIncreasePower.setOnClickListener(this);
        int lowPower = robotInfo.getAutoChargePowerLevel();
        isbPower.setProgress(lowPower);
        isbPower.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress <= 10) progress = 10;
                if (progress >= 80) progress = 80;
                seekBar.setProgress(progress);
                savePower(progress);
            }
        });
    }

    private void savePower(int progress) {
        if (commutingTimeSetting.open) {
            if (progress > commutingTimeSetting.autoWorkPower - 10) {
                progress = commutingTimeSetting.autoWorkPower - 10;
                isbPower.setProgress(progress);
                ToastUtils.showShortToast(getString(R.string.text_auto_charge_level_must_bigger_than_working_power_level));
            }
        }
        robotInfo.setAutoChargePowerLevel(progress);
        CallingStateManager.INSTANCE.setLowPowerEvent(robotInfo.isLowPower());
        SpManager.getInstance().edit().putInt(Constants.KEY_LOW_POWER, progress).apply();
    }

    private void initRelocate() {
        Button btnRelocate = $(R.id.btn_relocate);
        btnRelocate.setOnClickListener(this);
    }

    private void initDockChargingPile() {
        Button btnDockChargingPile = $(R.id.btn_dock_charging_pile);
        btnDockChargingPile.setOnClickListener(this);
    }


    @Override
    protected void onCustomClickResult(int id) {
        switch (id) {
            case R.id.rb_scroll_show:
                if (robotInfo.isPointScrollShow()) break;
            case R.id.rb_pagination_show:
                if (id == R.id.rb_pagination_show && !robotInfo.isPointScrollShow()) break;
                rgPointShowSetting.check(id);
                robotInfo.setPointScrollShow(id == R.id.rb_scroll_show);
                SpManager.getInstance().edit().putBoolean(Constants.KEY_POINT_SHOW_MODE, id == R.id.rb_scroll_show).apply();
                break;
            case R.id.rb_open_stop_nearby:
                if (robotInfo.isStopNearbyOpen()) break;
            case R.id.rb_close_stop_nearby:
                if (id == R.id.rb_close_stop_nearby && !robotInfo.isStopNearbyOpen()) break;
                rgStopNearby.check(id);
                robotInfo.setStopNearbyOpen(id == R.id.rb_open_stop_nearby);
                SpManager.getInstance().edit().putBoolean(Constants.KEY_STOP_NEARBY_CONTROL, id == R.id.rb_open_stop_nearby).apply();
                break;
            case R.id.rb_with_lift_model:
                if (robotInfo.isLiftModelInstalled()) break;
            case R.id.rb_without_lift_model:
                if (id == R.id.rb_without_lift_model && !robotInfo.isLiftModelInstalled()) break;
                rgLiftModelControl.check(id);
                robotInfo.setLiftModelInstalled(id == R.id.rb_with_lift_model);
                SpManager.getInstance().edit().putBoolean(Constants.KEY_LIFT_MODEL_INSTALLATION, id == R.id.rb_with_lift_model).apply();
                break;
            case R.id.ib_decrease_auto_work_power:
            case R.id.ib_increase_auto_work_power:
                onAdjustAutoWorkPowerBtnClick(id);
                break;
            case R.id.rb_open_commuting_time:
            case R.id.rb_close_commuting_time:
                if (commutingTimeSetting.open && id == R.id.rb_open_commuting_time) break;
                if (!commutingTimeSetting.open && id == R.id.rb_close_commuting_time) break;
                commutingTimeSetting.open = id == R.id.rb_open_commuting_time;
                layoutCommutingTime.setVisibility(id == R.id.rb_open_commuting_time ? View.VISIBLE : View.GONE);
                layoutAutoWorkPower.setVisibility(id == R.id.rb_open_commuting_time ? View.VISIBLE : View.GONE);
                robotInfo.setCommutingTimeSetting(commutingTimeSetting);
                SpManager.getInstance().edit().putString(Constants.KEY_COMMUTING_TIME_SETTING, gson.toJson(commutingTimeSetting)).apply();
                break;
            case R.id.btn_working_time_setting:
            case R.id.btn_after_work_time_setting:
                new TimePickerDialog(requireActivity(), id == R.id.btn_working_time_setting ? commutingTimeSetting.workingTime : commutingTimeSetting.afterWorkTime, new TimePickerDialog.OnTimePickerConfirmListener() {
                    @Override
                    public void onConfirm(Dialog dialog, String time) {
                        if (id == R.id.btn_working_time_setting) {
                            if (time.equals(commutingTimeSetting.afterWorkTime)) {
                                ToastUtils.showShortToast(getString(R.string.text_working_time_and_after_work_time_cannot_same));
                                dialog.dismiss();
                                return;
                            }
                            if (time.equals(commutingTimeSetting.workingTime)) {
                                dialog.dismiss();
                                return;
                            }
                            commutingTimeSetting.workingTime = time;
                            tvWorkingTime.setText(getString(R.string.text_working_time, time));
                        } else {
                            if (time.equals(commutingTimeSetting.workingTime)) {
                                ToastUtils.showShortToast(getString(R.string.text_working_time_and_after_work_time_cannot_same));
                                dialog.dismiss();
                                return;
                            }
                            if (time.equals(commutingTimeSetting.afterWorkTime)) {
                                dialog.dismiss();
                                return;
                            }
                            commutingTimeSetting.afterWorkTime = time;
                            tvAfterWorkTime.setText(getString(R.string.text_after_work_time, time));
                        }
                        robotInfo.setCommutingTimeSetting(commutingTimeSetting);
                        SpManager.getInstance().edit().putString(Constants.KEY_COMMUTING_TIME_SETTING, gson.toJson(commutingTimeSetting)).apply();
                        dialog.dismiss();
                    }
                }).show();
                break;
            case R.id.rb_auto_model:
                if (robotInfo.getNavigationMode() == NavigationMode.autoPathMode) break;
            case R.id.rb_fix_model:
                if (robotInfo.getNavigationMode() == NavigationMode.fixPathMode && id == R.id.rb_fix_model)
                    break;
                EasyDialog.getInstance(requireActivity()).confirm(getString(R.string.text_clear_all_points_after_confirm), (dialog, mId) -> {
                    if (mId == R.id.btn_confirm) {
                        int currentNavigationModel = id == R.id.rb_auto_model ? NavigationMode.autoPathMode : NavigationMode.fixPathMode;
                        robotInfo.setNavigationMode(currentNavigationModel);
                        PointCacheInfo.INSTANCE.clearAllCacheData();
                        PointCacheUtil.INSTANCE.clearAllLocalData();
                        if (FloatingCallingListView.isShow())
                            FloatingCallingListView.getInstance().close();
                        CallingInfo.INSTANCE.removeAllCallingPoints();
                        SpManager.getInstance().edit().putInt(Constants.KEY_NAVIGATION_MODEL,currentNavigationModel).apply();
                    } else {
                        rgNavigationModelControl.check(robotInfo.getNavigationMode() == NavigationMode.autoPathMode ? R.id.rb_auto_model : R.id.rb_fix_model);
                    }
                    dialog.dismiss();
                });
                break;
            case R.id.ib_decrease_brightness:
            case R.id.ib_increase_brightness:
                onAdjustBrightnessBtnClick(id);
                break;
            case R.id.ib_increase_volume:
            case R.id.ib_decrease_volume:
                onAdjustVolumeBtnClick(id);
                break;
            case R.id.ib_increase_power:
            case R.id.ib_decrease_power:
                onAdjustPowerBtnClick(id);
                break;
            case R.id.btn_dock_charging_pile:
                onDockChargingPileBtnClick();
                break;
            case R.id.btn_relocate:
                onRelocateBtnClick();
                break;
            case R.id.btn_switch_map:
                onSwitchMap();
                break;
            case R.id.btn_change:
                startActivity(new Intent(requireContext(), AliasSettingActivity.class));
                break;
            case R.id.btn_music_choose_normal_mode:
                presenter.loadBackgroundMusic(requireContext(), robotInfo.getROSIPAddress());
                break;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

    }

    private void onSwitchMap() {
        presenter.onSwitchMap(requireContext());
    }

    @Override
    public void onMapListLoaded(List<MapVO> list) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        MapChooseDialog mapChooseDialog = new MapChooseDialog(requireContext(), list, true, true, this);
        mapChooseDialog.show();
    }

    @Override
    public void onMapListLoadedFailed(Throwable throwable) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        ToastUtils.showShortToast(getString(R.string.text_load_map_failed));
    }

    @Override
    public void onChargeDataLoadSuccess() {
        if (EasyDialog.isShow())
            EasyDialog.getInstance().dismiss();
        if (robotInfo.isElevatorMode()) {
            CurrentMapEvent currentMapEvent = robotInfo.getCurrentMapEvent();
            if (robotInfo.getChargingPileMap() == null) {
                EasyDialog.getInstance(requireContext()).warnError(getString(R.string.text_please_choose_charging_pile_map));
                return;
            }
            if (TextUtils.isEmpty(currentMapEvent.getAlias()) || !robotInfo.getChargingPileMap().getFirst().equals(currentMapEvent.getAlias())) {
                EasyDialog.getInstance(requireContext()).confirm(getString(R.string.text_please_switch_to_charging_point_first, currentMapEvent.getAlias()), new EasyDialog.OnViewClickListener() {
                    @Override
                    public void onViewClick(Dialog dialog, int id) {
                        dialog.dismiss();
                        if (id == R.id.btn_confirm) {
                            EasyDialog.getLoadingInstance(requireContext()).loading(getString(R.string.text_changing_map));
                            ros.applyMap(robotInfo.getChargingPileMap().getSecond());
                        }
                    }
                });
                return;
            }
        }
        lastRelocateCoordinate = null;

        EasyDialog.getInstance(requireContext()).confirm(getString(R.string.text_relocate_prompt), (dialog, id) -> {
            if (id == R.id.btn_confirm) {
                if (!robotInfo.isWirelessCharging()) {
                    ToastUtils.showShortToast(getString(R.string.voice_please_dock_charging_pile));
                    VoiceHelper.play("voice_please_dock_charging_pile");
                    return;
                }
                dialog.dismiss();

                lastRelocateCoordinate = PointCacheInfo.INSTANCE.getChargePoint().getSecond().getPosition();
                presenter.relocate(lastRelocateCoordinate);
            } else {
                lastRelocateCoordinate = null;
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onDataLoadFailed(Throwable throwable) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        String errorTip = Errors.INSTANCE.getDataLoadFailedTip(requireActivity(), throwable);
        if (!TextUtils.isEmpty(errorTip)) {
            EasyDialog.getInstance(requireContext()).warnError(errorTip);
        }
    }

    @Override
    public void onMusicListLoaded(List<String> music, String ip) {
        new Handler(Looper.getMainLooper()).post(() -> showBackgroundMusicSelectDialog(music, ip));
    }

    @Override
    public void onMusicListFailed(Throwable e) {
        if (EasyDialog.isShow()) {
            EasyDialog.getInstance().dismiss();
        }
        ToastUtils.showShortToast(getString(R.string.text_point_loaded_failed) + e);
    }

    @Override
    public void onMapListItemSelected(MapChooseDialog mapChooseDialog, MapVO map, boolean checkChargingPile) {
        CurrentMapEvent currentMapEvent = robotInfo.getCurrentMapEvent();
        Timber.w("map : " + map.name + " : " + currentMapEvent.getMap());
        if (map.name.equals(currentMapEvent.getMap())) {
            ToastUtils.showShortToast(getString(R.string.text_do_not_apply_map_repeatedly));
            return;
        }
        mapChooseDialog.dismiss();
        EasyDialog.getLoadingInstance(requireContext()).loading(getString(R.string.text_changing_map));
        ros.applyMap(map.name);
    }

    @Override
    public void onApplyMapEvent(@NonNull ApplyMapEvent event) {
        super.onApplyMapEvent(event);
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        if (event.isSuccess()) {
            ToastUtils.showShortToast(getString(R.string.text_switch_map_success));
        } else {
            ToastUtils.showShortToast(getString(R.string.text_switch_map_failure));
        }
    }

    @Override
    public void onNoMapSelected(boolean checkChargingPile) {
        ToastUtils.showShortToast(getString(R.string.text_please_choose_map));
    }

    private void onAdjustAutoWorkPowerBtnClick(int id) {
        int progress = isbAutoWorkPower.getProgress();
        if (id == R.id.ib_decrease_auto_work_power) {
            if (progress <= robotInfo.getAutoChargePowerLevel() + 10) {
                ToastUtils.showShortToast(getString(R.string.text_working_power_level_must_bigger_than_auto_charge));
                return;
            }
            progress--;
        } else {
            if (progress >= 90) return;
            progress++;
        }
        isbAutoWorkPower.setProgress(progress);
        commutingTimeSetting.autoWorkPower = progress;
        robotInfo.setCommutingTimeSetting(commutingTimeSetting);
        SpManager.getInstance().edit().putString(Constants.KEY_COMMUTING_TIME_SETTING, gson.toJson(commutingTimeSetting)).apply();
    }

    private void showBackgroundMusicSelectDialog(@NotNull List<String> music, String ip) {
        if (EasyDialog.isShow()) {
            EasyDialog.getInstance().dismiss();

            if (backgroundMusicSetting.backgroundMusicFileNames != null) {
                new ShowMusicDialog(requireContext(), music, backgroundMusicSetting.backgroundMusicFileNames, ip, this).show();
            } else {
                new ShowMusicDialog(requireContext(), music, null, ip, this).show();
            }

        }
    }

    private void onAdjustPowerBtnClick(int id) {
        int progress = isbPower.getProgress();
        if (id == R.id.ib_decrease_power) {
            if (progress <= 10) return;
            progress--;
        } else {
            if (progress >= 80) return;
            progress++;
        }
        isbPower.setProgress(progress);
        savePower(progress);
    }

    private void onAdjustVolumeBtnClick(int id) {
        int progress = isbVolume.getProgress();
        if (id == R.id.ib_decrease_volume) {
            if (progress <= 0) return;
            progress--;
        } else {
            if (progress >= 15) return;
            progress++;
        }
        isbVolume.setProgress(progress);
        saveMediaVolume(progress);
    }

    private void onAdjustBrightnessBtnClick(int id) {
        int progress = isbBrightness.getProgress();
        if (id == R.id.ib_decrease_brightness) {
            if (progress <= 0) return;
            progress -= 1;
        } else {
            if (progress >= 100) return;
            progress += 1;
        }
        isbBrightness.setProgress(progress);
        saveBrightness(progress);
    }

    private void onDockChargingPileBtnClick() {
        if (robotInfo.isCharging()) {
            EasyDialog.getInstance(requireContext()).warnError(getString(R.string.text_cannot_dock_when_charging));
            return;
        }
        if (robotInfo.isEmergencyButtonDown()) {
            EasyDialog.getInstance(requireContext()).warnError(getString(R.string.text_cannot_dock_when_emergency_button_down));
            return;
        }
        ros.dockChargingPile();
    }

    public void onRelocateBtnClick() {
        presenter.refreshChargePoint(requireActivity());
    }

    public double[] getLastRelocateCoordinate() {
        return lastRelocateCoordinate;
    }

    public void setLastRelocateCoordinate(double[] lastRelocateCoordinate) {
        this.lastRelocateCoordinate = lastRelocateCoordinate;
    }

    @Override
    public void showRelocatingView() {
        EasyDialog.getLoadingInstance(requireContext()).loading(getString(R.string.text_relocating));
    }

    @Override
    public void onDeleteBroadcastItem(int type, int position, String text) {
        obstacleModeSetting.obstaclePrompts.remove(position);
        String s = obstacleModeSetting.obstaclePromptAudioList.get(position);
        new File(s).delete();
        obstacleModeSetting.obstaclePromptAudioList.remove(position);
        SpManager.getInstance().edit().putString(Constants.KEY_OBSTACLE_CONFIG, gson.toJson(obstacleModeSetting)).apply();
        obstacleAdapter.setTextContentList(obstacleModeSetting.obstaclePrompts);
    }

    @Override
    public void onAudition(int type, int position, View v) {
        if (VoiceHelper.isPlaying()) {
            VoiceHelper.pause();
            ((ImageButton) v).setImageResource(R.drawable.icon_audition);
            return;
        }
        ((ImageButton) v).setImageResource(R.drawable.icon_audition_inactive);
        String file = obstacleModeSetting.obstaclePromptAudioList.get(position);
        VoiceHelper.playFile(file, () -> ((ImageButton) v).setImageResource(R.drawable.icon_audition));
    }

    @Override
    public void onCheckChange(int type, List<Integer> list) {
        obstacleModeSetting.targetObstaclePrompts = list;
        SpManager.getInstance().edit().putString(Constants.KEY_OBSTACLE_CONFIG, gson.toJson(obstacleModeSetting)).apply();
    }

    @Override
    public void onSynthesizeStart(View btnTryListen, View btnSave) {
        btnTryListen.setEnabled(false);
        btnTryListen.setBackgroundResource(R.drawable.bg_common_button_inactive);
        btnSave.setEnabled(false);
        btnSave.setBackgroundResource(R.drawable.bg_common_button_inactive);
    }

    @Override
    public void onSynthesizeEnd(View btnTryListen, View btnSave) {
        btnTryListen.setEnabled(true);
        btnTryListen.setBackgroundResource(R.drawable.selector_common_button);
        btnSave.setEnabled(true);
        btnSave.setBackgroundResource(R.drawable.selector_common_button);
    }

    @Override
    public void onSynthesizeError(String message, View btnTryListen, View btnSave) {
        btnTryListen.setEnabled(true);
        btnTryListen.setBackgroundResource(R.drawable.selector_common_button);
        btnSave.setEnabled(false);
        btnSave.setBackgroundResource(R.drawable.bg_common_button_inactive);
    }

    @Override
    public void onExpand(ExpandableLayout expandableLayout, boolean isExpand) {
        ImageButton ibExpandIndicator = expandableLayout.getHeaderLayout().findViewById(R.id.ib_expand_indicator);
        ibExpandIndicator.animate().rotation(isExpand ? 90 : 0).setDuration(200).start();
    }

    /**
     * 获取音乐
     */
    public String getMusic() {
        if (backgroundMusicSetting.backgroundMusicFileNames != null) {
            return backgroundMusicSetting.backgroundMusicFileNames.toString().replace("[", "").replace("]", "");
        }
        return "";
    }

    @Override
    public void onBackgroundMusicSelectedList(List<String> file) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File testDir = new File(dir, "music");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        SharedPreferences.Editor edit = SpManager.getInstance().edit();
        List<String> backgroundMusicFileNames = new ArrayList<>();
        List<String> backgroundMusicPaths = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < file.size(); i++) {
            String fileName = file.get(i);
            backgroundMusicFileNames.add(fileName);
            backgroundMusicPaths.add(testDir.getAbsolutePath() + "/" + fileName);
            builder.append(fileName);
            if (i < file.size() - 1) {
                builder.append(", ");
            }

            // 将 StringBuilder 转换为字符串
            String concatenatedFileNames = builder.toString();
            btnNormalBackgroundMusicShow.setText(file == null ? getString(R.string.text_do_not_play_background_music) : (concatenatedFileNames == null ? getString(R.string.text_do_not_play_background_music) : concatenatedFileNames));
            backgroundMusicSetting.backgroundMusicFileNames = backgroundMusicFileNames;
            backgroundMusicSetting.backgroundMusicPaths = backgroundMusicPaths;
            //将获取的音乐存放在对应存储中
            edit.putString(Constants.KEY_BACKGROUND_MUSIC, gson.toJson(backgroundMusicSetting)).apply();
        }
    }
}