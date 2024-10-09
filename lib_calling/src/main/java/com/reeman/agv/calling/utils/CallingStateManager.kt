package com.reeman.agv.calling.utils

import android.util.Log
import com.reeman.agv.calling.event.CallingButtonEvent
import com.reeman.agv.calling.event.QRCodeButtonEvent
import com.reeman.agv.calling.event.StartTaskCountDownEvent
import com.reeman.agv.calling.event.UnboundButtonEvent
import com.reeman.commons.event.CoreDataEvent
import com.reeman.commons.event.InitiativeLiftingModuleStateEvent
import io.reactivex.rxjava3.subjects.PublishSubject

object CallingStateManager {
    private val TAG = this::class.java.simpleName

    private val eventCoreDataSubject = PublishSubject.create<CoreDataEvent>()
    private val eventMappingModeSubject = PublishSubject.create<Int>()
    private val eventSelfCheckSubject = PublishSubject.create<Boolean>()
    private val eventTaskExecutingSubject = PublishSubject.create<Int>()
    private val eventLowPowerSubject = PublishSubject.create<Boolean>()
    private val eventInitiativeLiftingModuleStateSubject = PublishSubject.create<InitiativeLiftingModuleStateEvent>()
    private val eventCountingDownAfterCallingTaskSubject = PublishSubject.create<Boolean>()
    private val eventCurrentActivityCanTakeRemoteTaskSubject = PublishSubject.create<Boolean>()
    private val eventCanTakeTaskDuringTaskExecutingSubject = PublishSubject.create<Boolean>()
    private val eventStartTaskCountDownSubject = PublishSubject.create<StartTaskCountDownEvent>()
    private val eventCallingButtonSubject = PublishSubject.create<CallingButtonEvent>()
    private val eventQRCodeButtonSubject = PublishSubject.create<QRCodeButtonEvent>()
    private val eventUnboundButtonSubject = PublishSubject.create<UnboundButtonEvent>()
    private val eventTimeTickSubject = PublishSubject.create<Long>()

    fun setStartTaskCountDownEvent(event:StartTaskCountDownEvent){
        eventStartTaskCountDownSubject.onNext(event)
    }

    fun getStartTaskCountDownEvent() = eventStartTaskCountDownSubject

    fun setTimeTickEvent(timeStamp:Long){
        eventTimeTickSubject.onNext(timeStamp)
    }

    fun getTimeTickEvent() = eventTimeTickSubject

    fun setUnboundButtonEvent(event: UnboundButtonEvent){
        eventUnboundButtonSubject.onNext(event)
    }

    fun getUnboundButtonEvent()=  eventUnboundButtonSubject

    fun setCallingButtonEvent(event:CallingButtonEvent){
        eventCallingButtonSubject.onNext(event)
    }

    fun getCallingButtonEvent() = eventCallingButtonSubject

    fun setQRCodeButtonEvent(event: QRCodeButtonEvent){
        eventQRCodeButtonSubject.onNext(event)
    }

    fun getQRCodeButtonEvent() = eventQRCodeButtonSubject
    fun setSelfCheckEvent(state: Boolean) {
        Log.e(TAG,"setSelfCheckEvent : $state")
        eventSelfCheckSubject.onNext(state)
    }

    fun getStateSelfCheck() = eventSelfCheckSubject

    fun setMappingModeEvent(state: Int) {
        Log.e(TAG,"setMappingModeEvent : $state")
        eventMappingModeSubject.onNext(state)
    }

    fun getMappingModeEvent() = eventMappingModeSubject

    fun setCoreDataEvent(event: CoreDataEvent) {
        Log.e(TAG,"setCoreDataEvent")
        eventCoreDataSubject.onNext(event)
    }

    fun getCoreDataEvent() = eventCoreDataSubject

    fun setTaskExecutingEvent(state: Int) {
        Log.e(TAG,"setTaskExecutingEvent : $state")
        eventTaskExecutingSubject.onNext(state)
    }

    fun getTaskExecutingEvent() = eventTaskExecutingSubject

    fun setLowPowerEvent(state: Boolean) {
        Log.e(TAG,"setLowPowerEvent : $state")
        eventLowPowerSubject.onNext(state)
    }

    fun getLowPowerEvent() = eventLowPowerSubject

    fun setInitiativeLiftingModuleStateEvent(event: InitiativeLiftingModuleStateEvent) {
        Log.e(TAG,"setInitiativeLiftingModuleStateEvent")
        eventInitiativeLiftingModuleStateSubject.onNext(event)
    }

    fun getInitiativeLiftingModuleStateEvent() = eventInitiativeLiftingModuleStateSubject

    fun setCountingDownAfterCallingTaskEvent(state: Boolean) {
        Log.e(TAG,"setCountingDownAfterCallingTaskEvent : $state")
        eventCountingDownAfterCallingTaskSubject.onNext(state)
    }

    fun getCountingDownAfterCallingTaskEvent() = eventCountingDownAfterCallingTaskSubject

    fun setCurrentActivityCanTakeRemoteTaskEvent(state: Boolean) {
        Log.e(TAG,"setCurrentActivityCanTakeRemoteTaskEvent : $state")
        eventCurrentActivityCanTakeRemoteTaskSubject.onNext(state)
    }

    fun getCurrentActivityCanTakeRemoteTaskEvent() = eventCurrentActivityCanTakeRemoteTaskSubject

    fun setCanTakeTaskDuringTaskExecuting(state: Boolean) {
        Log.e(TAG,"setCanTakeTaskDuringTaskExecuting : $state")
        eventCanTakeTaskDuringTaskExecutingSubject.onNext(state)
    }

    fun getCanTakeTaskDuringTaskExecuting() = eventCanTakeTaskDuringTaskExecutingSubject
}
