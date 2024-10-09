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
import com.reeman.agv.adapter.DeliveryPointsAdapter;
import com.reeman.agv.adapter.MapGroupAdapter;
import com.reeman.agv.adapter.QRCodeCheckedPointsAdapter;
import com.reeman.agv.base.BaseFragment;
import com.reeman.agv.fragments.main.listener.ModeQRCodeClickListener;
import com.reeman.agv.widgets.EasyDialog;
import com.reeman.agv.widgets.PagedGridView;
import com.reeman.points.model.custom.GenericPoint;
import com.reeman.points.model.custom.GenericPointsWithMap;

import java.util.List;

import kotlin.Pair;


public class ModeQRCodeWithMapFragment extends BaseFragment implements QRCodeCheckedPointsAdapter.OnQRCodeViewClickListener, MapGroupAdapter.OnMapGroupItemClickListener, PagedGridView.OnPointCheckedListener {

    private AppCompatButton btnStart;
    private PagedGridView pgvQRCodePoints;
    private final List<GenericPointsWithMap> pointsWithMapList;
    private int pointsWithMapListIndex = 0;
    private QRCodeCheckedPointsAdapter qrCodeCheckedPointsAdapter;

    private MapGroupAdapter mapGroupAdapter;

    private final ModeQRCodeClickListener listener;

    public ModeQRCodeWithMapFragment(List<GenericPointsWithMap> pointsWithMapList, ModeQRCodeClickListener listener) {
        this.pointsWithMapList = pointsWithMapList;
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_mode_qrcode_with_map;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvQRCodePoints = $(R.id.rv_qrcode_points);
        RecyclerView rvMapGroup = $(R.id.rv_table_group);
        pgvQRCodePoints = $(R.id.pgv_qrcode_points);

        btnStart = $(R.id.btn_start);
        btnStart.setOnClickListener(this);
        mapGroupAdapter = new MapGroupAdapter(pointsWithMapList, this);
        rvMapGroup.setAdapter(mapGroupAdapter);
        rvMapGroup.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return true;
            }
        });
        pgvQRCodePoints.initData(mapGroupAdapter.getSelectedMap(),pointsWithMapList.get(pointsWithMapListIndex).getPointList(),robotInfo.isPointScrollShow(),this);
        qrCodeCheckedPointsAdapter = new QRCodeCheckedPointsAdapter(this);
        rvQRCodePoints.setAdapter(qrCodeCheckedPointsAdapter);
        rvQRCodePoints.setLayoutManager(new LinearLayoutManager(requireActivity()));
        if (robotInfo.isSpaceShip() && robotInfo.isLiftModelInstalled()) {
            mHandler.postDelayed(() -> ros.getAltitudeState(), 200);
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
            List<Pair<Pair<String, String>, Pair<String, String>>> pointPairList = qrCodeCheckedPointsAdapter.getPairList();
            if (pointPairList.get(size - 1).getSecond() == null) {
                EasyDialog.getInstance(requireActivity()).warnError(getString(R.string.voice_target_location_is_empty));
                return;
            }
            listener.onStart(pointPairList);
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
    public void onMapGroupItemClick(int index, String alias, List<GenericPoint> pointList) {
        pointsWithMapListIndex = index;
        pgvQRCodePoints.setData(alias,pointList);
    }

    @Override
    public void onPointChecked(@NonNull GenericPoint checkedPoint) {

    }

    @Override
    public void onPointWithMapChecked(@NonNull String map, @NonNull GenericPoint checkedPoint) {
        qrCodeCheckedPointsAdapter.addItem(new Pair<>(map,checkedPoint.getName()));
    }
}
