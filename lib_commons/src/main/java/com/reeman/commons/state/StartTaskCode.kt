package com.reeman.commons.state


/**
 *     TASK_START 任务开始执行
 *     TASK_EXECUTING 任务正在执行
 *     EMERGENCY_STOP_DOWN 急停开关被按下
 *     AC_CHARGING 线充
 *     MAPPING 正在建图
 *     SELF_CHECKING 自检中
 *     LOW_POWER 低电
 *     NOT_WORKING_TIME 非工作时间
 *     LIFTING 顶升中
 *     LIFT_MODEL_LOCATION_ERROR 顶升模块待复位
 *     CALLING_TASK_COUNT_DOWN 呼叫任务倒计时未结束
 *     NOT_SUPPORT_ONLINE_TASK 当前界面不支持在线任务
 *     DECRYPT_FAILED 解密失败
 *     TASK_PARAM_ERROR 任务参数错误,未传关键参数或json解析失败
 *     JSON_EXCEPTION json转换异常
 *     TOKEN_EXCEPTION token错误
 *     NO_ROUTE 找不到指定路线
 *     CANCEL_MANUAL 任务被取消
 *     DEVICE_OFFLINE 收到获取点位/任务主题时,检测到设备未发送过心跳
 *     EXECUTING_AGAIN_SWITCH_OPENED_IN_ROUTE_TASK 路线模式下开启了任务循环执行,无法处理移动端任务
 *     CURRENT_TASK_MODE_NOT_SUPPORT_RESPONSE_TASK 当前任务模式(呼叫模式,回充模式,返航模式)不支持在任务执行中响应移动端任务
 */
object StartTaskCode {
    const val TASK_START = 0//任务开始执行
    const val TASK_EXECUTING = -1//任务正在执行
    const val EMERGENCY_STOP_DOWN = -2//急停开关被按下
    const val AC_CHARGING = -3//线充
    const val MAPPING = -4//正在建图
    const val SELF_CHECKING = -5//自检中
    const val LOW_POWER = -6//低电
    const val NOT_WORKING_TIME = -7//非工作时间
    const val LIFTING = -8//顶升中
    const val LIFT_MODEL_LOCATION_ERROR = -9//顶升模块待复位
    const val CALLING_TASK_COUNT_DOWN = -10//呼叫任务倒计时未结束
    const val NOT_SUPPORT_ONLINE_TASK = -11//当前界面不支持在线任务
    const val DECRYPT_FAILED = -12//解密失败
    const val TASK_PARAM_ERROR = -13//任务参数错误,未传关键参数或json解析失败
    const val JSON_SYNTAX_EXCEPTION = -14//json转换异常
    const val TOKEN_EXCEPTION = -15//token错误
    const val NO_ROUTE = -16//找不到指定路线
    const val CANCEL_MANUAL = -17//任务被取消
    const val DEVICE_OFFLINE = -18//收到获取点位/任务主题时,检测到设备未发送过心跳
    const val EXECUTING_AGAIN_SWITCH_OPENED_IN_ROUTE_TASK = -19//路线模式下开启了任务循环执行,无法处理移动端任务
    const val CURRENT_TASK_MODE_NOT_SUPPORT_RESPONSE_TASK = -20//当前任务模式(呼叫模式,回充模式,返航模式)不支持在任务执行中响应移动端任务
    const val NOT_SUPPORT_ELEVATOR_MODE = -21//不支持梯控


}