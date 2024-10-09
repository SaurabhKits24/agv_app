package com.reeman.agv.widgets;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reeman.agv.R;

import java.util.List;

public class ChooseItemPopupWindow extends ListPopupWindow {

    public ChooseItemPopupWindow(Context context, View anchor, List<String> items,OnItemChosenListener onItemChosenListener) {
        super(context);
        initView(context, anchor,null, items, null,onItemChosenListener);
    }

    public ChooseItemPopupWindow(Context context, View anchor, String title, List<String> items,OnItemChosenListener onItemChosenListener) {
        super(context);
        initView(context, anchor, title, items, null,onItemChosenListener);
    }

    public ChooseItemPopupWindow(Context context, View anchor,  List<String> items, String selectItem,OnItemChosenListener onItemChosenListener) {
        super(context);
        initView(context, anchor, null, items, selectItem,onItemChosenListener);
    }

    public ChooseItemPopupWindow(Context context, View anchor, String title, List<String> items, String selectItem,OnItemChosenListener onItemChosenListener) {
        super(context);
        initView(context, anchor, title, items, selectItem,onItemChosenListener);
    }

    private void initView(Context context, View anchor, String title, List<String> items, String selectItem,OnItemChosenListener onItemChosenListener) {
        setAnchorView(anchor);
        setWidth(500);
        setHeight(360);
        int centerX = (int) (anchor.getX() + anchor.getWidth() / 2);
        setHorizontalOffset((int) (centerX - (anchor.getX() + 250)));
        setVerticalOffset((int) (anchor.getY() + 40));
        View promptView = LayoutInflater.from(context).inflate(R.layout.layout_pop_up_window_prompt, null);
        if (!TextUtils.isEmpty(title)) {
            TextView tvTitle = promptView.findViewById(R.id.tv_pop_up_window_title);
            tvTitle.setText(title);
        }
        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_common_dialog));
        setAnimationStyle(R.style.popupWindowAlphaAnimation);
        setPromptView(promptView);
        if (TextUtils.isEmpty(selectItem)) {
            setAdapter(new ArrayAdapter<>(context, R.layout.layout_spinner_item, R.id.tv_spinner_item, items));
        } else {
            setAdapter(new ArrayAdapter<String>(context, R.layout.layout_spinner_item, R.id.tv_spinner_item, items) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View itemView = super.getView(position, convertView, parent);
                    if (items.get(position).equals(selectItem)) {
                        itemView.setBackgroundColor(Color.parseColor("#cdcdcd"));
                    } else {
                        itemView.setBackgroundColor(Color.WHITE);
                    }
                    return itemView;
                }
            });
        }
        setDropDownGravity(Gravity.CENTER);
        setOnItemClickListener((parent, view, position, id) -> {
            if (onItemChosenListener != null) {
                onItemChosenListener.onSpeedChosen(ChooseItemPopupWindow.this, items.get(position));
            }
        });
    }


    public interface OnItemChosenListener {
        void onSpeedChosen(ListPopupWindow window, String itemData);
    }
}
