@file:Suppress("UNCHECKED_CAST")

package com.reeman.commons.eventbus

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

object EventBus {
    private val TAG = this::class.java.simpleName
    private val disposablesMap: MutableMap<Any, CompositeDisposable> = HashMap()
    private val subjectMap: MutableMap<Class<*>, Subject<*>?> = HashMap()


    fun <T> sendEvent(eventType: Class<T>, event: T) {
        Log.d(TAG, "publish event : $eventType")
        val subject = getSubject(eventType)
        subject.onNext(event)
    }

    inline fun <reified T> sendEvent(event: T) {
        sendEvent(T::class.java, event)
    }

    fun <T> registerObserver(
        subscriber: Any,
        eventType: Class<T>,
        observer: EventObserver<T>,
        subscribeOnScheduler: Scheduler,
        observeOnScheduler: Scheduler
    ) {
        val subject = getSubject(eventType)
        val disposable = subject
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
            .subscribe { event: T -> observer.onEvent(event) }
        var compositeDisposable = disposablesMap[subscriber]
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
            disposablesMap[subscriber] = compositeDisposable
        }
        Log.d(
            TAG,
            "registerObserver,eventType : $eventType , subscriber : ${subscriber::class.java.simpleName}"
        )
        compositeDisposable.add(disposable)
    }

    inline fun <reified T> registerObserver(
        subscriber: Any,
        observer: EventObserver<T>,
        subscribeOnScheduler: Scheduler = Schedulers.io(),
        observeOnScheduler: Scheduler = AndroidSchedulers.mainThread()
    ) {
        registerObserver(
            subscriber,
            T::class.java,
            observer,
            subscribeOnScheduler,
            observeOnScheduler
        )
    }

    fun unregisterAll(subscriber: Any) {
        Log.d(TAG, "unregister : ${subscriber::class.java.simpleName}")

        val compositeDisposable = disposablesMap[subscriber]
        compositeDisposable?.let {
            it.clear()
            disposablesMap.remove(subscriber)
        }
    }

    private fun <T> getSubject(eventType: Class<T>): Subject<T> {
        synchronized(subjectMap) {
            var subject = subjectMap[eventType] as Subject<T>?
            if (subject == null) {
                subject = PublishSubject.create()
                subjectMap[eventType] = subject
            }
            return subject
        }
    }


    interface EventObserver<T> {
        fun onEvent(event: T)
    }

}