package com.reeman.agv.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.reeman.agv.R;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.utils.LocaleUtil;
import com.reeman.commons.utils.SpManager;

import timber.log.Timber;

public class FloatingCountdown {
    private static FloatingCountdown floatingCountdown;
    private final Context context;
    private WindowManager windowManager;
    private View floatingView;
    private TextView tvCountdown;

    private static boolean isShow = false;

    private CountDownTimer countDownTimer;
    private static boolean isPaused = false;
    private WindowManager.LayoutParams windowManagerParams;

    private CountDownTimerCallback callback;

    private long countDownSeconds;

    public static boolean isShow() {
        return isShow;
    }

    public static boolean isPaused() {
        return isPaused;
    }

    public static FloatingCountdown getInstance() {
        return floatingCountdown;
    }

    public static FloatingCountdown getInstance(Context context, long seconds, CountDownTimerCallback callback) {
        if (floatingCountdown == null) {
            synchronized (FloatingCountdown.class) {
                if (floatingCountdown == null) {
                    floatingCountdown = new FloatingCountdown(context, seconds, callback);
                }
            }
        }
        return floatingCountdown;
    }

    public FloatingCountdown(Context context, long seconds, CountDownTimerCallback callback) {
        this.context = context;
        this.callback = callback;
        this.countDownSeconds = seconds;
        initView();
        initCountDownTimer();
    }

    public void show(){
        if (isShow) return;
        isShow = true;
        windowManager.addView(floatingView, windowManagerParams);
        countDownTimer.start();
    }

    private void initView() {
        int languageType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
        Timber.tag(getClass().getSimpleName()).v("语言 %d", languageType);
        LocaleUtil.changeAppLanguage(context.getResources(), languageType);
        LayoutInflater inflater = LayoutInflater.from(context);
        floatingView = inflater.inflate(R.layout.layout_floating_countdown, null);

        windowManagerParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );


        windowManagerParams.gravity = Gravity.TOP | Gravity.START;
        windowManagerParams.x = 0;
        windowManagerParams.y = 150;

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        tvCountdown = floatingView.findViewById(R.id.tv_count_down);
        updateCountdownText(countDownSeconds*1000);
        tvCountdown.post(() -> {
            ViewGroup.LayoutParams layoutParams = tvCountdown.getLayoutParams();
            layoutParams.height = tvCountdown.getWidth();
            tvCountdown.setLayoutParams(layoutParams);
        });
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = windowManagerParams.x;
                        initialY = windowManagerParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        windowManagerParams.x = initialX + deltaX;
                        windowManagerParams.y = initialY + deltaY;
                        windowManager.updateViewLayout(floatingView, windowManagerParams);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void initCountDownTimer() {
        countDownTimer = new CountDownTimer(countDownSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isPaused) {
                    updateCountdownText(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                if (callback != null) {
                    callback.onFinish();
                }
                close();

            }
        };
    }

    private void updateCountdownText(long millisUntilFinished) {
        long seconds = millisUntilFinished / 1000;
        countDownSeconds = seconds;
        String formattedTime = String.format("%02d:%02d", seconds / 60, seconds % 60);
        tvCountdown.setText(formattedTime);
        if (seconds < 5) {
            tvCountdown.post(() -> tvCountdown.setTextColor(Color.RED));

        }
    }

    public void close() {
        isShow = false;
        isPaused = false;
        callback = null;
        if (windowManager != null && floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
            windowManager = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        floatingCountdown = null;
    }

    public void pause() {
        isPaused = true;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public void resume() {
        isPaused = false;
        initCountDownTimer();
        countDownTimer.start();
    }

    public interface CountDownTimerCallback {
        void onFinish();
    }
}

