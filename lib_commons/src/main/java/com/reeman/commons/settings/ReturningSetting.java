package com.reeman.commons.settings;

public class ReturningSetting{

    public float gotoProductionPointSpeed;

    public float gotoChargingPileSpeed;

    public boolean startTaskCountDownSwitch;

    public int startTaskCountDownTime;

    public ReturningSetting(float gotoProductionPointSpeed, float gotoChargingPileSpeed, boolean startTaskCountDownSwitch, int startTaskCountDownTime) {
        this.gotoProductionPointSpeed = gotoProductionPointSpeed;
        this.gotoChargingPileSpeed = gotoChargingPileSpeed;
        this.startTaskCountDownSwitch = startTaskCountDownSwitch;
        this.startTaskCountDownTime = startTaskCountDownTime;
    }

    public static ReturningSetting getDefault(){
        return new ReturningSetting(
                0.4f,
                0.4f,
                false,
                5
        );
    }

    @Override
    public String toString() {
        return "ReturningSetting{" +
                "gotoProductionPointSpeed=" + gotoProductionPointSpeed +
                ", gotoChargingPileSpeed=" + gotoChargingPileSpeed +
                ", startTaskCountDownSwitch=" + startTaskCountDownSwitch +
                ", startTaskCountDownTime=" + startTaskCountDownTime +
                '}';
    }
}
