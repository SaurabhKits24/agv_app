package com.reeman.agv.widgets;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
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

public class FloatingCallingListView {
    private static FloatingCallingListView floatingCallingListView;
    private final Context context;
    private WindowManager windowManager;
    private View view;

    private TextView tvCallingQueueSize;

    private static boolean isShow;

    private OnFloatingCallingListViewClickListener listener;

    public void setListener(OnFloatingCallingListViewClickListener listener) {
        this.listener = listener;
    }

    public static boolean isShow() {
        return floatingCallingListView != null && isShow;
    }

    private WindowManager.LayoutParams windowManagerParams;

    public static FloatingCallingListView getInstance() {
        return floatingCallingListView;
    }


    public static FloatingCallingListView getInstance(Context context, int queueSize, OnFloatingCallingListViewClickListener listener) {
        if (floatingCallingListView == null) {
            synchronized (FloatingCallingListView.class) {
                if (floatingCallingListView == null) {
                    floatingCallingListView = new FloatingCallingListView(context, queueSize, listener);
                }
            }
        }
        return floatingCallingListView;
    }

    public FloatingCallingListView(Context context, int queueSize, OnFloatingCallingListViewClickListener listener) {
        this.context = context;
        this.listener = listener;
        initView(queueSize);
    }

    private void initView(int queueSize) {
        int languageType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
        Timber.tag(getClass().getSimpleName()).v("语言 %d", languageType);
        LocaleUtil.changeAppLanguage(context.getResources(), languageType);
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.layout_floating_calling_list, null);
        tvCallingQueueSize = view.findViewById(R.id.tv_calling_queue_size);
        tvCallingQueueSize.post(() -> {
            ViewGroup.LayoutParams layoutParams = tvCallingQueueSize.getLayoutParams();
            layoutParams.width = tvCallingQueueSize.getHeight();
            tvCallingQueueSize.setLayoutParams(layoutParams);
        });
        tvCallingQueueSize.setText(String.valueOf(queueSize));
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
        windowManagerParams.y = 50;

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isClick = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (windowManager == null || view == null) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = windowManagerParams.x;
                        initialY = windowManagerParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isClick = true;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        windowManagerParams.x = initialX + deltaX;
                        windowManagerParams.y = initialY + deltaY;
                        windowManager.updateViewLayout(view, windowManagerParams);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
                                isClick = false;
                            }
                        } else {
                            isClick = false;
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (isClick && listener != null) {
                            listener.onFloatingCallingListClick();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

    public void show() {
        if (isShow) return;
        isShow = true;
        windowManager.addView(view, windowManagerParams);
    }

    public void updateQueueSize(int queueSize) {
        if (isShow && view != null && tvCallingQueueSize != null) {
            tvCallingQueueSize.setText(String.valueOf(queueSize));
        }
    }

    public void close() {
        isShow = false;
        if (windowManager != null && view != null) {
            windowManager.removeView(view);
            view = null;
            windowManager = null;
        }
        floatingCallingListView = null;
    }

    public interface OnFloatingCallingListViewClickListener {

        void onFloatingCallingListClick();
    }

}

