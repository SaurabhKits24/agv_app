package com.reeman.agv.presenter.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.reeman.agv.R;
import com.reeman.agv.calling.CallingInfo;
import com.reeman.agv.contract.QRCodeCallingConfigContract;
import com.reeman.agv.utils.PackageUtils;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.provider.SerialPortProvider;
import com.reeman.commons.utils.ByteUtil;
import com.reeman.commons.utils.FileMapUtils;
import com.reeman.serialport.controller.SerialPortParser;
import com.reeman.serialport.util.Parser;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Pair;
import timber.log.Timber;

public class QRCodeCallingConfigPresenter implements QRCodeCallingConfigContract.Presenter {

    private final QRCodeCallingConfigContract.View view;

    private final Context context;

    private SerialPortParser instance;

    private volatile boolean configMode = false;
    private final Pattern mPattern = Pattern.compile("AA55");

    public QRCodeCallingConfigPresenter(QRCodeCallingConfigContract.View view, Context context) {
        this.view = view;
        this.context = context;
    }

    @Override
    public void startListen() {
        String path = SerialPortProvider.ofCallModule(Build.PRODUCT);
        File file = new File(path);
        File[] files = file.listFiles();
        if (!file.exists() || files == null || files.length == 0) {
            view.showNotFoundSerialDevice();
            return;
        }
        File target = null;
        for (File temp : files) {
            if (temp.getName().startsWith("ttyUSB")) {
                target = temp;
                break;
            }
        }
        if (target == null) {
            view.showNotFoundSerialDevice();
            return;
        }
        try {
            instance = new SerialPortParser(new File("/dev/" + target.getName()), 115200, new SerialPortParser.OnDataResultListener() {
                private final StringBuilder sb = new StringBuilder();

                @Override
                public void onDataResult(byte[] bytes, int len) {
                    sb.append(ByteUtil.byteArr2HexString(bytes, len));
                    while (sb.length() != 0) {
                        if (sb.length() < 4) break;
                        Matcher matcher = mPattern.matcher(sb);
                        if (matcher.find()) {
                            try {
                                int start = matcher.start();
                                int startIndex = start + 4;

                                if (startIndex + 2 >= sb.length())
                                    break;

                                String dataSize = sb.substring(startIndex, startIndex + 2);
                                int intSize = ByteUtil.hexStringToInt(dataSize);

                                int dataLastIndex = startIndex + intSize * 2 + 2;

                                if (dataLastIndex + 2 > sb.length())
                                    break;

                                String dataHexSum = sb.substring(startIndex, dataLastIndex);
                                String checkSum = sb.substring(dataLastIndex, dataLastIndex + 2);
                                if (checkSum.equals(Parser.checkXor(dataHexSum))) {
                                    String key = sb.substring(dataLastIndex - 6, dataLastIndex);
                                    if (configMode) {
                                        view.bind(key);
                                    } else {
                                        keyPress(key);
                                    }
                                    sb.delete(0, dataLastIndex + 2);
                                } else if (matcher.find()) {
                                    Timber.w("数据解析失败1 %s", sb.toString());
                                    sb.delete(0, matcher.start());
                                } else {
                                    Timber.w("数据解析失败2 %s", sb.toString());
                                    sb.delete(0, sb.length());
                                }
                            } catch (Exception e) {
                                Timber.w(e, "数据解析错误 %s", sb.toString());
                                sb.delete(0, sb.length());
                            }
                        } else {
                            Timber.w("找不到协议头 %s", sb.toString());
                            sb.delete(0, sb.length());
                        }
                    }
                }
            });
            instance.start();
        } catch (Exception e) {
            view.showOpenSerialDeviceFailed();
        }
    }

    @Override
    public void stopListen() {
        if (instance != null) {
            instance.stop();
        }
    }


    @SuppressLint("CheckResult")
    private void keyPress(String key) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
                    String table = FileMapUtils.get(Environment.getExternalStorageDirectory() + File.separator + PackageUtils.getAppName(context) + File.separator + Constants.KEY_BUTTON_MAP_WITH_QRCODE_TASK_PATH, key);
                    List<Pair<Pair<String, String>, Pair<String, String>>> qrCodePointPairList = new Gson().fromJson(table, new TypeToken<List<Pair<Pair<String, String>, Pair<String, String>>>>() {
                    }.getType());
                    if (qrCodePointPairList == null || qrCodePointPairList.isEmpty()){
                        emitter.onNext("");
                        return;
                    }
                    StringBuilder task = new StringBuilder();
                    for (int i = 0; i < qrCodePointPairList.size(); i++) {
                        task.append(qrCodePointPairList.get(i).getFirst().getSecond()).append("-");
                        task.append(qrCodePointPairList.get(i).getSecond().getSecond());
                        if (i != qrCodePointPairList.size() - 1) {
                            task.append(",");
                        }
                    }
                    emitter.onNext(task.toString());
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(task -> view.showToast(context.getString(TextUtils.isEmpty(task) ? R.string.text_please_bind_key_first : R.string.text_key_pressed, task)),
                        throwable -> {
                            Timber.w(throwable, "查找按键对应的编号失败");
                            view.showToast(context.getString(R.string.text_get_key_failed, throwable.getMessage()));
                        });
    }

    @SuppressLint("CheckResult")
    @Override
    public void deleteAll() {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    FileMapUtils.clear(Environment.getExternalStorageDirectory() + File.separator + PackageUtils.getAppName(context) + File.separator + Constants.KEY_BUTTON_MAP_WITH_QRCODE_TASK_PATH);
                    CallingInfo.INSTANCE.getCallingButtonWithQRCodeModelTaskMap().clear();
                    emitter.onNext(true);
                    Timber.w("删除成功");
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> view.showToast(context.getString(R.string.text_delete_success)), throwable -> {
                    Timber.w(throwable, "删除按键失败");
                    view.showToast(context.getString(R.string.text_delete_failed, throwable.getMessage()));
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void deleteByKey(String key) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    FileMapUtils.deleteByKey(Environment.getExternalStorageDirectory() + File.separator + PackageUtils.getAppName(context) + File.separator +Constants.KEY_BUTTON_MAP_WITH_QRCODE_TASK_PATH, key);
                    CallingInfo.INSTANCE.getCallingButtonWithQRCodeModelTaskMap().remove(key);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> view.showToast(context.getString(R.string.text_delete_success)), throwable -> {
                    Timber.w(throwable, "删除按键失败");
                    view.showToast(context.getString(R.string.text_delete_failed, throwable.getMessage()));
                });
    }


    @Override
    public void exitConfigMode() {
        Timber.v("退出配置模式");
        configMode = false;
    }

    @Override
    public void enterConfigMode() {
        Timber.v("进入配置模式");
        configMode = true;
    }

}
