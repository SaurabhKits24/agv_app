package com.reeman.commons.model.request;

import com.google.gson.annotations.SerializedName;

public class StateRecord {

    @SerializedName("deviceId")
    public int deviceId;
    @SerializedName("battery")
    public int battery;
    @SerializedName("powerMode")
    public int powerMode;
    @SerializedName("estop")
    public int eStop;
    @SerializedName("workingStatus")
    public int workingStatus;
    @SerializedName("rosVersion")
    public String rosVersion;
    @SerializedName("appVersion")
    public String appVersion;
    @SerializedName("robotType")
    public int robotType;
    @SerializedName("geolocation")
    public String geolocation;
    @SerializedName("macAddress")
    public String macAddress;
    @SerializedName("uploadTime")
    public long uploadTime;
    @SerializedName("interfaceVersion")
    public String interfaceVersion;
    @SerializedName("remarks")
    public String remarks;

    public StateRecord(int deviceId, int battery, int powerMode, int eStop, int workingStatus, String rosVersion, String appVersion, int robotType, String geolocation, String macAddress, long uploadTime, String interfaceVersion, String remarks) {
        this.deviceId = deviceId;
        this.battery = battery;
        this.powerMode = powerMode;
        this.eStop = eStop;
        this.workingStatus = workingStatus;
        this.rosVersion = rosVersion;
        this.appVersion = appVersion;
        this.robotType = robotType;
        this.geolocation = geolocation;
        this.macAddress = macAddress;
        this.uploadTime = uploadTime;
        this.interfaceVersion = interfaceVersion;
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "StateRecord{" +
                "deviceId=" + deviceId +
                ", battery=" + battery +
                ", powerMode=" + powerMode +
                ", eStop=" + eStop +
                ", workingStatus=" + workingStatus +
                ", rosVersion='" + rosVersion + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", robotType=" + robotType +
                ", geolocation='" + geolocation + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", uploadTime=" + uploadTime +
                ", interfaceVersion='" + interfaceVersion + '\'' +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}

