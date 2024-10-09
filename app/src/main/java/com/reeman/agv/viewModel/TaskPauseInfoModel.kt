package com.reeman.agv.viewModel

import android.os.Parcel
import android.os.Parcelable

/**
 * 任务暂停
 */
class TaskPauseInfoModel private constructor(
    val taskMode: String,
    val routeName: String,
    val targetFloor: String,
    val targetPoint: String,
    val currentFloor: String,
    val taskStartTime: String,
    val taskPauseTip: String,
    val countDownTime: Long,
    val showReturnButton: Boolean,
    val showContinueTaskButton: Boolean,
    val showCancelTaskButton: Boolean,
    val showRecallElevatorButton: Boolean,
    val showSkipCurrentTargetButton: Boolean
) : Parcelable {

    class Builder(private var taskMode: String = "") {
        private var routeName: String = ""
        private var targetFloor: String = ""
        private var targetPoint: String = ""
        private var currentFloor: String = ""
        private var taskStartTime: String = ""
        private var taskPauseTip: String = ""
        private var countDownTime: Long = 0
        private var showReturnButton: Boolean = false
        private var showContinueTaskButton: Boolean = false
        private var showCancelTaskButton: Boolean = false
        private var showRecallElevatorButton: Boolean = false
        private var showSkipCurrentTargetButton: Boolean = false

        fun setRouteName(routeName: String) = apply { this.routeName = routeName }
        fun setTargetFloor(targetFloor: String) = apply { this.targetFloor = targetFloor }
        fun setTargetPoint(targetPoint: String) = apply { this.targetPoint = targetPoint }
        fun setCurrentFloor(currentFloor: String) = apply { this.currentFloor = currentFloor }
        fun setTaskStartTime(taskStartTime: String) = apply { this.taskStartTime = taskStartTime }
        fun setTaskPauseTip(taskPauseTip: String) = apply { this.taskPauseTip = taskPauseTip }
        fun setCountDownTime(countDownTime: Long) = apply { this.countDownTime = countDownTime }
        fun setShowReturnButton(showReturnButton: Boolean) =
            apply { this.showReturnButton = showReturnButton }

        fun setShowContinueTaskButton(showContinueTaskButton: Boolean) =
            apply { this.showContinueTaskButton = showContinueTaskButton }

        fun setShowCancelTaskButton(showCancelTaskButton: Boolean) =
            apply { this.showCancelTaskButton = showCancelTaskButton }

        fun setShowRecallElevatorButton(showRecallElevatorButton: Boolean) =
            apply { this.showRecallElevatorButton = showRecallElevatorButton }

        fun setShowSkipCurrentTargetButton(showSkipCurrentTargetButton: Boolean) =
            apply { this.showSkipCurrentTargetButton = showSkipCurrentTargetButton }

        fun build(): TaskPauseInfoModel {
            return TaskPauseInfoModel(
                taskMode,
                routeName,
                targetFloor,
                targetPoint,
                currentFloor,
                taskStartTime,
                taskPauseTip,
                countDownTime,
                showReturnButton,
                showContinueTaskButton,
                showCancelTaskButton,
                showRecallElevatorButton,
                showSkipCurrentTargetButton
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
        parcel.writeString(targetFloor)
        parcel.writeString(targetPoint)
        parcel.writeString(currentFloor)
        parcel.writeString(taskStartTime)
        parcel.writeString(taskPauseTip)
        parcel.writeLong(countDownTime)
        parcel.writeByte(if (showReturnButton) 1 else 0)
        parcel.writeByte(if (showContinueTaskButton) 1 else 0)
        parcel.writeByte(if (showCancelTaskButton) 1 else 0)
        parcel.writeByte(if (showRecallElevatorButton) 1 else 0)
        parcel.writeByte(if (showSkipCurrentTargetButton) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskPauseInfoModel> {
        override fun createFromParcel(parcel: Parcel): TaskPauseInfoModel {
            return TaskPauseInfoModel(parcel)
        }

        override fun newArray(size: Int): Array<TaskPauseInfoModel?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "任务模式 : $taskMode" +
                "\n路线名称 : $routeName" +
                "\n目标楼层 : $targetFloor" +
                "\n导航目标点 : $targetPoint" +
                "\n当前楼层 : $currentFloor" +
                "\n任务开始时间 : $taskStartTime" +
                "\n任务暂停提示 : $taskPauseTip" +
                "\n倒计时 : $countDownTime" +
                "\n返回按钮 : $showReturnButton" +
                "\n继续任务按钮 : $showContinueTaskButton" +
                "\n取消任务按钮 : $showCancelTaskButton" +
                "\n重新呼梯按钮 : $showRecallElevatorButton"+
                "\n跳过当前点位按钮 : $showSkipCurrentTargetButton"
    }
}
