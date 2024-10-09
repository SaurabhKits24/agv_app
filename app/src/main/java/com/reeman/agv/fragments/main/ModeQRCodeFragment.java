package com.reeman.agv.fragments.main;

import static com.reeman.agv.base.BaseApplication.ros;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;
import com.reeman.agv.activities.QRCodeCallingBoundActivity;
import com.reeman.agv.activities.QRCodeCallingConfigActivity;
import com.reeman.agv.adapter.DeliveryPointsAdapter;
import com.reeman.agv.adapter.QRCodeCheckedPointsAdapter;
import com.reeman.agv.base.BaseFragment;
import com.reeman.agv.calling.CallingInfo;
import com.reeman.agv.calling.button.CallingHelper;
import com.reeman.agv.widgets.PagedGridView;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.settings.ModeQRCodeSetting;
import com.reeman.agv.fragments.main.listener.ModeQRCodeClickListener;
import com.reeman.agv.widgets.EasyDialog;
import com.reeman.points.model.custom.GenericPoint;

import java.util.LinkedList;
import java.util.List;

import kotlin.Pair;


public class ModeQRCodeFragment extends BaseFragment implements QRCodeCheckedPointsAdapter.OnQRCodeViewClickListener, PagedGridView.OnPointCheckedListener {

    private AppCompatButton btnStart;
    private final List<GenericPoint> pointList;
    private QRCodeCheckedPointsAdapter qrCodeCheckedPointsAdapter;

    private AppCompatImageButton btCallingBind;
    private AppCompatImageButton btCallingBound;
    private ModeQRCodeSetting qrCodeSetting;
    private final ModeQRCodeClickListener listener;

    public ModeQRCodeFragment(List<GenericPoint> pointList, ModeQRCodeClickListener listener) {
        this.pointList = pointList;
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_mode_qrcode;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvQRCodePoints = $(R.id.rv_qrcode_points);
        PagedGridView pgvQRCodePoints = $(R.id.pgv_qrcode_points);
        btnStart = $(R.id.btn_start);
        btnStart.setOnClickListener(this);
        btCallingBind = $(R.id.bt_calling_bind);
        btCallingBound = $(R.id.bt_calling_bound);
        btCallingBind.setOnClickListener(this);
        btCallingBound.setOnClickListener(this);
        pgvQRCodePoints.initData(null, pointList, robotInfo.isPointScrollShow(), this);
        qrCodeCheckedPointsAdapter = new QRCodeCheckedPointsAdapter(this);
        rvQRCodePoints.setAdapter(qrCodeCheckedPointsAdapter);
        rvQRCodePoints.setLayoutManager(new LinearLayoutManager(requireActivity()));
        if (robotInfo.isSpaceShip() && robotInfo.isLiftModelInstalled()) {
            mHandler.postDelayed(() -> ros.getAltitudeState(), 200);
        }

        qrCodeSetting = robotInfo.getModeQRCodeSetting();
        if (qrCodeSetting.callingBind) {
            btCallingBind.setVisibility(View.VISIBLE);
            btCallingBound.setVisibility(View.VISIBLE);
        } else {
            btCallingBind.setVisibility(View.GONE);
            btCallingBound.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        btnStart.postDelayed(startButtonClickableRunnable, 200);
        if (qrCodeCheckedPointsAdapter != null) {
            qrCodeCheckedPointsAdapter.clear();
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
    }

    @Override
    protected void onCustomClickResult(int id) {
        if (id == R.id.btn_start) {
            int size = qrCodeCheckedPointsAdapter.getItemCount();
            if (size == 0) {
                EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.voice_please_choose_qrcode_point));
                return;
            }
            List<Pair<Pair<String, String>, Pair<String, String>>> qrCodeModelList = qrCodeCheckedPointsAdapter.getPairList();
            if (qrCodeModelList.get(size - 1).getSecond() == null) {
                EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.voice_target_location_is_empty));
                return;
            }
            Log.d("ModeQRCodeFragment", "onCustomClickResult: " + qrCodeModelList);
            listener.onStart(qrCodeModelList);
        }

        if (id == R.id.bt_calling_bind) {
            int size = qrCodeCheckedPointsAdapter.getItemCount();
            if (size == 0) {
                EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.voice_please_choose_qrcode_point));
                return;
            }
            List<Pair<Pair<String, String>, Pair<String, String>>> list = qrCodeCheckedPointsAdapter.getPairList();
            if (list.get(size - 1).getSecond() == null) {
                EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.voice_target_location_is_empty));
                return;
            }
            if (CallingHelper.INSTANCE.isStart())
                CallingHelper.INSTANCE.stop();
            if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
            EasyDialog.getLoadingInstance(requireActivity()).loading(getString(R.string.text_enter_calling_config));
            mHandler.postDelayed(() -> {
                if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
                Intent intent = new Intent(requireActivity(), QRCodeCallingConfigActivity.class);
                intent.putExtra(Constants.TASK_TARGET, new LinkedList<>(list));
                requireActivity().startActivity(intent);
            }, 2000);

        }
        if (id == R.id.bt_calling_bound) {
            if (CallingInfo.INSTANCE.getCallingButtonWithQRCodeModelTaskMap().isEmpty()) {
                EasyDialog.getInstance(requireActivity())
                        .warnError(getString(R.string.voice_bind_location_is_empty));
                return;
            }
            //清空
            qrCodeCheckedPointsAdapter.clear();
            Intent intent = new Intent(requireActivity(), QRCodeCallingBoundActivity.class);
            requireActivity().startActivity(intent);
        }
    }

    @Override
    public void onPointSameWarn() {
        EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.text_start_location_cannot_equals_target_location));
    }

    @Override
    public void onDeleteQRCode(int position) {
        qrCodeCheckedPointsAdapter.removeQRCodeModeItem(position);
    }

    @Override
    public void onPointChecked(@NonNull GenericPoint checkedPoint) {
        qrCodeCheckedPointsAdapter.addItem(new Pair<>("", checkedPoint.getName()));
    }

    @Override
    public void onPointWithMapChecked(@NonNull String map, @NonNull GenericPoint checkedPoint) {

    }
}
