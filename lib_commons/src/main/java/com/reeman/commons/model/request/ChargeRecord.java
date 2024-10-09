package com.reeman.commons.model.request;

import com.google.gson.annotations.SerializedName;

public class ChargeRecord {

    @SerializedName("startBattery")
    public int startBattery;
    @SerializedName("endBattery")
    public int endBattery;
    @SerializedName("startTime")
    public long startTime;
    @SerializedName("endTime")
    public long endTime;
    @SerializedName("chargingModel")
    public int chargingModel;
    @SerializedName("startVoltage")
    public double startVoltage;
    @SerializedName("endVoltage")
    public double endVoltage;
    @SerializedName("startElectricCurrent")
    public int startElectricCurrent;
    @SerializedName("endElectricCurrent")
    public int endElectricCurrent;
    @SerializedName("uploadTime")
    public long uploadTime;
    @SerializedName("macAddress")
    public String macAddress;
    @SerializedName("interfaceVersion")
    public String interfaceVersion;
    @SerializedName("appVersion")
    public String appVersion;

    public ChargeRecord(int startBattery, int endBattery, long startTime, long endTime, int chargingModel, double startVoltage, double endVoltage, int startElectricCurrent, int endElectricCurrent, long uploadTime, String macAddress, String interfaceVersion, String appVersion) {
        this.startBattery = startBattery;
        this.endBattery = endBattery;
        this.startTime = startTime;
        this.endTime = endTime;
        this.chargingModel = chargingModel;
        this.startVoltage = startVoltage;
        this.endVoltage = endVoltage;
        this.startElectricCurrent = startElectricCurrent;
        this.endElectricCurrent = endElectricCurrent;
        this.uploadTime = uploadTime;
        this.macAddress = macAddress;
        this.interfaceVersion = interfaceVersion;
        this.appVersion = appVersion;
    }

    @Override
    public String toString() {
        return "ChargeRecord{" +
                "startBattery=" + startBattery +
                ", endBattery=" + endBattery +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", chargingModel=" + chargingModel +
                ", startVoltage=" + startVoltage +
                ", endVoltage=" + endVoltage +
                ", startElectricCurrent=" + startElectricCurrent +
                ", endElectricCurrent=" + endElectricCurrent +
                ", uploadTime=" + uploadTime +
                ", macAddress='" + macAddress + '\'' +
                ", interfaceVersion='" + interfaceVersion + '\'' +
                ", appVersion='" + appVersion + '\'' +
                '}';
    }
}
