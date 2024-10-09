package com.reeman.agv.fragments.task;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.reeman.agv.R;
import com.reeman.agv.base.BaseFragment;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.utils.StringUtils;
import com.reeman.agv.viewModel.TaskPauseInfoModel;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class PauseFragment extends BaseFragment {

    private TextView tvCountDownTime;

    private TextView tvTip;

    private final OnPauseClickListener listener;

    public PauseFragment(OnPauseClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_pause;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        String taskPauseInfoStr = bundle.getString(Constants.KEY_TASK_PAUSE_INFO, "");
        TaskPauseInfoModel taskPauseInfoModel = new Gson().fromJson(taskPauseInfoStr, TaskPauseInfoModel.class);
        initView(taskPauseInfoModel);
    }

    private void initView(TaskPauseInfoModel taskPauseInfoModel) {
        TextView tvTaskMode = $(R.id.tv_task_mode);
        TextView tvRouteName = $(R.id.tv_route_name);
        TextView tvGoto = $(R.id.tv_go_to);
        TextView tvNowFloor = $(R.id.tv_now_floor);
        TextView tvTaskStartTime = $(R.id.tv_task_start_time);
        tvTip = $(R.id.tv_tip);
        TextView tvNextPoint = $(R.id.tv_next_point);
        tvCountDownTime = $(R.id.tv_resume_countdown);
        TextView tvReturn = $(R.id.tv_return_to_product_point);
        TextView tvSkipCurrentTarget = $(R.id.tv_skip_current_target);
        TextView tvContinueTask = $(R.id.tv_continue_task);
        TextView tvCancel = $(R.id.tv_cancel);
        TextView tvRecallElevator = $(R.id.tv_call_elevator);


        if (TextUtils.isEmpty(taskPauseInfoModel.getTaskMode())) {
            tvTaskMode.setVisibility(View.GONE);
        } else {
            tvTaskMode.setText(taskPauseInfoModel.getTaskMode());
        }
        tvTaskStartTime.setText(getString(R.string.text_task_start_time, taskPauseInfoModel.getTaskStartTime()));
        tvTip.setText(taskPauseInfoModel.getTaskPauseTip());

        if (TextUtils.isEmpty(taskPauseInfoModel.getRouteName())) {
            tvRouteName.setVisibility(View.GONE);
        } else {
            tvRouteName.setText(getString(R.string.text_route_name, taskPauseInfoModel.getRouteName()));
        }
        if (TextUtils.isEmpty(taskPauseInfoModel.getTargetPoint())) {
            tvGoto.setVisibility(View.GONE);
            tvNextPoint.setVisibility(View.GONE);
        } else {
            if (!TextUtils.isEmpty(taskPauseInfoModel.getTargetFloor())) {
                tvGoto.setText(getString(R.string.text_wait_for_go_to_next_point, getString(R.string.text_point_with_floor, taskPauseInfoModel.getTargetFloor(), taskPauseInfoModel.getTargetPoint())));
                tvNextPoint.setText(getString(R.string.text_wait_for_go_to_next_point, getString(R.string.text_point_with_floor, taskPauseInfoModel.getTargetFloor(), taskPauseInfoModel.getTargetPoint())));
            } else {
                tvGoto.setText(getString(R.string.text_wait_for_go_to_next_point, taskPauseInfoModel.getTargetPoint()));
                tvNextPoint.setText(getString(R.string.text_wait_for_go_to_next_point, taskPauseInfoModel.getTargetPoint()));
            }
        }
        if (TextUtils.isEmpty(taskPauseInfoModel.getCurrentFloor())) {
            tvNowFloor.setVisibility(View.GONE);
        } else {
            tvNowFloor.setText(getString(R.string.text_now_floor, taskPauseInfoModel.getCurrentFloor()));
        }
        updateCountDownTimer(taskPauseInfoModel.getCountDownTime());
        tvSkipCurrentTarget.setVisibility(taskPauseInfoModel.getShowSkipCurrentTargetButton()?View.VISIBLE:View.GONE);
        tvSkipCurrentTarget.setOnClickListener(this);
        tvRecallElevator.setVisibility(taskPauseInfoModel.getShowRecallElevatorButton() ? View.VISIBLE : View.GONE);
        tvRecallElevator.setOnClickListener(this);
        tvReturn.setVisibility(taskPauseInfoModel.getShowReturnButton() ? View.VISIBLE : View.GONE);
        tvReturn.setOnClickListener(this);
        Timber.w("继续任务 : %s", taskPauseInfoModel.getShowContinueTaskButton());
        tvContinueTask.setVisibility(taskPauseInfoModel.getShowContinueTaskButton() ? View.VISIBLE : View.GONE);
        tvContinueTask.setOnClickListener(this);
        tvCancel.setVisibility(taskPauseInfoModel.getShowCancelTaskButton() ? View.VISIBLE : View.GONE);
        tvCancel.setOnClickListener(this);
    }

    public void updateTip(String tip) {
        tvTip.setText(tip);
    }

    public void updateCountDownTimer(long seconds) {
        if (tvCountDownTime.getVisibility() != View.GONE && seconds < 1) {
            tvCountDownTime.setVisibility(View.GONE);
            return;
        }
        if (tvCountDownTime.getVisibility() != View.VISIBLE && seconds > 0) {
            tvCountDownTime.setVisibility(View.VISIBLE);
        }
        long millisSeconds = seconds * 1000;
        int hour = (int) TimeUnit.MILLISECONDS.toHours(millisSeconds);
        int minute = (int) TimeUnit.MILLISECONDS.toMinutes(millisSeconds) % 60;
        int second = (int) TimeUnit.MILLISECONDS.toSeconds(millisSeconds) % 60;
        SpannableStringBuilder str = new SpannableStringBuilder();
        if (hour > 0) {
            SpannableString hourStr = new SpannableString(getString(R.string.text_hours_later, hour));
            StringUtils.updateNumber(hourStr, hourStr.toString().split("\\d")[0].length(), String.valueOf(hour).length());
            str.append(hourStr);
        }
        if (minute > 0) {
            SpannableString minuteStr = new SpannableString(getString(R.string.text_minutes_later, minute));
            StringUtils.updateNumber(minuteStr, minuteStr.toString().split("\\d")[0].length(), String.valueOf(minute).length());
            str.append(minuteStr);
        }
        SpannableString secondStr = new SpannableString(getString(R.string.text_task_continued_in_minutes, second));
        StringUtils.updateNumber(secondStr, secondStr.toString().split("\\d")[0].length(), String.valueOf(second).length());
        str.append(secondStr);
        tvCountDownTime.setText(str);
    }

    @Override
    protected void onCustomClickResult(int id) {
        switch (id) {
            case R.id.tv_call_elevator:
                listener.onCallElevatorBtnClick();
                break;
            case R.id.tv_return_to_product_point:
                listener.onReturnBtnClick();
                break;
            case R.id.tv_continue_task:
                listener.onContinueBtnClick();
                break;
            case R.id.tv_cancel:
                listener.onCancelBtnClick();
                break;
            case R.id.tv_skip_current_target:
                listener.onSkipCurrentTargetBtnClick();
                break;
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
    }

    public interface OnPauseClickListener {

        void onCallElevatorBtnClick();

        void onReturnBtnClick();

        void onContinueBtnClick();

        void onCancelBtnClick();

        void onSkipCurrentTargetBtnClick();
    }
}
