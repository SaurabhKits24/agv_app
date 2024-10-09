package com.reeman.agv.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.reeman.agv.R;

import java.util.List;

public class AutoWrapTextViewGroup extends ViewGroup {

    private static final String TAG = "AutoWrapTextViewGroup";

    private int horizontalSpacing = 10;
    private int verticalSpacing = 10;

    public AutoWrapTextViewGroup(Context context) {
        super(context);
    }

    public AutoWrapTextViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoWrapTextViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(List<String> data) {
        removeAllViews();
        for (String text : data) {
            TextView textView = new TextView(getContext());
            textView.setText(text);
            textView.setTextSize(16);
            textView.setGravity(android.view.Gravity.CENTER);
            textView.setBackgroundResource(R.drawable.bg_route_point);
            textView.setTextColor(Color.WHITE);
            addView(textView);
        }
        requestLayout();
    }

    public void setData(List<String> data,List<String> warnData) {
        removeAllViews();
        for (String text : data) {
            TextView textView = new TextView(getContext());
            textView.setText(text);
            textView.setTextSize(18);
            textView.setGravity(android.view.Gravity.CENTER);
            if (warnData.contains(text)){
                textView.setBackgroundResource(R.drawable.bg_route_point_warn);
            }else {
                textView.setBackgroundResource(R.drawable.bg_route_point);
            }
            textView.setTextColor(Color.WHITE);
            addView(textView);
        }
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();

        int parentRight = r - l - getPaddingRight();
        int parentBottom = b - t - getPaddingBottom();

        int currentLineHeight = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                if (childLeft + childWidth > parentRight) {
                    childLeft = getPaddingLeft();
                    childTop += currentLineHeight + verticalSpacing; // add vertical spacing
                    currentLineHeight = 0;
                }

                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
                Log.d(TAG, "onLayout: Placing child " + i + " at (" + childLeft + ", " + childTop + ")");

                childLeft += childWidth + horizontalSpacing; // add horizontal spacing
                currentLineHeight = Math.max(currentLineHeight, childHeight);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0;
        int height = 0;

        int currentLineWidth = 0;
        int currentLineHeight = 0;

        int maxWidth = widthMode == MeasureSpec.EXACTLY ? widthSize : Integer.MAX_VALUE;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                if (currentLineWidth + childWidth > maxWidth) {
                    width = Math.max(width, currentLineWidth);
                    currentLineWidth = childWidth;
                    height += currentLineHeight + verticalSpacing; // add vertical spacing
                    currentLineHeight = childHeight;
                } else {
                    currentLineWidth += childWidth + horizontalSpacing; // add horizontal spacing
                    currentLineHeight = Math.max(currentLineHeight, childHeight);
                }
            }
        }

        width = Math.max(width, currentLineWidth) + getPaddingLeft() + getPaddingRight();
        height += currentLineHeight + getPaddingTop() + getPaddingBottom();

        width = widthMode == MeasureSpec.EXACTLY ? widthSize : width;
        height = heightMode == MeasureSpec.EXACTLY ? heightSize : height;

        setMeasuredDimension(width, height);
        Log.d(TAG, "onMeasure: width=" + width + ", height=" + height);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}