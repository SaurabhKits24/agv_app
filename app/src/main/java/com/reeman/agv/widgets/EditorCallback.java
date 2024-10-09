package com.reeman.agv.widgets;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

/**
 * 创建日期：2017/9/13.
 *
 * @author kevin
 */

public interface EditorCallback {
    void onTryListen(Activity activity, String text, View cancel, View submit);
    void onConfirm(Activity activity, String content, View cancel, View submit);
    void onAttached(ViewGroup rootView);

    void onFinish();
}
