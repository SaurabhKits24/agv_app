package com.reeman.agv.contract;

import com.reeman.agv.presenter.IPresenter;
import com.reeman.agv.view.IView;
import com.reeman.dao.repository.entities.RouteWithPoints;

import java.util.List;


public interface ModeRouteContract {

    interface Presenter extends IPresenter {
        void deleteRoute(RouteWithPoints routeWithPoints);
        void updateRoute(RouteWithPoints routeWithPoints);
         void addRoute(RouteWithPoints routeWithPoints);
        void getAllRoute(int navigationMode);
    }

    interface View extends IView {

        default void onDeleteRouteSuccess(){}

        default void onDeleteRouteFailed(Throwable throwable){}

        default void onUpdateRouteSuccess(){}

        default void onUpdateRouteFailed(Throwable throwable){}

        default void onAddRouteSuccess(){}

        default void onAddRouteFailed(Throwable throwable){}

        default void onGetAllRouteSuccess(List<RouteWithPoints> routeWithPointsList){}

        default void onGetAllRouteFailed(Throwable throwable){}
    }
}
