package com.reeman.agv.fragments.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.reeman.agv.R;
import com.reeman.agv.adapter.RouteWithPointsAdapter;
import com.reeman.agv.base.BaseFragment;
import com.reeman.agv.contract.ModeRouteContract;
import com.reeman.agv.presenter.impl.ModeRoutePresenter;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.dao.repository.entities.RouteWithPoints;
import com.reeman.agv.widgets.EasyDialog;
import java.util.List;

public class ModeRouteEditFragment extends BaseFragment implements RouteWithPointsAdapter.OnItemClickListener, ModeRouteContract.View {

    private ModeRoutePresenter presenter;

    private ExpandableListView expandRouteListView;
    private RouteWithPointsAdapter routeWithPointsAdapter;
    private List<RouteWithPoints> routeWithPointsList;

    private int deleteGroupPosition;

    private final ModeRouteEditClickListener clickListener;

    public List<RouteWithPoints> getRouteWithPointsList() {
        return routeWithPointsList;
    }

    public void setRouteWithPointsList(List<RouteWithPoints> routeWithPointsList) {
        this.routeWithPointsList = routeWithPointsList;
    }

    public ModeRouteEditFragment(List<RouteWithPoints> routeWithPointsList, ModeRouteEditClickListener clickListener) {
        this.routeWithPointsList = routeWithPointsList;
        this.clickListener = clickListener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_mode_route_edit;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expandRouteListView = $(R.id.expand_route_list);
        AppCompatImageButton ibAddRoute = $(R.id.ib_add_route);
        routeWithPointsAdapter = new RouteWithPointsAdapter(requireActivity(), expandRouteListView, routeWithPointsList, this);
        expandRouteListView.setIndicatorBounds(0, 50);
        expandRouteListView.setAdapter(routeWithPointsAdapter);
        ibAddRoute.setOnClickListener(this);
        presenter = new ModeRoutePresenter(this);
    }

    @Override
    protected void onCustomClickResult(int id) {
        if (id == R.id.ib_add_route) {
            clickListener.onAddRouteTask(RouteWithPoints.getDefault(getString(R.string.text_default_route_name, routeWithPointsList.size() + 1), robotInfo.getNavigationMode()));
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
    }

    @Override
    public void onEditRoute(RouteWithPoints routeWithPoints) {
        clickListener.onEditRouteTask(routeWithPoints);
    }

    @Override
    public void onDeleteRoute(int groupPosition, RouteWithPoints routeWithPoints) {
        EasyDialog.getInstance(requireActivity()).confirm(getString(R.string.text_if_delete_route, routeWithPoints.getRouteName()), (dialog, id) -> {
            dialog.dismiss();
            if (id == R.id.btn_confirm) {
                presenter.deleteRoute(routeWithPoints);
                EasyDialog.getLoadingInstance(requireActivity()).loading(getString(R.string.text_is_delete_route));
                deleteGroupPosition = groupPosition;
            }
        });
    }

    @Override
    public void onDeleteRouteSuccess() {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        ToastUtils.showShortToast(getString(R.string.text_delete_success));
        routeWithPointsAdapter.remove(deleteGroupPosition);

    }

    @Override
    public void onDeleteRouteFailed(Throwable throwable) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        ToastUtils.showShortToast(getString(R.string.text_delete_failed,throwable.getMessage()));
    }

    public interface ModeRouteEditClickListener {

        void onAddRouteTask(RouteWithPoints routeWithPoints);

        void onEditRouteTask(RouteWithPoints routeWithPoints);
    }
}
