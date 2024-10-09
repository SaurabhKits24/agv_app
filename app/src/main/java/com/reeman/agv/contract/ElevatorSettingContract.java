package com.reeman.agv.contract;

import android.content.Context;

import kotlin.Pair;

import com.reeman.agv.presenter.IPresenter;
import com.reeman.points.model.request.MapVO;
import com.reeman.agv.view.IView;

import java.util.List;

public interface ElevatorSettingContract {

    interface Presenter extends IPresenter {
        void onPointsLoadByMap(Context context,String map,String alias,boolean checkChargingPile);

        void onLoadMaps(Context context, Pair<String,String> defaultMap, boolean checkChargingPile);
    }

    interface View extends IView {

        void onMapListLoadedSuccess(List<MapVO> mapVOList,boolean checkChargingPile);
        void onMapListLoadedFailed(String msg);

        void onPointsLoadedFailed(String msg,boolean checkChargingPile);

        void onPointsLoadedSuccess(String alias,String map,boolean checkChargingPile);

    }
}
