package com.reeman.agv.calling.model

import com.reeman.commons.state.TaskMode
import java.io.Serializable

class RemoteTaskModel(
    val point: String,

    val taskMode: TaskMode,

    val firstCallingTime: Long,

    var lastCallingTime: Long,

    var callingCount: Int,

) : Serializable {
    override fun toString(): String {
        return "RemoteTaskModel(point='$point', taskMode=$taskMode, firstCallingTime=$firstCallingTime, lastCallingTime=$lastCallingTime, callingCount=$callingCount)"
    }
}