package com.reeman.agv.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.reeman.agv.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kotlin.Pair;

public class QRCodeCallingBoundAdapter extends BaseExpandableListAdapter {

    private final Activity activity;
    private final ExpandableListView expandRouteListView;
    private final Map<String, List<Pair<Pair<String,String>, Pair<String,String>>>> boundCallingMap;
    private final List<String> routeNames;
    private final OnItemClickListener listener;

    public QRCodeCallingBoundAdapter(Activity activity, ExpandableListView expandRouteListView, Map<String,List<Pair<Pair<String,String>, Pair<String,String>>>> boundCallingMap, OnItemClickListener listener) {
        this.activity = activity;
        this.expandRouteListView = expandRouteListView;
        this.boundCallingMap = boundCallingMap;
        this.routeNames = new ArrayList<>(boundCallingMap.keySet());

        this.listener = listener;
        expandRouteListView.setOnGroupClickListener((parent, v, groupPosition, id) -> false);
    }

    public void resetData(Map<String, List<Pair<Pair<String,String>, Pair<String,String>>>> map) {
        this.boundCallingMap.clear();
        this.routeNames.clear();
        if (!map.isEmpty()) {
            this.boundCallingMap.putAll(map);
            this.routeNames.addAll(map.keySet());
        }

        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return routeNames.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String header = routeNames.get(groupPosition);
        return Objects.requireNonNull(boundCallingMap.get(header)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return routeNames.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String routeName = routeNames.get(groupPosition);
        return Objects.requireNonNull(boundCallingMap.get(routeName)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_item_bound_call_mode, null);
        }

        convertView.setOnClickListener(view -> {
            if (expandRouteListView.isGroupExpanded(groupPosition)) {
                expandRouteListView.collapseGroup(groupPosition);
            } else {
                expandRouteListView.expandGroup(groupPosition);
            }
        });

        TextView tvRouteName = convertView.findViewById(R.id.tv_bound_key_name);
        AppCompatImageView ivDeleteRoute = convertView.findViewById(R.id.iv_bound_delete_key);
        ivDeleteRoute.setOnClickListener(view -> listener.onDeleteBoundKey(groupPosition));
        tvRouteName.setText(headerTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Pair<Pair<String,String>, Pair<String,String>> qrCodeModel = (Pair<Pair<String,String>, Pair<String,String>>) getChild(groupPosition, childPosition);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.layout_item_bound_point, null);
        TextView first = convertView.findViewById(R.id.tv_bound_first_point);
        TextView second = convertView.findViewById(R.id.tv_bound_second_point);
        //AppCompatImageView ivDeletePoint = convertView.findViewById(R.id.iv_delete_point);
        first.setText(qrCodeModel.getFirst().getSecond());
        second.setText(qrCodeModel.getSecond().getSecond());
        //ivDeletePoint.setOnClickListener(view -> listener.onDeletePoint(groupPosition, childPosition));
        return convertView;
    }


    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        for (int i = 0; i < routeNames.size(); i++) {
            if (i != groupPosition) {
                if (expandRouteListView.isGroupExpanded(i)) {
                    expandRouteListView.collapseGroup(i);
                }
            }
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public interface OnItemClickListener {
        /**
         * 删除路线
         * @param position
         */
        void onDeleteBoundKey(int position);

        /**
         * 删除路线点
         * @param groupPosition
         * @param childPosition
         */
        void onDeletePoint(int groupPosition, int childPosition);

    }
}
