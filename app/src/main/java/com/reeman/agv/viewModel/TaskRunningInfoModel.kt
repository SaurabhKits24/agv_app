package com.reeman.agv.viewModel

import android.os.Parcel
import android.os.Parcelable
import com.reeman.agv.elevator.state.Step

/**
 * 任务执行中
 */
class TaskRunningInfoModel private constructor(
    val elevatorStep: Step,
    val liftModelState: String,
    val qrCodeNavigationState: String,
    val targetFloor: String,
    val targetPoint: String,
) : Parcelable {

    class Builder() {
        private var elevatorStep: Step = Step.IDLE
        private var liftModelState: String = ""
        private var qrCodeNavigationState: String = ""
        private var targetFloor: String = ""
        private var targetPoint: String = ""

        fun setElevatorStep(elevatorStep: Step) = apply { this.elevatorStep = elevatorStep }

        fun setLiftModelState(liftModelState: String) =
            apply { this.liftModelState = liftModelState }

        fun setQRCodeNavigationState(qrCodeNavigationState: String) =
            apply { this.qrCodeNavigationState = qrCodeNavigationState }

        fun setTargetFloor(targetFloor: String) = apply { this.targetFloor = targetFloor }

        fun setTargetPoint(targetPoint: String) = apply { this.targetPoint = targetPoint }

        fun build(): TaskRunningInfoModel {
            return TaskRunningInfoModel(
                elevatorStep,
                liftModelState,
                qrCodeNavigationState,
                targetFloor,
                targetPoint,
            )
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readSerializable() as Step,
        parcel.readString()?:"",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(elevatorStep)
        parcel.writeString(liftModelState)
        parcel.writeString(qrCodeNavigationState)
        parcel.writeString(targetFloor)
        parcel.writeString(targetPoint)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskRunningInfoModel> {
        override fun createFromParcel(parcel: Parcel): TaskRunningInfoModel {
            return TaskRunningInfoModel(parcel)
        }

        override fun newArray(size: Int): Array<TaskRunningInfoModel?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "梯控步骤 : $elevatorStep" +
                "\n顶升状态 : $liftModelState" +
                "\n标签码导航状态 : $qrCodeNavigationState" +
                "\n目标楼层 : $targetFloor" +
                "\n目标点 : $targetPoint"
    }
}