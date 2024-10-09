package com.reeman.agv.presenter.impl;


import static com.reeman.agv.base.BaseApplication.ros;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.reeman.agv.R;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.state.NavigationMode;
import com.reeman.agv.contract.BasicSettingContract;
import com.reeman.agv.request.ServiceFactory;
import com.reeman.points.model.request.MapVO;
import com.reeman.agv.request.service.RobotService;
import com.reeman.commons.state.RobotInfo;
import com.reeman.commons.utils.AESUtil;
import com.reeman.commons.utils.LocaleUtil;
import com.reeman.commons.utils.MMKVManager;
import com.reeman.commons.utils.SpManager;
import com.reeman.agv.widgets.EasyDialog;
import com.reeman.points.model.custom.GenericPoint;
import com.reeman.points.model.custom.GenericPointsWithMap;
import com.reeman.points.process.PointRefreshProcessingStrategy;
import com.reeman.points.process.PointRefreshProcessor;
import com.reeman.points.process.callback.RefreshPointDataCallback;
import com.reeman.points.process.impl.DeliveryPointsRefreshProcessingStrategy;
import com.reeman.points.process.impl.DeliveryPointsWithMapsRefreshProcessingStrategy;
import com.reeman.points.process.impl.FixedDeliveryPointsRefreshProcessingStrategy;
import com.reeman.points.process.impl.FixedDeliveryPointsWithMapsRefreshProcessingStrategy;
import com.reeman.points.request.ApiClient;
import com.reeman.points.request.url.Url;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class BasicSettingPresenter implements BasicSettingContract.Presenter {
    private final BasicSettingContract.View view;

    private final RobotInfo robotInfo;

    public BasicSettingPresenter(BasicSettingContract.View view) {
        this.view = view;
        robotInfo = RobotInfo.INSTANCE;

    }

    @Override
    public void relocate(double[] relocPosition) {
        ros.relocateByCoordinate(relocPosition);
        view.showRelocatingView();
    }

    private String lastTryListenText;
    private String lastGeneratedFile;

    public String getLastTryListenText() {
        return lastTryListenText;
    }

    public String getLastGeneratedFile() {
        return lastGeneratedFile;
    }


    @SuppressLint("CheckResult")
    @Override
    public void onSwitchMap(Context context) {
        EasyDialog.getLoadingInstance(context).loading(context.getString(R.string.text_loading_map_list));
        String ipAddress = robotInfo.getROSIPAddress();
        ApiClient.INSTANCE.getApiService().fetchMapListAsync(Url.INSTANCE.getMapList(ipAddress))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mapListResponse -> {
                    if (mapListResponse == null || mapListResponse.size() == 0) {
                        view.onMapListLoaded(Collections.emptyList());
                        return;
                    }
                    List<MapVO> mapVOList = new ArrayList<>();
                    for (int i = 0; i < mapListResponse.size(); i++) {
                        mapVOList.add(new MapVO(mapListResponse.get(i).name, mapListResponse.get(i).alias, false));
                    }
                    view.onMapListLoaded(mapVOList);
                }, throwable -> {
                    Timber.w(throwable,"地图加载失败");
                    view.onMapListLoadedFailed(throwable);
                });
    }

    @Override
    public void loadBackgroundMusic(Context context, String ip) {
        Timber.w("获取音乐地址为:%s",ip);
        EasyDialog.getLoadingInstance(context).loading(context.getString(R.string.text_loading_background_music));
        String path;
        path = "http://" + ip + "/file_list/delivery";
        RobotService robotService = ServiceFactory.getRobotService();
        robotService.fetchMusic(path)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<String> music) {
                        view.onMusicListLoaded(music,ip);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        view.onMusicListFailed(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    @Override
    public void tryListen(Context context, String dir, String text, String type, View btnTryListen, View btnSave) {
        this.lastTryListenText = text;
        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                        emitter.onNext(1);
                        int localeType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
                        if (localeType == -1) localeType = LocaleUtil.getLocaleType();
                        String language = LocaleUtil.getLanguage(localeType);
                        String voice = LocaleUtil.getVoice(localeType);
                        SpeechConfig speechConfig = SpeechConfig.fromSubscription(Constants.DEFAULT_SUBSCRIPTION_KEY, "eastasia");
                        speechConfig.setSpeechSynthesisLanguage(language);
                        speechConfig.setSpeechSynthesisVoiceName(voice);
                        speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Riff24Khz16BitMonoPcm);
                        AudioConfig audioConfig = AudioConfig.fromDefaultSpeakerOutput();
                        SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, audioConfig);
                        SpeechSynthesisResult result = synthesizer.SpeakText(text);
                        ResultReason reason = result.getReason();
                        if (!reason.equals(ResultReason.SynthesizingAudioCompleted)) {
                            emitter.onError(new RuntimeException(reason.toString()));
                            return;
                        }
                        byte[] audioData = result.getAudioData();
                        try {
                            File root = new File(context.getFilesDir() + dir + "/" + language + "/" + type);
                            if (!root.exists()) root.mkdirs();
                            File targetFile = new File(root, System.currentTimeMillis() + ".wav");
                            lastGeneratedFile = targetFile.getAbsolutePath();
                            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                            bufferedOutputStream.write(audioData, 0, audioData.length);
                            bufferedOutputStream.flush();
                            bufferedOutputStream.close();
                            emitter.onNext(2);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Integer o) {
                        if (o == 1) {
                            view.onSynthesizeStart(btnTryListen, btnSave);
                        } else {
                            view.onSynthesizeEnd(btnTryListen, btnSave);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        view.onSynthesizeError(e.getMessage(), btnTryListen, btnSave);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void refreshChargePoint(Context context) {
        PointRefreshProcessingStrategy processingStrategy;
        if (robotInfo.isElevatorMode()) {
            if (robotInfo.getNavigationMode() == NavigationMode.autoPathMode) {
                processingStrategy = new DeliveryPointsWithMapsRefreshProcessingStrategy();
            } else {
                processingStrategy = new FixedDeliveryPointsWithMapsRefreshProcessingStrategy();
            }
        } else {
            if (robotInfo.getNavigationMode() == NavigationMode.autoPathMode) {
                processingStrategy = new DeliveryPointsRefreshProcessingStrategy();
            } else {
                processingStrategy = new FixedDeliveryPointsRefreshProcessingStrategy();
            }
        }
        EasyDialog.getLoadingInstance(context).loading(context.getString(R.string.text_init_charging_point));
        new PointRefreshProcessor(processingStrategy, new RefreshPointDataCallback() {

            @Override
            public void onPointsLoadSuccess(@androidx.annotation.NonNull List<GenericPoint> pointList) {
                view.onChargeDataLoadSuccess();
            }

            @Override
            public void onPointsWithMapsLoadSuccess(@androidx.annotation.NonNull List<GenericPointsWithMap> pointsWithMapList) {
                view.onChargeDataLoadSuccess();
            }

            @Override
            public void onThrowable(Throwable throwable) {
                Timber.w(throwable, "拉取充电桩失败");
                view.onDataLoadFailed(throwable);
            }
        }).process( robotInfo.getROSIPAddress(),false,robotInfo.supportEnterElevatorPoint(), Arrays.asList(GenericPoint.CHARGE));
    }
}
