package com.reeman.commons.model.request;

import com.google.gson.annotations.SerializedName;

public class Msg {

    @SerializedName("notifyType")
    public int notifyType;
    @SerializedName("notifyTitle")
    public String notifyTitle;
    @SerializedName("notifyContent")
    public String notifyContent;
    @SerializedName("deviceName")
    public String deviceName;

    public Msg() {
    }

    public Msg(int notifyType, String notifyTitle, String notifyContent, String deviceName) {
        this.notifyType = notifyType;
        this.notifyTitle = notifyTitle;
        this.notifyContent = notifyContent;
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return "Msg{" +
                "notifyType=" + notifyType +
                ", notifyTitle='" + notifyTitle + '\'' +
                ", notifyContent='" + notifyContent + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }
}
