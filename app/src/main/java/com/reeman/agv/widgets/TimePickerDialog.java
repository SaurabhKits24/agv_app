package com.reeman.agv.widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.reeman.agv.R;

public class TimePickerDialog extends BaseDialog{


    public TimePickerDialog(@NonNull Context context, String time,OnTimePickerConfirmListener listener) {
        super(context);
        View root = LayoutInflater.from(context).inflate(R.layout.layout_time_picker_dialog, null);
        setCanceledOnTouchOutside(true);
        Button btnConfirm = root.findViewById(R.id.btn_confirm);
        TimePickerView timePickerView = root.findViewById(R.id.time_picker_view);
        timePickerView.setTime(time);
        timePickerView.setWeights(1,7);
        timePickerView.setVisibility(View.VISIBLE,View.VISIBLE,View.GONE,View.GONE);
        btnConfirm.setOnClickListener(view -> listener.onConfirm(this,timePickerView.getTime()));
        setContentView(root);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }

    public TimePickerDialog(@NonNull Context context, int waitingTime,OnTimePickerConfirmListener listener) {
        super(context);
        View root = LayoutInflater.from(context).inflate(R.layout.layout_time_picker_dialog, null);
        setCanceledOnTouchOutside(true);
        Button btnConfirm = root.findViewById(R.id.btn_confirm);
        TimePickerView timePickerView = root.findViewById(R.id.time_picker_view);
        timePickerView.setTotalTime(waitingTime);
        timePickerView.setWeights(1,7);
        timePickerView.setMinSecond(10);
        btnConfirm.setOnClickListener(view -> listener.onConfirm(this,timePickerView.getTotalTime()));
        setContentView(root);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }

    public TimePickerDialog(@NonNull Context context, int waitingTime,int minTime,OnTimePickerConfirmListener listener) {
        super(context);
        View root = LayoutInflater.from(context).inflate(R.layout.layout_time_picker_dialog, null);
        setCanceledOnTouchOutside(true);
        Button btnConfirm = root.findViewById(R.id.btn_confirm);
        TimePickerView timePickerView = root.findViewById(R.id.time_picker_view);
        timePickerView.setTotalTime(waitingTime);
        timePickerView.setWeights(1,7);
        timePickerView.setMinSecond(minTime);
        btnConfirm.setOnClickListener(view -> listener.onConfirm(this,timePickerView.getTotalTime()));
        setContentView(root);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }


    public interface OnTimePickerConfirmListener{
        default void onConfirm(Dialog dialog, int waitingTime){

        }

        default void onConfirm(Dialog dialog,String time){

        }
    }


}
