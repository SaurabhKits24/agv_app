package com.reeman.agv.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.reeman.agv.R;
import com.reeman.dao.repository.entities.PointsVO;
import com.reeman.dao.repository.entities.RouteWithPoints;

import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class RouteWithPointsAdapter extends BaseExpandableListAdapter {

    private final Activity activity;
    private final ExpandableListView expandRouteListView;
    private final List<RouteWithPoints> routeWithPointsList;
    private final HashMap<String, List<PointsVO>> childHashMap;
    private final OnItemClickListener listener;

    public List<RouteWithPoints> getRouteWithPointsList() {
        return routeWithPointsList;
    }

    public RouteWithPointsAdapter(Activity activity, ExpandableListView expandRouteListView, List<RouteWithPoints> routeWithPointsList, OnItemClickListener listener) {
        this.activity = activity;
        this.expandRouteListView = expandRouteListView;
        this.routeWithPointsList = routeWithPointsList;
        childHashMap = new HashMap<>();
        if (routeWithPointsList != null && !routeWithPointsList.isEmpty()) {
            for (RouteWithPoints routeWithPoints : routeWithPointsList) {
                childHashMap.put(routeWithPoints.getRouteName(), routeWithPoints.getPointsVOList());
            }
        }
//        this.childHashMap = childHashMap;
        this.listener = listener;
        expandRouteListView.setOnGroupClickListener((parent, v, groupPosition, id) -> false);
    }

    public void remove(int position) {
        Timber.w("delete position: %s , routeWithPointsList : %s",position,routeWithPointsList);
        if (routeWithPointsList != null && !routeWithPointsList.isEmpty() && position >= 0 && position < routeWithPointsList.size()) {
            RouteWithPoints routeWithPoints = routeWithPointsList.get(position);
            childHashMap.remove(routeWithPoints.getRouteName());
            routeWithPointsList.remove(position);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getGroupCount() {
        return routeWithPointsList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String header = routeWithPointsList.get(groupPosition).getRouteName();
        return childHashMap.get(header).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return routeWithPointsList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String header = routeWithPointsList.get(groupPosition).getRouteName();
        return childHashMap.get(header).get(childPosition);
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
        String headerTitle = ((RouteWithPoints) getGroup(groupPosition)).getRouteName();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_item_route_edit_mode, null);
        }

        convertView.setOnClickListener(view -> {
            if (expandRouteListView.isGroupExpanded(groupPosition)) {
                expandRouteListView.collapseGroup(groupPosition);
            } else {
                expandRouteListView.expandGroup(groupPosition);
            }
        });

        TextView tvRouteName = convertView.findViewById(R.id.tv_route_name);
        AppCompatImageView ivEditRoute = convertView.findViewById(R.id.iv_edit_route);
        AppCompatImageView ivDeleteRoute = convertView.findViewById(R.id.iv_delete_route);
        ivEditRoute.setOnClickListener(view -> listener.onEditRoute(routeWithPointsList.get(groupPosition)));
        ivDeleteRoute.setOnClickListener(view -> listener.onDeleteRoute(groupPosition,routeWithPointsList.get(groupPosition)));
        tvRouteName.setText(headerTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        PointsVO pointsVO = (PointsVO) getChild(groupPosition, childPosition);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.layout_item_point, null);
        TextView tvPointName = convertView.findViewById(R.id.tv_point_name);
        TextView tvWaitingTime = convertView.findViewById(R.id.tv_waiting_time);
        convertView.findViewById(R.id.iv_delete_point).setVisibility(View.GONE);
        tvPointName.setText(pointsVO.getPoint());
        if (pointsVO.isOpenWaitingTime()) {
            tvWaitingTime.setText(String.valueOf(pointsVO.getWaitingTime()));
        } else {
            tvWaitingTime.setText(activity.getString(R.string.text_closed));
        }
        return convertView;
    }


    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        for (int i = 0; i < routeWithPointsList.size(); i++) {
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
         * 修改路线
         */
        void onEditRoute(RouteWithPoints routeWithPoints);

        /**
         * 删除路线
         */
        void onDeleteRoute(int groupPosition,RouteWithPoints routeWithPoints);

    }
}
