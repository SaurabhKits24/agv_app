package com.reeman.dao.repository.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "t_crash_notify")
public class CrashNotify {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @SerializedName("notify")
    @ColumnInfo(name = "t_notify")
    public String notify;

    public CrashNotify( String notify) {
        this.notify = notify;
    }

    @Override
    public String toString() {
        return "CrashNotify{" +
                "id=" + id +
                ", notify='" + notify + '\'' +
                '}';
    }
}
