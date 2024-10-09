package com.reeman.commons.settings;


public class CommutingTimeSetting {

    public boolean open;

    public String workingTime;

    public String afterWorkTime;

    public int autoWorkPower;

    public CommutingTimeSetting(boolean open, String workingTime, String afterWorkTime, int autoWorkPower) {
        this.open = open;
        this.workingTime = workingTime;
        this.afterWorkTime = afterWorkTime;
        this.autoWorkPower = autoWorkPower;
    }

    public static CommutingTimeSetting getDefault(){
        return new CommutingTimeSetting(
                false,
                "08:00",
                "18:00",
                90
        );
    }

    @Override
    public String toString() {
        return "CommutingTimeSetting{" +
                "open=" + open +
                ", workingTime='" + workingTime + '\'' +
                ", afterWorkTime='" + afterWorkTime + '\'' +
                ", autoWorkPower=" + autoWorkPower +
                '}';
    }
}
