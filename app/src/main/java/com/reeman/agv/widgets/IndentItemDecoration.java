package com.reeman.agv.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;

public class IndentItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable divider;
    private int startIndent;
    private int endIndent;

    public IndentItemDecoration(Context context, int startIndent, int endIndent) {
        divider = context.getDrawable(R.drawable.divider);
        this.startIndent = startIndent;
        this.endIndent = endIndent;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int childCount = parent.getChildCount();
        int width = parent.getWidth();
        int dividerHeight = divider.getIntrinsicHeight();

        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + dividerHeight;

            divider.setBounds(startIndent, top, width - endIndent, bottom);
            divider.draw(canvas);
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = divider.getIntrinsicHeight();
    }
}
