package com.reeman.agv.calling.utils

/**
 * 0:空闲
 * 1:任务倒计时
 * 2:任务执行中(呼叫任务结束时应在倒计时结束后将状态置为0)
 * 3:任务异常结束
 */
class TaskExecutingCode {
    companion object {
        const val FREE = 0
        const val COUNTING_DOWN = 1
        const val TASK_EXECUTING = 2
        const val TASK_FINISHED_CAUSE_EXCEPTION = 3
    }
}