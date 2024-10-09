package com.reeman.agv.widgets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;
import com.reeman.agv.adapter.RemoteTaskItemAdapter;
import com.reeman.agv.calling.model.RemoteTaskModel;

import java.util.List;

public class RemoteTaskListDialog extends BaseDialog {

    private static RemoteTaskListDialog remoteTaskListDialog;

    public static boolean isShow() {
        return remoteTaskListDialog != null && remoteTaskListDialog.isShowing();
    }

    public static RemoteTaskListDialog getInstance() {
        return remoteTaskListDialog;
    }

    public static RemoteTaskListDialog getInstance(Context context, List<RemoteTaskModel> remoteTaskModelList, OnClickListener listener) {
        if (remoteTaskListDialog == null) {
            synchronized (RemoteTaskListDialog.class) {
                if (remoteTaskListDialog == null) {
                    remoteTaskListDialog = new RemoteTaskListDialog(context, remoteTaskModelList, listener);
                }
            }
        }
        return remoteTaskListDialog;
    }

    private RemoteTaskListDialog(Context context, List<RemoteTaskModel> remoteTaskModelList, OnClickListener listener) {
        super(context);
        View floatingView =  LayoutInflater.from(context).inflate(R.layout.layout_calling_points_dialog, null);
        RecyclerView rvCallingPoints = floatingView.findViewById(R.id.rv_point_list);
        Button btnClear = floatingView.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(v -> {
            dismiss();
            listener.onClear();
        });
        rvCallingPoints.setAdapter(new RemoteTaskItemAdapter(context,remoteTaskModelList));
        rvCallingPoints.setLayoutManager(new LinearLayoutManager(context));

        setCancelable(true);
        setContentView(floatingView);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        params.width = (int) (screenWidth * 0.8);
        params.height = (int) (screenHeight * 0.8);
        window.setAttributes(params);
        setOnDismissListener(dialog -> {
            if (listener != null) listener.onDismiss();
            remoteTaskListDialog = null;
        });
    }

    public interface OnClickListener {
        void onClear();

        void onDismiss();
    }
}


