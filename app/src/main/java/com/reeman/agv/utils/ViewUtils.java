package com.reeman.agv.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import androidx.appcompat.widget.AppCompatImageButton;

public class ViewUtils {


    /**
     * 从右渐入
     * @return
     */
    public static AnimationSet getFadeInFromRight() {
        AnimationSet fadeAnimation = new AnimationSet(true);
        TranslateAnimation translateAnimationIn = new TranslateAnimation(
                android.view.animation.Animation.RELATIVE_TO_PARENT, 1f,
                android.view.animation.Animation.RELATIVE_TO_PARENT, 0f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0f
        );
        AlphaAnimation alphaAnimationIn = new AlphaAnimation(0f, 1f);
        fadeAnimation.addAnimation(translateAnimationIn);
        fadeAnimation.addAnimation(alphaAnimationIn);
        fadeAnimation.setDuration(500);
        return fadeAnimation;
    }

    /**
     * 渐出到左
     * @return
     */
    public static AnimationSet getFadeOutToLeft(){
        AnimationSet fadeAnimation = new AnimationSet(true);
        TranslateAnimation translateAnimationOut = new TranslateAnimation(
                android.view.animation.Animation.RELATIVE_TO_SELF, 0f,
                android.view.animation.Animation.RELATIVE_TO_PARENT, -1f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0f
        );
        AlphaAnimation alphaAnimationOut = new AlphaAnimation(1f, 0f);
        fadeAnimation.addAnimation(translateAnimationOut);
        fadeAnimation.addAnimation(alphaAnimationOut);
        fadeAnimation.setDuration(500);
        return fadeAnimation;
    }

    /**
     * 从下渐入
     * @return
     */
    public static AnimationSet getFadeInFromBottom() {
        AnimationSet fadeAnimation = new AnimationSet(true);
        TranslateAnimation translateAnimationIn = new TranslateAnimation(
                android.view.animation.Animation.RELATIVE_TO_SELF, 0f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0f,
                android.view.animation.Animation.RELATIVE_TO_PARENT, 1f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0f
        );
        AlphaAnimation alphaAnimationIn = new AlphaAnimation(0f, 1f);
        fadeAnimation.addAnimation(translateAnimationIn);
        fadeAnimation.addAnimation(alphaAnimationIn);
        fadeAnimation.setDuration(500);
        return fadeAnimation;
    }

    /**
     * 渐出到上
     * @return
     */
    public static AnimationSet getFadeOutToTop() {
        AnimationSet fadeAnimation = new AnimationSet(true);
        TranslateAnimation translateAnimationOut = new TranslateAnimation(
                android.view.animation.Animation.RELATIVE_TO_SELF, 0f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0f,
                android.view.animation.Animation.RELATIVE_TO_PARENT, -1f
        );
        AlphaAnimation alphaAnimationOut = new AlphaAnimation(1f, 0f);
        fadeAnimation.addAnimation(translateAnimationOut);
        fadeAnimation.addAnimation(alphaAnimationOut);
        fadeAnimation.setDuration(500);
        return fadeAnimation;
    }

    /**
     * 重设view的height与width相等
     * @param viewGroup
     * @param ids
     */
    public static void resetViewHeight(ViewGroup viewGroup, int... ids) {
        for (int id : ids) {
            AppCompatImageButton imageButton = viewGroup.findViewById(id);
            imageButton.post(() -> {
                ViewGroup.LayoutParams layoutParams = imageButton.getLayoutParams();
                layoutParams.height = imageButton.getWidth();
                imageButton.setLayoutParams(layoutParams);
            });
        }
    }

    /**
     * 切换子view,带渐入渐出动画
     * @param parentView
     * @param newView
     */
    public static void showViewWithAnimation(ViewGroup parentView, View newView) {
        View child = parentView.getChildAt(0);
        if (child == null) {
            parentView.removeAllViews();
            parentView.addView(newView);
        } else {
            child.startAnimation(getFadeOutToTop());
            parentView.removeView(child);
            parentView.addView(newView);
            newView.startAnimation(getFadeInFromBottom());

        }
    }
}
