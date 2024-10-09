package com.reeman.agv.base;

import static com.reeman.agv.base.BaseApplication.ros;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.reeman.agv.R;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.exceptions.ClickFastException;
import com.reeman.commons.state.RobotInfo;
import com.reeman.commons.utils.LocaleUtil;
import com.reeman.commons.utils.SpManager;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.ros.callback.ROSCallback;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public abstract class BaseFragment extends Fragment implements View.OnClickListener, ROSCallback {

    protected ViewGroup root;

    protected Map<Integer, View> views;

    private long lastClickTimeMills;

    protected RobotInfo robotInfo;

    private Disposable disposable;

    protected final Handler mHandler = new Handler();

    public boolean restrictFrequency(long interval) {
        long currentTimeMillis = SystemClock.uptimeMillis();
        if (currentTimeMillis - lastClickTimeMills < interval) {
            lastClickTimeMills = currentTimeMillis;
            return true;
        }
        lastClickTimeMills = currentTimeMillis;
        return false;
    }

    protected void updateLastClickTimeMills() {
        lastClickTimeMills = SystemClock.uptimeMillis();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int languageType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
        Timber.tag(getClass().getSimpleName()).w("语言 %d", languageType);
        LocaleUtil.changeAppLanguage(getResources(), languageType);
        views = new HashMap<>();
        robotInfo = RobotInfo.INSTANCE;
        Log.w(this.getClass().getSimpleName(), " onCreate");
        if (ros != null) {
            ros.registerListener(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.w(this.getClass().getSimpleName(), " onCreateView");
        root = (ViewGroup) LayoutInflater.from(getContext()).inflate(getLayoutRes(), container, false);
        return root;
    }

    protected abstract @LayoutRes
    int getLayoutRes();

    public <T extends View> T $(@IdRes int id) {
        View view = views.get(id);
        if (view != null) return (T) view;
        View targetView = root.findViewById(id);
        views.put(id, targetView);
        return (T) targetView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.w(this.getClass().getSimpleName(), " onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.w(this.getClass().getSimpleName(), " onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w(this.getClass().getSimpleName(), " onResume");
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.w(this.getClass().getSimpleName(), " onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w(this.getClass().getSimpleName(), " onStop");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.w(this.getClass().getSimpleName(), " onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(this.getClass().getSimpleName(), " onDestroy");
        if (ros != null) {
            ros.unregisterListener(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.w(this.getClass().getSimpleName(), " onDetach");
    }

    protected void setOnClickListeners(int... ids) {
        for (int id : ids) {
            View view = views.get(id);
            if (view == null) {
                view = root.findViewById(id);
                views.put(id, view);
            }
            if (view != null) view.setOnClickListener(this);
        }
    }

    protected void setOnClickListeners(ViewGroup viewGroup, int... ids) {
        for (int id : ids) {
            View view = views.get(id);
            if (view == null) {
                view = viewGroup.findViewById(id);
                views.put(id, view);
            }
            if (view != null) view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        destroyClickEvent();
        onCustomClick(view);
    }

    @SuppressLint("CheckResult")
    private void onCustomClick(View view) {
        disposable = Observable.create(emitter -> {
                    if (restrictFrequency(400)) {
                        emitter.onError(new ClickFastException());
                    }
                    if (view instanceof TextView)
                        Timber.tag(getClass().getSimpleName()).w("点击[" + ((TextView) view).getText().toString() + "]");
                    int id = view.getId();
                    emitter.onNext(id);
                })
                .debounce(150, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(eventId -> {
                    updateLastClickTimeMills();
                    int mId = (int) eventId;
                    onCustomClickResult(mId, view);
                }, throwable -> {
                    updateLastClickTimeMills();
                    Timber.tag(getClass().getSimpleName()).w(throwable, "点击事件");
                    if (throwable instanceof ClickFastException) {
                        ToastUtils.showShortToast(getString(R.string.text_click_too_fast));
                    } else {
                        throw throwable;
                    }
                });
    }

    protected void onCustomClickResult(int id) {

    }

    protected void onCustomClickResult(int id, View v) {
        onCustomClickResult(id);
    }

    private void destroyClickEvent() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    protected float onFloatValueChange(View v, boolean isAdd) {
        if (v instanceof IndicatorSeekBar) return ((IndicatorSeekBar) v).getProgressFloat();
        ViewGroup parent = (ViewGroup) v.getParent();
        IndicatorSeekBar seekBar = (IndicatorSeekBar) parent.getChildAt(1);
        float progress = seekBar.getProgressFloat();
        progress = (float) (isAdd ? progress + 0.1 : progress - 0.1);
        seekBar.setProgress(progress);
        return seekBar.getProgressFloat();
    }

    protected int onIntValueChange(View v, boolean isAdd) {
        if (v instanceof IndicatorSeekBar) return ((IndicatorSeekBar) v).getProgress();
        ViewGroup parent = (ViewGroup) v.getParent();
        IndicatorSeekBar seekBar = (IndicatorSeekBar) parent.getChildAt(1);
        float progress = seekBar.getProgressFloat();
        progress = isAdd ? progress + 1 : progress - 1;
        seekBar.setProgress(progress);
        return seekBar.getProgress();
    }
}
