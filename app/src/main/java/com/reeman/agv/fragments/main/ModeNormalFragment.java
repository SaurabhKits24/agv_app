package com.reeman.agv.fragments.main;

import static com.reeman.agv.base.BaseApplication.ros;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;
import com.reeman.agv.adapter.DeliveryCheckedPointsAdapter;
import com.reeman.agv.base.BaseFragment;
import com.reeman.agv.calling.CallingInfo;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.utils.VoiceHelper;
import com.reeman.agv.widgets.EasyDialog;
import com.reeman.agv.widgets.PagedGridView;
import com.reeman.points.model.custom.GenericPoint;


import java.util.List;

import kotlin.Pair;


public class ModeNormalFragment extends BaseFragment implements  PagedGridView.OnPointCheckedListener {

    private AppCompatButton btnStart;
    private final List<GenericPoint> pointList;
    private DeliveryCheckedPointsAdapter deliveryCheckedPointsAdapter;

    private final ModeNormalClickListener listener;

    public ModeNormalFragment(List<GenericPoint> pointList, ModeNormalClickListener listener) {
        this.pointList = pointList;
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_mode_normal;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvSelectedPoints = $(R.id.rv_selected_points);
        PagedGridView pgvDeliveryPoints = $(R.id.pgv_delivery_points);
        $(R.id.btn_lift_up).setOnClickListener(this);
        $(R.id.btn_lift_down).setOnClickListener(this);
        btnStart = $(R.id.btn_start);
        btnStart.setOnClickListener(this);
        pgvDeliveryPoints.initData(null,pointList,robotInfo.isPointScrollShow(),this);
        deliveryCheckedPointsAdapter = new DeliveryCheckedPointsAdapter();
        rvSelectedPoints.setAdapter(deliveryCheckedPointsAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        rvSelectedPoints.setLayoutManager(layoutManager);
        $(R.id.layout_manual_lift_control).setVisibility(robotInfo.isNormalModeWithManualLiftControl() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (robotInfo.isNormalModeWithManualLiftControl()){
            ros.ioControl(0x04);
        }
        btnStart.postDelayed(startButtonClickableRunnable, 200);
        if (deliveryCheckedPointsAdapter != null) {
            deliveryCheckedPointsAdapter.clear();
        }
    }

    private final Runnable startButtonClickableRunnable = new Runnable() {
        @Override
        public void run() {
            btnStart.setClickable(true);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        btnStart.removeCallbacks(startButtonClickableRunnable);
        btnStart.setClickable(false);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        switch (id) {
            case R.id.btn_start:
                onStartClick();
                break;
            case R.id.btn_lift_up:
            case R.id.btn_lift_down:
                if (robotInfo.isEmergencyButtonDown()) {
                    EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.voice_scram_stop_turn_on));
                    return;
                }
                if (robotInfo.isCharging()) {
                    EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.voice_charging_and_can_not_move));
                    return;
                }
                if (!robotInfo.isLifting()) {
                    if (id == R.id.btn_lift_up && robotInfo.getLiftModelState() == 1) {
                        ToastUtils.showShortToast(getString(R.string.text_lift_model_already_up));
                        return;
                    }
                    if (id == R.id.btn_lift_down && robotInfo.getLiftModelState() == 0) {
                        ToastUtils.showShortToast(getString(R.string.text_lift_model_already_down));
                        return;
                    }
                }
                EasyDialog.getInstance(requireActivity()).confirm(getString(id == R.id.btn_lift_up ? R.string.text_click_confirm_to_lift_up : R.string.text_click_confirm_to_lift_down), (dialog, id1) -> {
                    dialog.dismiss();
                    if (id1 == R.id.btn_confirm) {
                        if (id == R.id.btn_lift_up) {
                            ros.liftUp();
                            CallingInfo.INSTANCE.getHeartBeatInfo().setLifting(true);
                        } else {
                            ros.liftDown();
                        }
                        CallingInfo.INSTANCE.setLifting(true);
                        EasyDialog.getLoadingInstance(requireActivity()).loading(getString(id == R.id.btn_lift_up ? R.string.text_pickup_model_lifting_up : R.string.text_pickup_model_lifting_down));
                    }
                });
                break;
        }
    }

    public void onStartClick() {
        List<Pair<String,String>> checkedPoints = deliveryCheckedPointsAdapter.getDeliveryPointList();
        if (checkedPoints.size() < 1) {
            VoiceHelper.play("voice_please_select_point_first");
            EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.voice_please_select_point_first));
            return;
        }
        listener.onStart(checkedPoints);
    }

    @Override
    public void onPointChecked(@NonNull GenericPoint checkedPoint) {
        deliveryCheckedPointsAdapter.addDeliveryPointList(new Pair<>("",checkedPoint.getName()));
    }

    @Override
    public void onPointWithMapChecked(@NonNull String map, @NonNull GenericPoint checkedPoint) {

    }

    public interface ModeNormalClickListener {
        void onStart(List<Pair<String,String>> points);
    }
}