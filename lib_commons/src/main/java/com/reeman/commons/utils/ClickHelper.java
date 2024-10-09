package com.reeman.commons.utils;

public class ClickHelper {

    private static long lastClickTimeMills;
    private static int fastClickCount;


    public ClickHelper(OnFastClickListener listener) {
        this.listener = listener;
    }

    public void fastClick() {
        if (System.currentTimeMillis() - lastClickTimeMills < 300) {
            if (++fastClickCount >= 3) {
                if (listener != null) listener.onFastClick();
                fastClickCount = 0;
            }
        } else {
            fastClickCount = 0;
        }
        lastClickTimeMills = System.currentTimeMillis();
    }

    private final OnFastClickListener listener;

    public interface OnFastClickListener {
        void onFastClick();
    }
}
