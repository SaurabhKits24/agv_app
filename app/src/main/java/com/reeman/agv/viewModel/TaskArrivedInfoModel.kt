package com.reeman.agv.viewModel

import android.os.Parcel
import android.os.Parcelable

/**
 * 任务暂停
 */
class TaskArrivedInfoModel private constructor(
    val taskMode: String,
    val routeName: String,
    val nextFloor: String,
    val nextPoint: String,
    val nowFloor: String,
    val taskStartTime: String,
    val currentPoint: String,
    val countDownTime: Long,
    val showReturnButton: Boolean,
    val showLiftUpButton: Boolean,
    val showLiftDownButton: Boolean,
    val showGotoNextPointButton: Boolean,
    val showCancelTaskButton: Boolean
) : Parcelable {

    class Builder(private var taskMode: String = "") {
        private var routeName: String = ""
        private var nextFloor: String = ""
        private var nextPoint: String = ""
        private var nowFloor: String = ""
        private var taskStartTime: String = ""
        private var currentPoint: String = ""
        private var countDownTime: Long = 0
        private var showReturnButton: Boolean = false
        private var showLiftUpButton: Boolean = false
        private var showLiftDownButton: Boolean = false
        private var showGotoNextPointButton: Boolean = false
        private var showCancelTaskButton: Boolean = false

        fun setRouteName(routeName: String) = apply { this.routeName = routeName }
        fun setNextFloor(nextFloor: String) = apply { this.nextFloor = nextFloor }

        fun setNextPoint(nextPoint: String) = apply { this.nextPoint = nextPoint }

        fun setNowFloor(nowFloor: String) = apply { this.nowFloor = nowFloor }
        fun setTaskStartTime(taskStartTime: String) = apply { this.taskStartTime = taskStartTime }
        fun setCurrentPoint(currentPoint: String) = apply { this.currentPoint = currentPoint }
        fun setCountDownTime(countDownTime: Long) = apply { this.countDownTime = countDownTime }
        fun setShowReturnButton(showReturnButton: Boolean) =
            apply { this.showReturnButton = showReturnButton }

        fun setShowLiftUpButton(showLiftUpButton: Boolean) =
            apply { this.showLiftUpButton = showLiftUpButton }

        fun setShowLiftDownButton(showLiftDownButton: Boolean) =
            apply { this.showLiftDownButton = showLiftDownButton }

        fun setShowGotoNextPointButton(showGotoNextPointButton: Boolean) =
            apply { this.showGotoNextPointButton = showGotoNextPointButton }

        fun setShowCancelTaskButton(showCancelTaskButton: Boolean) =
            apply { this.showCancelTaskButton = showCancelTaskButton }

        fun build(): TaskArrivedInfoModel {
            return TaskArrivedInfoModel(
                taskMode,
                routeName,
                nextFloor,
                nextPoint,
                nowFloor,
                taskStartTime,
                currentPoint,
                countDownTime,
                showReturnButton,
                showLiftUpButton,
                showLiftDownButton,
                showGotoNextPointButton,
                showCancelTaskButton
            )
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(taskMode)
        parcel.writeString(routeName)
        parcel.writeString(nextFloor)
        parcel.writeString(nextPoint)
        parcel.writeString(nowFloor)
        parcel.writeString(taskStartTime)
        parcel.writeString(currentPoint)
        parcel.writeLong(countDownTime)
        parcel.writeByte(if (showReturnButton) 1 else 0)
        parcel.writeByte(if (showLiftUpButton) 1 else 0)
        parcel.writeByte(if (showLiftDownButton) 1 else 0)
        parcel.writeByte(if (showGotoNextPointButton) 1 else 0)
        parcel.writeByte(if (showCancelTaskButton) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskArrivedInfoModel> {
        override fun createFromParcel(parcel: Parcel): TaskArrivedInfoModel {
            return TaskArrivedInfoModel(parcel)
        }

        override fun newArray(size: Int): Array<TaskArrivedInfoModel?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "任务模式 : $taskMode" +
                "\n路线名称 : $routeName" +
                "\n下一层楼 :$nextFloor" +
                "\n下一个目标点 : $nextPoint" +
                "\n当前楼层 : $nowFloor" +
                "\n任务开始时间 : $taskStartTime" +
                "\n当前点位 : $currentPoint" +
                "\n倒计时 : $countDownTime" +
                "\n返回按钮 : $showReturnButton" +
                "\n顶升抬起按钮 : $showLiftUpButton" +
                "\n顶升放下按钮 : $showLiftDownButton" +
                "\n去下一个点按钮 : $showGotoNextPointButton" +
                "\n取消任务按钮 : $showCancelTaskButton"
    }
}
