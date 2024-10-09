package com.reeman.commons.settings;


public class ModeRouteSetting extends BaseModeSetting {
    public ModeRouteSetting(float speed, boolean startTaskCountDownSwitch, int startTaskCountDownTime) {
        super(speed, startTaskCountDownSwitch, startTaskCountDownTime);
    }

    public static ModeRouteSetting getDefault(){
        return new ModeRouteSetting(0.4f,false,5);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
