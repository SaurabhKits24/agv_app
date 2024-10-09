package com.reeman.agv.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.BaseAdapter;

import com.reeman.agv.R;
import com.reeman.agv.widgets.TableNumberView;
import com.reeman.points.model.custom.GenericPoint;

import java.util.List;

public class TableNumberAdapter extends BaseAdapter {

    private List<GenericPoint> list;
    private int selectedItem = -1; // 选中项的索引


    public TableNumberAdapter(List<GenericPoint> list, OnTableNumberClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void setList(List<GenericPoint> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setSelectedItem(int position) {
        selectedItem = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TableNumberView tableNumberView;
        if (convertView == null) {
            tableNumberView = new TableNumberView(parent.getContext());
        } else {
            tableNumberView = (TableNumberView) convertView;
        }
        tableNumberView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableNumberView.animate()
                        .setDuration(100)
                        .setInterpolator(new AccelerateInterpolator())
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .withLayer()
                        .withStartAction(() -> {
                            tableNumberView.setBackgroundResource(R.drawable.bg_table_number_selected);
                            tableNumberView.setTextColor(Color.WHITE);
                        })
                        .withEndAction(() -> tableNumberView
                                .animate()
                                .setDuration(100)
                                .scaleX(1)
                                .scaleY(1)
                                .withEndAction(() -> {
                                    tableNumberView.setBackgroundResource(R.drawable.bg_table_number_normal);
                                    tableNumberView.setTextColor(Color.parseColor("#FF666666"));
                                })
                                .start())
                        .start();
                setSelectedItem(position);
                listener.onTableNumberClick(position);

            }
        });
        // 设置选中和非选中项的背景和文本颜色
        if (position == selectedItem) {
            tableNumberView.setBackgroundResource(R.drawable.bg_table_number_selected);
            tableNumberView.setTextColor(Color.WHITE);
        } else {
            tableNumberView.setBackgroundResource(R.drawable.bg_table_number_normal);
            tableNumberView.setTextColor(Color.parseColor("#FF666666"));
        }

        String name = list.get(position).getName();
        tableNumberView.setText(name);
        if (name.length() > 10) {
            tableNumberView.setTextSize(18);
        } else {
            tableNumberView.setTextSize(24);
        }
        return tableNumberView;
    }


    private final OnTableNumberClickListener listener;

    public interface OnTableNumberClickListener {
        void onTableNumberClick(int position);
    }
}
