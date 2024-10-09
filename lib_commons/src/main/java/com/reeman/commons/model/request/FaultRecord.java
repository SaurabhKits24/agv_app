package com.reeman.commons.model.request;

import com.google.gson.annotations.SerializedName;

public class FaultRecord {

    @SerializedName("faultReason")
    public int faultReason;
    @SerializedName("faultDetails")
    public String faultDetails;
    @SerializedName("macAddress")
    public String macAddress;
    @SerializedName("interfaceVersion")
    public String interfaceVersion;
    @SerializedName("appVersion")
    public String appVersion;
    @SerializedName("rosVersion")
    public String rosVersion;
    @SerializedName("occurTime")
    public long occurTime;
    @SerializedName("uploadTime")
    public long uploadTime;


    public FaultRecord(int faultReason, String faultDetails, String macAddress, String interfaceVersion, String appVersion, String rosVersion, long occurTime, long uploadTime) {
        this.faultReason = faultReason;
        this.faultDetails = faultDetails;
        this.macAddress = macAddress;
        this.interfaceVersion = interfaceVersion;
        this.appVersion = appVersion;
        this.rosVersion = rosVersion;
        this.occurTime = occurTime;
        this.uploadTime = uploadTime;
    }

    @Override
    public String toString() {
        return "FaultRecord{" +
                "faultReason=" + faultReason +
                ", faultDetails='" + faultDetails + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", interfaceVersion='" + interfaceVersion + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", rosVersion='" + rosVersion + '\'' +
                ", occurTime=" + occurTime +
                ", uploadTime=" + uploadTime +
                '}';
    }
}
