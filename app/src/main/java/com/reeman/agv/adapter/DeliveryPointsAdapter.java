package com.reeman.agv.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.BaseAdapter;

import com.reeman.agv.R;
import com.reeman.commons.utils.ClickRestrict;
import com.reeman.agv.widgets.TableNumberView;
import com.reeman.points.model.custom.GenericPoint;

import java.util.List;

public class DeliveryPointsAdapter extends BaseAdapter {

    private List<GenericPoint> list;
    private final OnDeliveryPointClickListener listener;
    private String map;

    public DeliveryPointsAdapter(OnDeliveryPointClickListener listener) {
        this.listener = listener;
    }

    public void updatePoints(String map,List<GenericPoint> list){
        this.map = map;
        this.list = list;
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

        GenericPoint point = list.get(position);
        tableNumberView.select(false);
        tableNumberView.setTextColor(Color.parseColor("#FF666666"));
        tableNumberView.setBackgroundResource(R.drawable.bg_table_number_normal);
        tableNumberView.setText(point.getName());

        if (point.getName().length() > 5) {
            tableNumberView.setTextSize(18);
        } else {
            tableNumberView.setTextSize(24);
        }

        tableNumberView.setOnClickListener(v -> {
            if (ClickRestrict.restrictFrequency(300))return;
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
            GenericPoint checkedPoint = (GenericPoint) v.getTag();
            if (map == null) {
                listener.onDeliveryPointClick(checkedPoint);
            }else {
                listener.onEleDeliveryPointClick(map,checkedPoint);
            }
        });

        tableNumberView.setTag(point);

        return tableNumberView;
    }


    public interface OnDeliveryPointClickListener {
        void onDeliveryPointClick(GenericPoint checkedPoint);

        void onEleDeliveryPointClick(String map,GenericPoint checkedPoint);

    }
}
