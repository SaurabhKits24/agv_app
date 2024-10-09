package com.reeman.agv.presenter.impl

import android.app.Activity
import com.reeman.agv.BuildConfig
import com.reeman.agv.activities.AliasSettingActivity
import com.reeman.agv.activities.LanguageSelectActivity
import com.reeman.agv.activities.MainActivity
import com.reeman.agv.activities.WiFiConnectActivity
import com.reeman.agv.base.BaseActivity
import com.reeman.agv.contract.SplashContract
import com.reeman.agv.request.ServiceFactory
import com.reeman.agv.request.url.API
import com.reeman.agv.utils.TimeSettingUtils
import com.reeman.commons.constants.Constants
import com.reeman.commons.model.request.ResponseWithTime
import com.reeman.commons.state.RobotInfo
import com.reeman.commons.utils.SpManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class SplashPresenter(private val view: SplashContract.View) : SplashContract.Presenter {
    override fun startup(context: Activity) {

        fun startMainActivity() {
            BaseActivity.startup(context, MainActivity::class.java)
            context.finish()
        }

        if (SpManager.getInstance().getBoolean(Constants.KEY_IS_LANGUAGE_CHOSEN, false)) {
            if (SpManager.getInstance().getBoolean(Constants.KEY_IS_NETWORK_GUIDE, false)) {
                if (SpManager.getInstance().getBoolean(Constants.KEY_IS_ALIAS_GUIDE, false)) {
                    if (BuildConfig.IS3128) {
                        ServiceFactory.getRobotService().getServerTime(API.getServerTimeAPI())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally {
                                startMainActivity()
                            }
                            .subscribe(object : Observer<ResponseWithTime> {
                                override fun onSubscribe(d: Disposable) {}
                                override fun onNext(responseWithTime: ResponseWithTime) {
                                    Timber.d(responseWithTime.toString())
                                    if (responseWithTime.code == 0) {
                                        TimeSettingUtils.disableAutoTime(context.contentResolver)
                                        TimeSettingUtils.setCurrentTimeMillis(responseWithTime.data)
                                    }
                                    RobotInfo.isTimeSynchronized = true
                                    RobotInfo.lastSynchronizedTimestamp = responseWithTime.data

                                }

                                override fun onError(e: Throwable) {
                                    Timber.d(e, "获取时间服务器时间失败")
                                }

                                override fun onComplete() {}
                            })
                    } else {
                        startMainActivity()
                    }
                    return
                }
                BaseActivity.startup(context, AliasSettingActivity::class.java)
                context.finish()
                return
            }
            BaseActivity.startup(context, WiFiConnectActivity::class.java)
            context.finish()
            return
        }
        BaseActivity.startup(context, LanguageSelectActivity::class.java)
        context.finish()
    }
}