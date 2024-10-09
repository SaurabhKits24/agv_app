package com.reeman.agv.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.kyleduo.switchbutton.SwitchButton;
import com.reeman.agv.R;

import java.lang.reflect.Field;

public class TextSwitchButton extends SwitchButton {

    private String leftText;
    private String rightText;
    private Paint textPaint;
    private SwitchButton switchButtonInstance;

    public TextSwitchButton(Context context) {
        super(context);
        switchButtonInstance = new SwitchButton(context);
        init();
    }

    public TextSwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        switchButtonInstance = new SwitchButton(context);
        init();
    }

    public TextSwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        switchButtonInstance = new SwitchButton(context);
        init();
    }

    private void init() {
        // 初始化画笔
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE); // 设置文字颜色
        textPaint.setTextSize(18); // 设置文字大小
        textPaint.setAntiAlias(true); // 开启抗锯齿

    }

    public void setText() {
        this.leftText = getTextOff().toString();
        this.rightText = getTextOn().toString();
        invalidate(); // 重新绘制
    }

    private RectF getThumb() {
        RectF thumbRect = null;
        try {
            Field thumbRectField = SwitchButton.class.getDeclaredField("mPresentThumbRectF");
            thumbRectField.setAccessible(true);
            thumbRect = (RectF) thumbRectField.get(switchButtonInstance);
            // 现在可以使用 thumbRect 访问滑动块的位置信息
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return thumbRect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF thumb = getThumb();
        // 获取滑动块的位置
        float thumbLeft = thumb.left;
        float thumbRight = thumb.right;
        float thumbTop = thumb.top;
        float thumbBottom = thumb.bottom;

        // 计算文字的位置
        float leftTextX = thumbLeft - textPaint.measureText(leftText) -8;
        float leftTextY = thumbTop + (thumbBottom - thumbTop) / 2f + getTextBaselineOffset(textPaint);

        float rightTextX = thumbRight + 8;
        float rightTextY = thumbTop + (thumbBottom - thumbTop) / 2f + getTextBaselineOffset(textPaint);

        // 绘制文字
        canvas.drawText(leftText, leftTextX, leftTextY, textPaint);
        canvas.drawText(rightText, rightTextX, rightTextY, textPaint);
    }

    private float getTextBaselineOffset(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return (fontMetrics.descent + fontMetrics.ascent) / 2f;
    }
}
