package com.reeman.agv.calling.setting;


import kotlin.Pair;

import com.reeman.commons.settings.BaseModeSetting;

import java.util.List;

public class ModeCallingSetting extends BaseModeSetting {

    public boolean openCallingQueue;

    public int waitingTime;

    public int cacheTime;

    public Pair<String, List<String>> key;

    public ModeCallingSetting(float speed, boolean startTaskCountDownSwitch, int startTaskCountDownTime, boolean openCallingQueue, int waitingTime, int cacheTime, Pair<String, List<String>> key) {
        super(speed, startTaskCountDownSwitch, startTaskCountDownTime);
        this.openCallingQueue = openCallingQueue;
        this.waitingTime = waitingTime;
        this.cacheTime = cacheTime;
        this.key = key;
    }

    public static ModeCallingSetting getDefault() {
        return new ModeCallingSetting(0.4f, false,5,false, 60, 60, null);
    }

    @Override
    public String toString() {
        return "ModeCallingSetting{" +
                "openCallingQueue=" + openCallingQueue +
                ", waitingTime=" + waitingTime +
                ", cacheTime=" + cacheTime +
                ", key=" + key +
                ", speed=" + speed +
                ", startTaskCountDownSwitch=" + startTaskCountDownSwitch +
                ", startTaskCountDownTime=" + startTaskCountDownTime +
                '}';
    }
}