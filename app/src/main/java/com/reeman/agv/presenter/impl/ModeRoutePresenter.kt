package com.reeman.agv.presenter.impl

import com.reeman.agv.contract.ModeRouteContract
import com.reeman.dao.repository.DbRepository
import com.reeman.dao.repository.entities.RouteWithPoints
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class ModeRoutePresenter(private val view: ModeRouteContract.View) : ModeRouteContract.Presenter {
    override fun deleteRoute(routeWithPoints: RouteWithPoints) {
        DbRepository.getInstance().deleteRouteById(routeWithPoints.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {}
                override fun onSuccess(integer: Int) {
                    view.onDeleteRouteSuccess()
                }

                override fun onError(e: Throwable) {
                    Timber.w(e, "删除路线失败")
                    view.onDeleteRouteFailed(e)
                }
            })
    }

    override fun updateRoute(routeWithPoints: RouteWithPoints) {
        DbRepository.getInstance().updateRoute(routeWithPoints)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {}
                override fun onSuccess(integer: Int) {
                    view.onUpdateRouteSuccess()
                }

                override fun onError(e: Throwable) {
                    Timber.w(e, "更新路线失败")
                    view.onUpdateRouteFailed(e)
                }
            })
    }

    override fun addRoute(routeWithPoints: RouteWithPoints) {
        DbRepository.getInstance().addRoute(routeWithPoints)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Long> {
                override fun onSubscribe(d: Disposable) {}
                override fun onSuccess(aLong: Long) {
                    view.onAddRouteSuccess()
                }

                override fun onError(e: Throwable) {
                    Timber.w(e, "添加路线失败")
                    view.onAddRouteFailed(e)
                }
            })
    }

    override fun getAllRoute(navigationMode: Int) {
        DbRepository.getInstance().getAllRouteWithPoints(navigationMode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :SingleObserver<List<RouteWithPoints>>{
                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    Timber.w(e,"查询路线失败")
                    view.onGetAllRouteFailed(e)
                }

                override fun onSuccess(t: List<RouteWithPoints>) {
                    view.onGetAllRouteSuccess(t)
                }

            })
    }
}