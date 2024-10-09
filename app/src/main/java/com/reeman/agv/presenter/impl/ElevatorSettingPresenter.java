package com.reeman.agv.presenter.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import kotlin.Pair;

import com.reeman.agv.R;
import com.reeman.agv.contract.ElevatorSettingContract;
import com.reeman.points.model.request.MapVO;
import com.reeman.commons.state.NavigationMode;
import com.reeman.commons.state.RobotInfo;
import com.reeman.agv.widgets.EasyDialog;
import com.reeman.points.exception.RequiredPointsNotFoundException;
import com.reeman.points.model.request.Point;
import com.reeman.points.request.ApiClient;
import com.reeman.points.request.url.Url;
import com.reeman.points.utils.PointCacheInfo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class ElevatorSettingPresenter implements ElevatorSettingContract.Presenter {

    private final ElevatorSettingContract.View view;

    private final RobotInfo robotInfo;

    public ElevatorSettingPresenter(ElevatorSettingContract.View view) {
        this.view = view;
        robotInfo = RobotInfo.INSTANCE;
    }

    @Override
    public void onPointsLoadByMap(Context context, String map, String alias, boolean checkChargingPile) {
        EasyDialog.getLoadingInstance(context).loading(context.getString(R.string.text_check_map_list));
        if (robotInfo.getNavigationMode() == NavigationMode.autoPathMode) {
            fetchPointsByMap(context, map, alias, checkChargingPile);
        } else {
            fetchFixPointsByMap(context, map, alias, checkChargingPile);
        }
    }

    @SuppressLint("CheckResult")
    private void fetchFixPointsByMap(Context context, String map, String alias, boolean checkChargingPile) {
        ApiClient.INSTANCE.getApiService().fetchFixedPathPointsByMapAsync(Url.INSTANCE.getFixedPoints(robotInfo.getROSIPAddress()),map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pathModelPoint -> {
                    if (pathModelPoint == null
                            || pathModelPoint.getPoint() == null
                            || pathModelPoint.getPoint().isEmpty()
                            || pathModelPoint.getPath() == null
                            || pathModelPoint.getPath().isEmpty()
                    ) {
                        view.onPointsLoadedFailed(context.getString(R.string.text_loaded_success_not_mark_point), checkChargingPile);
                        return;
                    }
                    if (checkChargingPile) {
                        PointCacheInfo.INSTANCE.checkIsFixedChargePointMarked(pathModelPoint.getPoint());
                    }else {
                        PointCacheInfo.INSTANCE.checkIsFixedProductionPointMarked(pathModelPoint.getPoint());
                    }
                    view.onPointsLoadedSuccess(alias, map, checkChargingPile);
                }, throwable -> fetchPointFailed(context,throwable,checkChargingPile));
    }

    @SuppressLint("CheckResult")
    private void fetchPointsByMap(Context context, String map, String alias, boolean checkChargingPile) {
        ApiClient.INSTANCE.getApiService().fetchPointsByMapAsync(Url.INSTANCE.getPointsByMap(robotInfo.getROSIPAddress()),map )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pointsMap -> {
                    if (pointsMap == null || pointsMap.isEmpty()) {
                        view.onPointsLoadedFailed(context.getString(R.string.text_loaded_success_not_mark_point), checkChargingPile);
                        return;
                    }
                    List<Point> waypoints = pointsMap.get("waypoints");
                    if (waypoints == null || waypoints.isEmpty()) {
                        view.onPointsLoadedFailed(context.getString(R.string.text_loaded_success_not_mark_point), checkChargingPile);
                        return;
                    }
                    if (checkChargingPile) {
                        PointCacheInfo.INSTANCE.checkIsChargePointMarked(waypoints);
                    }else {
                        PointCacheInfo.INSTANCE.checkIsProductionPointMarked(waypoints);
                    }
                    view.onPointsLoadedSuccess(alias, map, checkChargingPile);
                }, throwable -> fetchPointFailed(context,throwable,checkChargingPile));
    }

    private void fetchPointFailed(Context context,Throwable throwable,boolean checkChargingPile){
        if (throwable instanceof RequiredPointsNotFoundException){
            StringBuilder stringBuilder = new StringBuilder(context.getString(R.string.text_apply_default_map_success));
            if (!((RequiredPointsNotFoundException) throwable).isChargePointMarked()) {
                stringBuilder.append(context.getString(R.string.text_not_mark_charge_point));
            } else {
                stringBuilder.append(context.getString(R.string.text_not_mark_product_point));
            }
            view.onPointsLoadedFailed(stringBuilder.toString(), checkChargingPile);
        } else {
            view.onPointsLoadedFailed(context.getString(R.string.text_point_loaded_failed_cannot_apply_map), checkChargingPile);
        }
    }
    @SuppressLint("CheckResult")
    @Override
    public void onLoadMaps(Context context, Pair<String, String> defaultMap, boolean checkChargingPile) {
        EasyDialog.getLoadingInstance(context).loading(context.getString(R.string.text_loading_map_list));
        String ipAddress = robotInfo.getROSIPAddress();
        ApiClient.INSTANCE.getApiService().fetchMapListAsync(Url.INSTANCE.getMapList(ipAddress))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mapListResponse -> {
                    if (mapListResponse == null || mapListResponse.size() == 0) {
                        view.onMapListLoadedFailed(context.getString(R.string.text_map_load_empty));
                        return;
                    }
                    List<MapVO> mapVOList = new ArrayList<>();
                    for (int i = 0; i < mapListResponse.size(); i++) {
                        if (TextUtils.isEmpty(mapListResponse.get(i).alias) || !mapListResponse.get(i).alias.matches("-?\\d+"))
                            continue;
                        if (
                                defaultMap != null
                                        && !TextUtils.isEmpty(mapListResponse.get(i).alias)
                                        && defaultMap.getFirst().equals(mapListResponse.get(i).alias)
                        ) {
                            mapVOList.add(new MapVO(mapListResponse.get(i).name, mapListResponse.get(i).alias, true));
                        } else {
                            mapVOList.add(new MapVO(mapListResponse.get(i).name, mapListResponse.get(i).alias, false));
                        }
                    }
                    if (mapVOList.isEmpty()) {
                        view.onMapListLoadedFailed(context.getString(R.string.text_map_load_illegal));
                        return;
                    }
                    view.onMapListLoadedSuccess(mapVOList, checkChargingPile);
                }, throwable -> {
                    Timber.e(throwable, "地图加载失败");
                    view.onMapListLoadedFailed(context.getString(R.string.text_map_load_failed));
                });
    }
}
