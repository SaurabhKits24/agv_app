package com.reeman.agv.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.reeman.agv.R;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.utils.SpManager;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

public class VolumeAdjustDialog extends BaseDialog {

    public VolumeAdjustDialog(Context context) {
        super(context);
        View root = LayoutInflater.from(context).inflate(R.layout.layout_dialog_volume_adjustment, null);
        IndicatorSeekBar indicatorSeekBar = root.findViewById(R.id.isb_adjust_volume);
        int volume = SpManager.getInstance().getInt(Constants.KEY_MEDIA_VOLUME, Constants.DEFAULT_MEDIA_VOLUME);
        indicatorSeekBar.setProgress(volume);
        indicatorSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                int progress = seekBar.getProgress();
                SpManager.getInstance().edit().putInt(Constants.KEY_MEDIA_VOLUME, progress).apply();
            }
        });
        setContentView(root);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }


}
