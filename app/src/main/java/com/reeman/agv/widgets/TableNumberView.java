package com.reeman.agv.widgets;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.reeman.agv.R;

public class TableNumberView extends RelativeLayout {

    private final TextView content;
    private final ImageView badgeView;

    public TableNumberView(Context context) {
        super(context);
        content = new TextView(context);
        content.setSingleLine();
        content.setEllipsize(TextUtils.TruncateAt.END);
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        int margin = 10;
        int padding = 5;
        int height = 64;
        if (heightPixels >= 800) {
            margin = 20;
            padding = 10;
            height = 78;
        }
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = margin;
        layoutParams.rightMargin = margin;
        layoutParams.addRule(CENTER_IN_PARENT);
        content.setLayoutParams(layoutParams);
        addView(content);

        badgeView = new ImageView(getContext());
        badgeView.setVisibility(INVISIBLE);
        badgeView.setPadding(padding, padding, padding, padding);
        badgeView.setImageResource(R.drawable.ic_check_24);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(ALIGN_PARENT_RIGHT);
        lp.addRule(ALIGN_PARENT_TOP);
        badgeView.setLayoutParams(lp);
        addView(badgeView);

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    }

    public void setTextSize(float textSize) {
        content.setTextSize(textSize);
    }

    public String getText() {
        return content.getText().toString();
    }

    public void setTextColor(@ColorInt int color) {
        content.setTextColor(color);
    }

    public void setText(String text) {
        content.setText(text);
    }

    public void select(boolean selected) {
        badgeView.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
    }
}
