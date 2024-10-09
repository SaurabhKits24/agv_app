package com.reeman.agv.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.reeman.agv.BuildConfig
import com.reeman.agv.base.BaseApplication
import com.reeman.agv.calling.utils.CallingStateManager
import com.reeman.agv.request.ServiceFactory
import com.reeman.agv.request.url.API
import com.reeman.agv.utils.TimeSettingUtils.disableAutoTime
import com.reeman.agv.utils.TimeSettingUtils.setCurrentTimeMillis
import com.reeman.commons.event.TimeStampEvent
import com.reeman.commons.eventbus.EventBus
import com.reeman.commons.model.request.ResponseWithTime
import com.reeman.commons.state.RobotInfo
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class RobotReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        action?.apply {
            Log.w("action", this)
        }
        when (action) {
            Intent.ACTION_TIME_TICK -> {
                val currentTimeMillis = System.currentTimeMillis()
                if (BuildConfig.IS3128 && (!RobotInfo.isTimeSynchronized || currentTimeMillis < RobotInfo.lastSynchronizedTimestamp)) {
                    ServiceFactory.getRobotService().getServerTime(API.getServerTimeAPI())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(object : Observer<ResponseWithTime> {
                            override fun onSubscribe(d: Disposable) {}
                            override fun onNext(responseWithTime: ResponseWithTime) {
                                Timber.d(responseWithTime.toString())
                                if (responseWithTime.code == 0) {
                                    disableAutoTime(BaseApplication.mApp.contentResolver)
                                    setCurrentTimeMillis(responseWithTime.data)
                                }
                                RobotInfo.isTimeSynchronized = true
                                RobotInfo.lastSynchronizedTimestamp = responseWithTime.data
                                CallingStateManager.setTimeTickEvent(responseWithTime.data)
                                EventBus.sendEvent(TimeStampEvent(responseWithTime.data))
                            }

                            override fun onError(e: Throwable) {
                                Timber.d(e, "获取时间服务器时间失败")
                                CallingStateManager.setTimeTickEvent(currentTimeMillis)
                                EventBus.sendEvent(TimeStampEvent(currentTimeMillis))
                            }

                            override fun onComplete() {}
                        })
                } else {
                    CallingStateManager.setTimeTickEvent(currentTimeMillis)
                    EventBus.sendEvent(TimeStampEvent(currentTimeMillis))
                }
            }

            "android.net.conn.CONNECTIVITY_CHANGE" -> {}
        }
    }

    class RobotIntentFilter : IntentFilter() {
        init {
            addAction(Intent.ACTION_TIME_TICK)
            addAction("android.net.conn.CONNECTIVITY_CHANGE")
        }
    }
}