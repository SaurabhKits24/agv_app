package com.reeman.dao.repository.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "t_delivery_record")
public class DeliveryRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @SerializedName("deliveryMode")
    @ColumnInfo(name = "t_delivery_mode")
    public int deliveryMode;

    @SerializedName("deliveryLayers")
    @ColumnInfo(name = "t_delivery_layers")
    public int deliveryLayers;

    @SerializedName("deliveryTables")
    @ColumnInfo(name = "t_delivery_tables")
    public int deliveryTables;

    @SerializedName("startTime")
    @ColumnInfo(name = "t_start_time")
    public long startTime;

    @SerializedName("endTime")
    @ColumnInfo(name = "t_end_time")
    public long endTime;

    @SerializedName("uploadTime")
    @ColumnInfo(name = "t_upload_time")
    public long uploadTime;

    @SerializedName("odom")
    @ColumnInfo(name = "t_odom")
    public double odom;

    @SerializedName("getMealTime")
    @ColumnInfo(name = "t_meal_time")
    public int getMealTime;

    @SerializedName("deliveryResult")
    @ColumnInfo(name = "t_delivery_result")
    public int deliveryResult;

    @SerializedName("faultReason")
    @ColumnInfo(name = "t_fault_reason")
    public String faultReason;

    @SerializedName("macAddress")
    @ColumnInfo(name = "t_mac_address")
    public String macAddress;

    @SerializedName("interfaceVersion")
    @ColumnInfo(name = "t_interface_version")
    public String interfaceVersion;

    @SerializedName("appVersion")
    @ColumnInfo(name = "t_app_version")
    public String appVersion;

    @Ignore
    public DeliveryRecord(int deliveryMode,long startTime,String macAddress, String interfaceVersion, String appVersion) {
        this.deliveryMode = deliveryMode;
        this.startTime = startTime;
        this.macAddress = macAddress;
        this.interfaceVersion = interfaceVersion;
        this.appVersion = appVersion;
    }

    public DeliveryRecord(int deliveryMode, int deliveryLayers, int deliveryTables, long startTime, long endTime, long uploadTime, double odom, int getMealTime, int deliveryResult, String faultReason, String macAddress, String interfaceVersion, String appVersion) {
        this.deliveryMode = deliveryMode;
        this.deliveryLayers = deliveryLayers;
        this.deliveryTables = deliveryTables;
        this.startTime = startTime;
        this.endTime = endTime;
        this.uploadTime = uploadTime;
        this.odom = odom;
        this.getMealTime = getMealTime;
        this.deliveryResult = deliveryResult;
        this.faultReason = faultReason;
        this.macAddress = macAddress;
        this.interfaceVersion = interfaceVersion;
        this.appVersion = appVersion;
    }

    @Override
    public String toString() {
        return "DeliveryRecord{" +
                "deliveryMode=" + deliveryMode +
                ", deliveryLayers=" + deliveryLayers +
                ", deliveryTables=" + deliveryTables +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", uploadTime=" + uploadTime +
                ", odom=" + odom +
                ", getMealTime=" + getMealTime +
                ", deliveryResult=" + deliveryResult +
                ", faultReason='" + faultReason + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", interfaceVersion='" + interfaceVersion + '\'' +
                ", appVersion='" + appVersion + '\'' +
                '}';
    }
}


