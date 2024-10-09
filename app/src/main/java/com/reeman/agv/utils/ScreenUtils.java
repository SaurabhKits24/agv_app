package com.reeman.agv.utils;

import android.app.Activity;
import android.os.Build;

import com.reeman.commons.board.Board;
import com.reeman.commons.board.BoardFactory;

public class ScreenUtils {

    /**
     * 沉浸式，上划可以显示导航栏，过一段时间自动消失
     *
     * @param context
     */
    public static void setImmersive(Activity context) {
        Board board = BoardFactory.create(context, Build.PRODUCT);
        board.navigationBarControl(context, true);
        board.gestureNavigationBarControl(context, true);
        board.statusBarControl(context, true);
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    public static void hideBottomUIMenu(Activity activity) {
        Board board = BoardFactory.create(activity, Build.PRODUCT);
        board.navigationBarControl(activity, false);
        board.gestureNavigationBarControl(activity, false);
        board.statusBarControl(activity, false);
    }
}