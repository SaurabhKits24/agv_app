package com.reeman.agv.fragments.task;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.reeman.agv.R;
import com.reeman.agv.base.BaseFragment;
import com.reeman.commons.constants.Constants;
import com.reeman.agv.elevator.state.Step;
import com.reeman.agv.viewModel.TaskRunningInfoModel;

public class RunningFragment extends BaseFragment {

    private RelativeLayout layoutRunning;

    private TextView tvElevatorTip;

    private TextView tvLiftModelState;

    private TextView tvQRCodeNavigationState;

    private TextView tvTargetPoint;

    private LinearLayout layoutQRCodeMode;

    private final OnRunningClickListener listener;

    public RunningFragment(OnRunningClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_running;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        String taskRunningInfoStr = bundle.getString(Constants.KEY_TASK_RUNNING_INFO, "");
        TaskRunningInfoModel taskRunningInfoModel = new Gson().fromJson(taskRunningInfoStr, TaskRunningInfoModel.class);
        initView(taskRunningInfoModel);

    }

    private void initView(TaskRunningInfoModel taskRunningInfoModel) {
        layoutRunning = $(R.id.layout_running);
        tvElevatorTip = $(R.id.tv_elevator_tip);
        layoutQRCodeMode = $(R.id.layout_qrcode_mode);
        tvLiftModelState = $(R.id.tv_lift_model_state);
        tvQRCodeNavigationState = $(R.id.tv_qrcode_navigation_state);
        tvTargetPoint = $(R.id.tv_target_point);
        layoutRunning.setOnClickListener(v -> listener.onClick());
        updateElevatorTip(taskRunningInfoModel.getElevatorStep(), taskRunningInfoModel.getTargetFloor());

        updateLiftModeStateAndQRCodeNavigationState(taskRunningInfoModel.getLiftModelState(),taskRunningInfoModel.getQrCodeNavigationState());

        updateTargetPoint(taskRunningInfoModel.getTargetFloor(), taskRunningInfoModel.getTargetPoint());
    }

    public void updateTargetPoint(String targetFloor,String targetPoint){
        if (TextUtils.isEmpty(targetFloor)) {
            tvTargetPoint.setText(targetPoint);
        } else {
            tvTargetPoint.setText(getString(R.string.text_floor_with_point, targetFloor, targetPoint));
        }
    }

    public void updateElevatorTip(Step step, String targetFloor) {
        if (step == Step.IDLE) {
            tvElevatorTip.setVisibility(View.GONE);
        } else if (step == Step.REQUEST_ID) {
            tvElevatorTip.setVisibility(View.VISIBLE);
            tvElevatorTip.setText(getString(R.string.text_call_elevator));
        } else if (step == Step.CHECK_PATH_TO_ELEVATOR) {
            tvElevatorTip.setText(getString(R.string.text_checking_path_to_elevator));
        } else if (step == Step.ENTER_ELEVATOR_ACK) {
            tvElevatorTip.setText(getString(R.string.text_elevator_arrive_current_floor));
        } else if (step == Step.ENTER_ELEVATOR_COMPLETE) {
            tvElevatorTip.setText(getString(R.string.text_enter_elevator_go_to_target_floor, targetFloor));
        } else if (step == Step.LEAVE_ELEVATOR_ACK) {
            tvElevatorTip.setText(getString(R.string.text_elevator_arrive_target_floor, targetFloor));
        } else if (step == Step.LEAVE_ELEVATOR_COMPLETE) {
            tvElevatorTip.setText(getString(R.string.voice_leave_elevator));
        } else if (step == Step.CALL_ELEVATOR) {
            tvElevatorTip.setText(getString(R.string.text_call_elevator_success));
        } else if (step == Step.CALL_ELEVATOR_INSIDE) {
            tvElevatorTip.setText(getString(R.string.text_call_elevator_inside_success));
        }else if (step == Step.APPLY_MAP){
            tvElevatorTip.setText(getString(R.string.text_switch_to_target_map, targetFloor));
        }else if (step == Step.INIT_POSE){
            tvElevatorTip.setText(getString(R.string.text_switch_map_success_init_pose));
        }
    }

    public void updateRunningTip(String tip){

        tvElevatorTip.setVisibility(View.VISIBLE);
        tvElevatorTip.setText(tip);
    }

    public void dismissRunningTip(){
        tvElevatorTip.setVisibility(View.GONE);
    }

    private void updateLiftModeStateAndQRCodeNavigationState(String liftModeState,String qrCodeNavigationState){
        boolean isLiftModelStateEmpty = TextUtils.isEmpty(liftModeState);
        boolean isQRCodeNavigationStateEmpty = TextUtils.isEmpty(qrCodeNavigationState);
        if (isLiftModelStateEmpty && isQRCodeNavigationStateEmpty) {
            layoutQRCodeMode.setVisibility(View.GONE);
        } else {
            if (isLiftModelStateEmpty) {
                tvLiftModelState.setVisibility(View.GONE);
            } else {
                tvLiftModelState.setText(liftModeState);
            }
            if (isQRCodeNavigationStateEmpty) {
                tvQRCodeNavigationState.setVisibility(View.GONE);
            } else {
                tvQRCodeNavigationState.setText(qrCodeNavigationState);
            }
        }
    }

    public interface OnRunningClickListener{
        void onClick();
    }
}
