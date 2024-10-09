package com.reeman.agv.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.reeman.agv.R;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.concurrent.TimeUnit;

public class TimePickerView extends LinearLayout implements View.OnClickListener {

    private LinearLayout[] layoutTime = new LinearLayout[3];
    private IndicatorSeekBar hoursSeekBar;
    private IndicatorSeekBar minutesSeekBar;
    private IndicatorSeekBar secondsSeekBar;
    private TextView totalTimeTextView;
    private int totalTime = 30;
    private Context context;
    private int hour, minute, second = 30;
    private int minSecond = 0;

    public TimePickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.layout_time_setting_view, this);
        // 初始化视图和控件
        initViews();
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setWeights(int textWeights, int seekbarWeights) {
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, textWeights);
        LinearLayout.LayoutParams seekbarLayoutParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, seekbarWeights);
        for (int i = 0; i < layoutTime.length; i++) {
            LinearLayout layout = layoutTime[i];
            if (layout.getChildCount() == 2) {
                View childAtTextView = layout.getChildAt(0);
                View childAtSeekbar = layout.getChildAt(1);
                if (childAtTextView instanceof TextView && childAtSeekbar instanceof IndicatorSeekBar) {
                    childAtTextView.setLayoutParams(textLayoutParams);
                    childAtSeekbar.setLayoutParams(seekbarLayoutParams);
                }
            }
        }

    }

    public void setVisibility(int hoursVisibility, int minutesVisibility, int secondsVisibility, int totalTimeVisibility) {
        layoutTime[0].setVisibility(hoursVisibility);
        layoutTime[1].setVisibility(minutesVisibility);
        layoutTime[2].setVisibility(secondsVisibility);
        totalTimeTextView.setVisibility(totalTimeVisibility);
    }

    public void setMinSecond(int minSecond) {
        this.minSecond = minSecond;
        if (secondsSeekBar != null) {
            secondsSeekBar.setMin(minSecond);
        }
    }

    public void setTime(String time) {
        String[] split = time.split(":");
        hour = Integer.parseInt(split[0]);
        minute = Integer.parseInt(split[1]);
        hoursSeekBar.setProgress(hour);
        minutesSeekBar.setProgress(minute);
    }

    public String getTime() {
        String hourStr = hour < 10 ? "0" + hour : hour + "";
        String minuteStr = minute < 10 ? "0" + minute : minute + "";
        return hourStr+":"+minuteStr;
    }

    public void setTotalTime(int seconds) {
        int millisSeconds = seconds * 1000;
        hour = (int) TimeUnit.MILLISECONDS.toHours(millisSeconds);
        minute = (int) TimeUnit.MILLISECONDS.toMinutes(millisSeconds) % 60;
        second = (int) TimeUnit.MILLISECONDS.toSeconds(millisSeconds) % 60;
        this.totalTime = seconds;
        hoursSeekBar.setProgress(hour);
        minutesSeekBar.setProgress(minute);
        secondsSeekBar.setProgress(second);
        totalTimeTextView.setText(context.getString(R.string.text_total_time, totalTime));
    }

    private void initViews() {
        // 获取视图中的控件
        layoutTime[0] = findViewById(R.id.layout_hours);
        layoutTime[1] = findViewById(R.id.layout_minutes);
        layoutTime[2] = findViewById(R.id.layout_seconds);
        hoursSeekBar = findViewById(R.id.sb_hours);
        minutesSeekBar = findViewById(R.id.sb_minutes);
        secondsSeekBar = findViewById(R.id.sb_seconds);
        ImageButton ibHoursReduce = findViewById(R.id.ib_hours_reduce);
        ImageButton ibHoursAdd = findViewById(R.id.ib_hours_add);
        ImageButton ibMinutesReduce = findViewById(R.id.ib_minutes_reduce);
        ImageButton ibMinutesAdd = findViewById(R.id.ib_minutes_add);
        ImageButton ibSecondsReduce = findViewById(R.id.ib_seconds_reduce);
        ImageButton ibSecondsAdd = findViewById(R.id.ib_seconds_add);
        ibHoursReduce.setOnClickListener(this);
        ibHoursAdd.setOnClickListener(this);
        ibMinutesReduce.setOnClickListener(this);
        ibMinutesAdd.setOnClickListener(this);
        ibSecondsReduce.setOnClickListener(this);
        ibSecondsAdd.setOnClickListener(this);
        totalTimeTextView = findViewById(R.id.et_total_time);
        totalTimeTextView.setText(context.getString(R.string.text_total_time, totalTime));
        hoursSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                hour = seekBar.getProgress();
                updateTotalTime();
            }
        });
        minutesSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                minute = seekBar.getProgress();
                updateTotalTime();
            }
        });
        secondsSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                second = seekBar.getProgress();
                updateTotalTime();
            }
        });
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ib_hours_reduce || id == R.id.ib_hours_add) {
            onAdjustHoursBtnClick(id);
        } else if (id == R.id.ib_minutes_reduce || id == R.id.ib_minutes_add) {
            onAdjustMinutesBtnClick(id);
        } else if (id == R.id.ib_seconds_reduce || id == R.id.ib_seconds_add) {
            onAdjustSecondsBtnClick(id);
        }
    }

    private void onAdjustHoursBtnClick(int id) {
        int progress = hoursSeekBar.getProgress();
        if (id == R.id.ib_hours_reduce) {
            if (progress <= 0) return;
            progress -= 1;
        } else {
            if (progress >= 23) return;
            progress += 1;
        }
        hoursSeekBar.setProgress(progress);
        hour = progress;
        updateTotalTime();

    }

    private void onAdjustMinutesBtnClick(int id) {
        int progress = minutesSeekBar.getProgress();
        if (id == R.id.ib_minutes_reduce) {
            if (progress <= 0) return;
            progress -= 1;
        } else {
            if (progress >= 59) return;
            progress += 1;
        }
        minutesSeekBar.setProgress(progress);
        minute = progress;
        updateTotalTime();
    }

    private void onAdjustSecondsBtnClick(int id) {
        int progress = secondsSeekBar.getProgress();
        if (id == R.id.ib_seconds_reduce) {
            if (progress <= 0) return;
            progress -= 1;
        } else {
            if (progress >= 59) return;
            progress += 1;
        }
        secondsSeekBar.setProgress(progress);
        second = progress;
        updateTotalTime();
    }

    private void updateTotalTime() {
        totalTime = hour * 3600 + minute * 60 + second;
        if (totalTime < minSecond) {
            totalTime = minSecond;
            secondsSeekBar.setProgress(minSecond);
        }
        totalTimeTextView.setText(context.getString(R.string.text_total_time, totalTime));
    }
}
