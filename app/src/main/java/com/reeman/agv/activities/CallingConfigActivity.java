package com.reeman.agv.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import kotlin.Pair;

import com.google.gson.reflect.TypeToken;
import com.reeman.agv.R;
import com.reeman.agv.adapter.CallingModeSelectedPointItemAdapter;
import com.reeman.agv.base.BaseActivity;
import com.reeman.agv.constants.Errors;
import com.reeman.agv.widgets.CallingTaskChoosePointDialog;
import com.reeman.agv.widgets.CallingTaskChoosePointWithMapDialog;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.utils.FileMapUtils;
import com.reeman.agv.contract.CallingConfigContract;
import com.reeman.agv.presenter.impl.CallingConfigPresenter;
import com.reeman.agv.utils.PackageUtils;
import com.reeman.agv.utils.ScreenUtils;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.widgets.CustomProgressDialog;
import com.reeman.agv.widgets.EasyDialog;

import java.io.File;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class CallingConfigActivity extends BaseActivity implements CallingConfigContract.View, CallingModeSelectedPointItemAdapter.OnDeleteClickListener {

    private CallingConfigPresenter presenter;
    private TextView tvSelectedPoint;

    private RecyclerView rvAlreadyBindingPoint;
    private CallingModeSelectedPointItemAdapter callingModeSelectedPointItemAdapter;
    private CustomProgressDialog progressDialog;


    @Override
    protected int getLayoutRes() {
        return R.layout.activity_calling_config;
    }

    @Override
    protected void initCustomView() {
        tvSelectedPoint = $(R.id.tv_selected_point);
        $(R.id.btn_select_point).setOnClickListener(this);
        $(R.id.btn_bind).setOnClickListener(this);
        $(R.id.btn_delete_all).setOnClickListener(this);
        $(R.id.btn_exit).setOnClickListener(this);
        rvAlreadyBindingPoint = $(R.id.rv_already_binding_point);

    }

    @Override
    protected void initData() {
        presenter = new CallingConfigPresenter(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.startListen();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
    }

    @Override
    protected void onCustomClickResult(int id) {
        super.onCustomClickResult(id);
        switch (id) {
            case R.id.btn_select_point:
                presenter.refreshCallingModePoints(this);
                break;
            case R.id.btn_bind:
                startBind();
                break;
            case R.id.btn_delete_all:
                presenter.deleteAll();
                break;
            case R.id.btn_exit:
                exit();
                break;
        }
    }

    @Override
    public void openSerialDeviceSuccess() {
        List<Pair<String, Pair<String, String>>> pointBoundList = callingInfo.getCallingButtonList();
        if (robotInfo.isElevatorMode()) {
            pointBoundList= callingInfo.getCallingButtonWithElevatorList();
        }
        callingModeSelectedPointItemAdapter = new CallingModeSelectedPointItemAdapter(pointBoundList, this);
        rvAlreadyBindingPoint.setAdapter(callingModeSelectedPointItemAdapter);
        rvAlreadyBindingPoint.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            ScreenUtils.hideBottomUIMenu(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stopListen();
        updateCallingMap();
    }

    @Override
    public void showToast(String msg) {
        ToastUtils.showShortToast(msg);
    }

    @Override
    public void onCallingModePointsDataLoadSuccess(List<String> pointList) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        new CallingTaskChoosePointDialog(this, pointList, null, points -> {
            if (points != null) {
                tvSelectedPoint.setText(getString(R.string.text_current_point, points.getSecond()));
                tvSelectedPoint.setTag(points.getSecond());
            } else {
                tvSelectedPoint.setText(getString(R.string.text_not_select_point));
                tvSelectedPoint.setTag(null);
            }
        }).show();
    }

    @Override
    public void onCallingModeMapsWithPointsDataLoadSuccess(List<Pair<String, List<String>>> pointsWithMapList) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        new CallingTaskChoosePointWithMapDialog(this, pointsWithMapList, null, points -> {
            if (points != null) {
                tvSelectedPoint.setText(getString(R.string.text_current_point, points.getFirst() + " - " + points.getSecond()));
                tvSelectedPoint.setTag(gson.toJson(new Pair<>(points.getFirst(), points.getSecond())));
            } else {
                tvSelectedPoint.setText(getString(R.string.text_not_select_point));
                tvSelectedPoint.setTag(null);
            }
        }).show();
    }

    @Override
    public void onDataLoadFailed(Throwable throwable) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        EasyDialog.getInstance(this).warnError(Errors.INSTANCE.getDataLoadFailedTip(this, throwable));
    }

    @Override
    public void showNotFoundSerialDevice() {
        Timber.v("找不到呼叫模块串口");
        EasyDialog.getInstance(this).warn(getString(R.string.text_not_found_serial_device), (dialog, id) -> {
            dialog.dismiss();
            finish();
        });
    }

    @Override
    public void showOpenSerialDeviceFailed() {
        Timber.v("打开呼叫模块串口失败");
        EasyDialog.getInstance(this).warn(getString(R.string.text_open_serial_device_failed), (dialog, id) -> {
            dialog.dismiss();
            finish();
        });
    }

    public void startBind() {
        Object tag = tvSelectedPoint.getTag();
        if (tag == null){
            EasyDialog.getInstance(this).warnError(getString(R.string.text_please_select_point));
            return;
        }
        String s = tag.toString();
        if (TextUtils.isEmpty(s)) {
            EasyDialog.getInstance(this).warnError(getString(R.string.text_please_select_point));
            return;
        }
        presenter.enterConfigMode();
        progressDialog = new CustomProgressDialog(this);
        progressDialog.setMessage(getString(R.string.text_waiting_for_key_pressed));
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.text_cancel), (dialog, which) -> {
            dialog.dismiss();
            presenter.exitConfigMode();
        });
        progressDialog.show();
    }

    @SuppressLint("CheckResult")
    @Override
    public void bind(String key) {
        Observable.create((ObservableOnSubscribe<Pair<String, Pair<String, String>>>) emitter -> {
                    String name = Environment.getExternalStorageDirectory() + File.separator + PackageUtils.getAppName(CallingConfigActivity.this) + File.separator + (robotInfo.isElevatorMode() ? Constants.KEY_BUTTON_MAP_WITH_ELEVATOR_PATH : Constants.KEY_BUTTON_MAP_PATH);
                    String newNum = tvSelectedPoint.getTag().toString();
                    if (TextUtils.isEmpty(FileMapUtils.get(name, key))) {
                        FileMapUtils.put(name, key, newNum);
                    } else {
                        Timber.w("replace ");
                        FileMapUtils.replace(name, key, newNum);
                    }
                    Timber.w("绑定呼叫按钮 : key : %s , point : %s", key, newNum);
                    presenter.exitConfigMode();
                    Pair<String, String> pointInfo;
                    if (robotInfo.isElevatorMode()) {
                        pointInfo = gson.fromJson(newNum, new TypeToken<Pair<String, String>>() {
                        }.getType());
                        callingInfo.getCallingButtonMapWithElevator().put(key, pointInfo);
                    } else {
                        pointInfo = new Pair<>("", newNum);
                        callingInfo.getCallingButtonMap().put(key, newNum);
                    }
                    emitter.onNext(new Pair<>(key, pointInfo));
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(pointInfoPair -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    callingModeSelectedPointItemAdapter.addItem(pointInfoPair);
                    ToastUtils.showShortToast(getString(R.string.text_bind_success));
                }, throwable -> {
                    Timber.w(throwable, "绑定呼叫按钮失败");
                    presenter.exitConfigMode();
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    ToastUtils.showShortToast(getString(R.string.text_bind_failed, throwable.getMessage()));
                });
    }

    @Override
    public void onDeleteAllSuccess() {
        ToastUtils.showShortToast(getString(R.string.text_delete_success));
        callingModeSelectedPointItemAdapter.removeAll();
    }

    @Override
    public void onDeleteByKeySuccess(int position) {
        ToastUtils.showShortToast(getString(R.string.text_delete_success));
        callingModeSelectedPointItemAdapter.removeItem(position);
    }

    public void exit() {
        finish();
        if (EasyDialog.isShow())
            EasyDialog.getInstance().dismiss();
    }

    @Override
    public void onDeleteClick(int position, Pair<String, Pair<String, String>> item) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        EasyDialog.getInstance(this).confirm(getString(R.string.text_confirm_delete_bound_info_item, item.getFirst()), new EasyDialog.OnViewClickListener() {
            @Override
            public void onViewClick(Dialog dialog, int id) {
                dialog.dismiss();
                if (id == R.id.btn_confirm) {
                    presenter.deleteByKey(position, item.getFirst());
                }
            }
        });
    }
}