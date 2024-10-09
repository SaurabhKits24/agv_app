package com.reeman.agv.calling.model

import com.google.gson.annotations.SerializedName

import android.os.Parcel
import android.os.Parcelable

class TaskInfo : Parcelable {
    @SerializedName("createTime")
    var createTime: Long = 0

    @SerializedName("startTime")
    var startTime: Long = 0

    @SerializedName("taskMode")
    var taskMode: Int = 0

    @SerializedName("token")
    var token: String? = null

    @SerializedName("targetPoint")
    var targetPoint: String = ""

    constructor()
    constructor(
        createTime: Long,
        startTime: Long,
        taskMode: Int,
        token: String?,
        targetPoint: String
    ) {
        this.createTime = createTime
        this.startTime = startTime
        this.taskMode = taskMode
        this.token = token
        this.targetPoint = targetPoint
    }

    companion object CREATOR : Parcelable.Creator<TaskInfo> {
        override fun createFromParcel(parcel: Parcel): TaskInfo {
            return TaskInfo(parcel)
        }

        override fun newArray(size: Int): Array<TaskInfo?> {
            return arrayOfNulls(size)
        }
    }

    private constructor(parcel: Parcel) {
        createTime = parcel.readLong()
        startTime = parcel.readLong()
        taskMode = parcel.readInt()
        token = parcel.readString()
        targetPoint = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(createTime)
        parcel.writeLong(startTime)
        parcel.writeInt(taskMode)
        parcel.writeString(token)
        parcel.writeString(targetPoint)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "TaskInfo{" +
                "createTime=" + createTime +
                ", startTime=" + startTime +
                ", taskMode=" + taskMode +
                ", token='" + token + '\'' +
                ", targetPoint='" + targetPoint + '\'' +
                '}'
    }
}

