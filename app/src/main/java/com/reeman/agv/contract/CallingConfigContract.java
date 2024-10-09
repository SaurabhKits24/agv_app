package com.reeman.agv.contract;

import android.content.Context;

import kotlin.Pair;

import com.reeman.points.model.custom.GenericPoint;
import com.reeman.points.model.custom.GenericPointsWithMap;
import com.reeman.points.model.request.MapVO;
import com.reeman.agv.presenter.IPresenter;
import com.reeman.agv.view.IView;

import java.util.List;

public interface CallingConfigContract {

    interface Presenter extends IPresenter{
        /**
         * 打开呼叫串口
         */
        void startListen();

        /**
         * 关闭串口
         */
        void stopListen();

        /**
         * 进入配置模式
         */
        void enterConfigMode();

        /**
         * 退出配置模式
         */
        void exitConfigMode();

        /**
         * 删除当前模式所有绑定数据
         */
        void deleteAll();

        /**
         * 根据按键删除绑定信息
         * @param position
         * @param key
         */
        void deleteByKey(int position,String key);

        /**
         * 获取呼叫模式点位
         * @param context
         */
        void refreshCallingModePoints(Context context);
    }

    interface View extends IView{

        void openSerialDeviceSuccess();
        /**
         * 找不到串口
         */
        void showNotFoundSerialDevice();

        /**
         * 打开串口设备失败
         */
        void showOpenSerialDeviceFailed();

        /**
         * 绑定按钮
         * @param key
         */
        void bind(String key);

        /**
         * 提示
         * @param msg
         */
        void showToast(String msg);

        /**
         * 呼叫模式-当前地图点位加载完成
         * @param pointList
         */
        void onCallingModePointsDataLoadSuccess(List<String> pointList);

        /**
         * 呼叫模式-所有地图点位加载成功
         * @param pointsWithMapList
         */
        void onCallingModeMapsWithPointsDataLoadSuccess(List<Pair<String,List<String>>> pointsWithMapList);

        /**
         * 数据加载失败
         * @param throwable
         */
        void onDataLoadFailed(Throwable throwable);

        void onDeleteByKeySuccess(int position);

        void onDeleteAllSuccess();
    }
}
