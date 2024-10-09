package com.reeman.agv.contract;

import com.reeman.agv.presenter.IPresenter;
import com.reeman.agv.view.IView;

public interface QRCodeCallingConfigContract {

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
         * 删除当前模式(梯控)所有绑定数据
         */
        void deleteAll();

        /**
         * 根据key删除
         */
        void deleteByKey(String key);

    }

    interface View extends IView{
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
    }
}
