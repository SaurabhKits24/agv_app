package com.reeman.agv.contract;

import android.content.Context;

import com.reeman.agv.presenter.IPresenter;
import com.reeman.points.model.request.MapVO;
import com.reeman.agv.view.IView;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;


public interface BasicSettingContract {

    interface Presenter extends IPresenter {

        void relocate(double[] relocPosition);

        void tryListen(Context context, String dir, String prompt, String type, android.view.View btnTryListen, android.view.View btnSave);

        void onSwitchMap(Context context);

        void refreshChargePoint(Context context);

        void loadBackgroundMusic(Context context, String ip);
    }

    interface View extends IView {

        void showRelocatingView();

        void onSynthesizeStart(android.view.View btnTryListen, android.view.View btnSave);

        void onSynthesizeEnd(android.view.View btnTryListen, android.view.View btnSave);

        void onSynthesizeError(String message, android.view.View btnTryListen, android.view.View btnSave);

        void onMapListLoaded(List<MapVO> list);

        void onMapListLoadedFailed(Throwable throwable);

        /**
         * 充电模式-点位加载成功
         */
        void onChargeDataLoadSuccess();

        void onDataLoadFailed(Throwable throwable);

        void onMusicListLoaded(@NonNull List<String> music, String ip);

        void onMusicListFailed(Throwable e);
    }
}
