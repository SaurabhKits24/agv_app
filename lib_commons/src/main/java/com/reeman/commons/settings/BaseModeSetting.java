package com.reeman.commons.settings;

public class BaseModeSetting{

    public float speed;

    public boolean startTaskCountDownSwitch;

    public int startTaskCountDownTime;

    public BaseModeSetting(float speed, boolean startTaskCountDownSwitch, int startTaskCountDownTime) {
        this.speed = speed;
        this.startTaskCountDownSwitch = startTaskCountDownSwitch;
        this.startTaskCountDownTime = startTaskCountDownTime;
    }

    @Override
    public String toString() {
        return "BaseModeSetting{" +
                "speed=" + speed +
                ", startTaskCountDownSwitch=" + startTaskCountDownSwitch +
                ", startTaskCountDownTime=" + startTaskCountDownTime +
                '}';
    }
}
