package com.reeman.agv.fragments.main;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;
import com.reeman.agv.adapter.RouteListAdapter;
import com.reeman.agv.base.BaseFragment;
import com.reeman.dao.repository.entities.RouteWithPoints;
import com.reeman.agv.widgets.IndentItemDecoration;

import java.util.List;

public class ModeRouteTaskFragment extends BaseFragment {

    private RouteListAdapter routeListAdapter;

    private final List<RouteWithPoints> routeWithPointsList;

    public ModeRouteTaskFragment(List<RouteWithPoints> routeWithPointsList) {
        this.routeWithPointsList = routeWithPointsList;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_mode_route_task;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvRouteList = $(R.id.rv_route_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        rvRouteList.setLayoutManager(layoutManager);
        IndentItemDecoration decor = new IndentItemDecoration(requireActivity(), 0, 30);
        rvRouteList.addItemDecoration(decor);
        routeListAdapter = new RouteListAdapter();
        rvRouteList.setAdapter(routeListAdapter);
        routeListAdapter.setList(routeWithPointsList, 0);
    }

    public int getSelectedRouteIndex(){
        return routeListAdapter.getCurrentIndex();
    }

    public RouteWithPoints getSelectedRoute(){
        return routeWithPointsList.get(getSelectedRouteIndex());
    }
}
