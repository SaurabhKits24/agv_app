package com.reeman.agv.contract;

import android.app.Activity;

import com.reeman.agv.presenter.IPresenter;
import com.reeman.agv.view.IView;


public interface SplashContract {
    interface Presenter extends IPresenter {

        void startup(Activity activity);
    }

    interface View extends IView {

    }
}
