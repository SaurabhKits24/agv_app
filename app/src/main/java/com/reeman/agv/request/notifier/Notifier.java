package com.reeman.agv.request.notifier;

import com.google.gson.Gson;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.utils.MMKVManager;
import com.reeman.agv.request.ServiceFactory;
import com.reeman.commons.model.request.Msg;
import com.reeman.agv.request.service.RobotService;
import com.reeman.agv.request.url.API;
import com.reeman.commons.utils.AESUtil;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class Notifier {

    public static void notify(Msg msg) {
        try {
            RobotService robotService = ServiceFactory.getRobotService();
            robotService.notify(API.notifyAPI(), getMap(msg)).subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(map1 -> {
                        Timber.w( "上传成功");
                    }, throwable -> {
                        Timber.w( throwable);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Observable<Map<String, Object>> notify2(Msg msg) {
        try {
            RobotService robotService = ServiceFactory.getRobotService();
            return robotService.notify(API.notifyAPI(), getMap(msg)).subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Map<String,String> getMap(Msg msg) throws GeneralSecurityException {
        Map<String, String> map = new HashMap<>();
        String key = "";
        map.put("device", AESUtil.encrypt(key, new Gson().toJson(msg)));
        map.put("key", key);
        return map;
    }
}
