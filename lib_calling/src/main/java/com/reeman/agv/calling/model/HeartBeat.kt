package com.reeman.agv.calling.model

import com.google.gson.annotations.SerializedName

import android.os.Parcel
import android.os.Parcelable
import com.reeman.commons.state.TaskMode

class HeartBeat : Parcelable {
    @SerializedName("hostname")
    var hostname: String = "unknown"

    @SerializedName("alias")
    var alias:String = "unknown"

    @SerializedName("token")
    var token:String = "unknown"

    @SerializedName("level")
    var level: Int = 50

    @SerializedName("lowPower")
    var lowPower: Boolean = false

    @SerializedName("emergencyButton")
    var emergencyButton: Int = 1

    @SerializedName("chargeState")
    var chargeState: Int = 1

    @SerializedName("isNavigating")
    var isNavigating: Boolean = false

    @SerializedName("isElevatorMode")
    var isElevatorMode: Boolean = false

    @SerializedName("robotType")
    var robotType: Int = 1

    @SerializedName("liftModelState")
    var liftModelState: Int = 0

    @SerializedName("isLifting")
    var isLifting: Boolean = false

    @SerializedName("isMapping")
    var isMapping: Boolean = false

    @SerializedName("taskExecuting")
    var taskExecuting: Boolean = false

    @SerializedName("currentTask")
    var currentTask: TaskInfo? = null
        set(value) {
            field = value
            if (value != null
                && value.taskMode == TaskMode.MODE_CALLING.ordinal
                && !taskList.isNullOrEmpty()
            ) {
                taskList!!.find { it.token == value.token && it.targetPoint == value.targetPoint }?.startTime =
                    value.startTime
            }
        }

    @SerializedName("taskList")
    var taskList: List<TaskInfo>? = ArrayList()

    constructor()

    companion object CREATOR : Parcelable.Creator<HeartBeat> {
        override fun createFromParcel(parcel: Parcel): HeartBeat {
            return HeartBeat(parcel)
        }

        override fun newArray(size: Int): Array<HeartBeat?> {
            return arrayOfNulls(size)
        }
    }

    private constructor(parcel: Parcel) {
        hostname = parcel.readString() ?: "unknown"
        alias = parcel.readString() ?: "unknown"
        token = parcel.readString()?:"unknown"
        level = parcel.readInt()
        lowPower = parcel.readByte() != 0.toByte()
        emergencyButton = parcel.readInt()
        chargeState = parcel.readInt()
        isNavigating = parcel.readByte() != 0.toByte()
        isElevatorMode = parcel.readByte() != 0.toByte()
        robotType = parcel.readInt()
        liftModelState = parcel.readInt()
        isLifting = parcel.readByte() != 0.toByte()
        isMapping = parcel.readByte() != 0.toByte()
        taskExecuting = parcel.readByte() != 0.toByte()
        currentTask = parcel.readParcelable(TaskInfo::class.java.classLoader)
        taskList = parcel.createTypedArrayList(TaskInfo)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(hostname)
        parcel.writeString(alias)
        parcel.writeString(token)
        parcel.writeInt(level)
        parcel.writeByte(if (lowPower) 1 else 0)
        parcel.writeInt(emergencyButton)
        parcel.writeInt(chargeState)
        parcel.writeByte(if (isNavigating) 1 else 0)
        parcel.writeByte(if (isElevatorMode) 1 else 0)
        parcel.writeInt(robotType)
        parcel.writeInt(liftModelState)
        parcel.writeByte(if (isLifting) 1 else 0)
        parcel.writeByte(if (isMapping) 1 else 0)
        parcel.writeByte(if (taskExecuting) 1 else 0)
        parcel.writeParcelable(currentTask, flags)
        parcel.writeTypedList(taskList)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "HeartBeat{" +
                "hostname='" + hostname + '\'' +
                ", alias="+alias+
                ", token="+token+
                ", level=" + level +
                ", lowPower=" + lowPower +
                ", emergencyButton=" + emergencyButton +
                ", chargeState=" + chargeState +
                ", isNavigating=" + isNavigating +
                ", isElevatorMode=" + isElevatorMode +
                ", robotType=" + robotType +
                ", liftModelState=" + liftModelState +
                ", isLifting=" + isLifting +
                ", isMapping=" + isMapping +
                ", taskExecuting=" + taskExecuting +
                ", currentTask=" + currentTask +
                ", taskList=" + taskList +
                '}'
    }
}
