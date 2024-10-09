package com.reeman.agv.widgets;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reeman.agv.R;
import com.reeman.agv.adapter.PointsAdapter;

import java.util.List;

import kotlin.Pair;

public class CallingTaskChoosePointDialog extends BaseDialog {

    private PointsAdapter pointsAdapter;

    public CallingTaskChoosePointDialog(@NonNull Context context, List<String> allPoints, Pair<String, String> selectedPoint, OnPointChooseResultListener listener) {
        super(context);
        View root = LayoutInflater.from(context).inflate(R.layout.layout_dialog_choose_point, null);
        GridView gvPalletPoints = root.findViewById(R.id.gv_points);
        pointsAdapter = new PointsAdapter(allPoints, selectedPoint);
        gvPalletPoints.setNumColumns(5);
        gvPalletPoints.setHorizontalSpacing(10);
        gvPalletPoints.setVerticalSpacing(5);
        gvPalletPoints.setAdapter(pointsAdapter);
        root.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            dismiss();
            listener.onPointChooseResult(pointsAdapter.getSelectedPoints());
        });
        setContentView(root);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        params.width = (int) (screenWidth * 0.8);
        params.height = (int) (screenHeight * 0.8);
        window.setAttributes(params);
    }

    @Override
    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        super.setOnDismissListener(listener);
        if (pointsAdapter !=null){
            pointsAdapter = null;
        }
    }

    public interface OnPointChooseResultListener {
        void onPointChooseResult(Pair<String, String> points);
    }
}

