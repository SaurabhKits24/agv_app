package com.reeman.agv.calling.model

import com.reeman.agv.calling.event.TaskEvent
import com.reeman.commons.state.TaskMode

class TaskDetails(
    val isButtonTask: Boolean,
    val key: String,
    val mode: TaskMode,
    val taskEvent: TaskEvent,
    val firstCallingTime: Long = System.currentTimeMillis(),
    var lastCallingTime: Long = System.currentTimeMillis(),
    var callingCount: Int = 1
){

    override fun toString(): String {
        return "TaskDetails(isButtonTask=$isButtonTask, key='$key', mode=$mode, taskEvent=$taskEvent, firstCallingTime=$firstCallingTime, lastCallingTime=$lastCallingTime, callingCount=$callingCount)"
    }
}