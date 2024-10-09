package com.reeman.agv.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.reeman.agv.R;
import com.reeman.agv.widgets.TableNumberView;
import com.reeman.commons.utils.ClickRestrict;

import java.util.List;

import kotlin.Pair;

public class PointsAdapter extends BaseAdapter {

    private List<String> allPoints;
    private Pair<String, String> selectedPoint;
    private String currentMap = "";

    public PointsAdapter(List<String> allPoints, Pair<String, String> selectedPoint) {
        this.allPoints = allPoints;
        this.selectedPoint = selectedPoint;
    }

    public PointsAdapter(List<String> allPoints, Pair<String, String> selectedPoint, String currentMap) {
        this.allPoints = allPoints;
        this.selectedPoint = selectedPoint;
        this.currentMap = currentMap;
    }

    public void updateData(List<String> allPoints, Pair<String, String> selectedPoint, String currentMap) {
        this.allPoints = allPoints;
        this.selectedPoint = selectedPoint;
        this.currentMap = currentMap;
        notifyDataSetChanged();
    }

    public Pair<String, String> getSelectedPoints() {
        return selectedPoint;
    }

    @Override
    public int getCount() {
        return allPoints == null ? 0 : allPoints.size();
    }

    @Override
    public Object getItem(int position) {
        return allPoints.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String point = allPoints.get(position);
        TableNumberView tableNumberView;
        if (convertView == null) {
            tableNumberView = new TableNumberView(parent.getContext());
            tableNumberView.setOnClickListener(v -> {
                if (ClickRestrict.restrictFrequency(300)) return;
                String text = ((TableNumberView) v).getText();
                if (isPointSelected(text)) {
                    setPointUnselected();
                    notifyDataSetChanged();
                } else {
                    selectedPoint = new Pair<>(currentMap, text);
                    notifyDataSetChanged();
                }
            });
        } else {
            tableNumberView = (TableNumberView) convertView;
        }

        boolean isSelected = isPointSelected(point);
        tableNumberView.select(isSelected);
        if (isSelected) {
            tableNumberView.setTextColor(Color.WHITE);
            tableNumberView.setBackgroundResource(R.drawable.bg_table_number_selected);
        } else {
            tableNumberView.setTextColor(Color.parseColor("#FF666666"));
            tableNumberView.setBackgroundResource(R.drawable.bg_table_number_normal);
        }

        tableNumberView.setText(point);
        if (point.length() > 10) {
            tableNumberView.setTextSize(12);
        } else {
            tableNumberView.setTextSize(16);
        }

        return tableNumberView;
    }

    private boolean isPointSelected(String name) {
        if (selectedPoint == null) return false;
        if (TextUtils.isEmpty(currentMap)) {
            return selectedPoint.getSecond().equals(name);
        } else {
            return selectedPoint.getSecond().equals(name) && currentMap.equals(selectedPoint.getFirst());
        }
    }

    private void setPointUnselected() {
        selectedPoint = null;
    }
}
