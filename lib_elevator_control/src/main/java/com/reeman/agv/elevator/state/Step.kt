package com.reeman.agv.elevator.state

enum class Step {
    /**
     * 初始状态
     */
    IDLE,

    /**
     * 检查电梯状态
     */
    CHECK_ELEVATOR_STATE,

    /**
     * 获取请求id
     */
    REQUEST_ID,

    /**
     * 激活
     */
    ACTIVATION,

    /**
     * 上线
     */
    ONLINE,

    /**
     * 连接mqtt
     */
    CONNECT_MQTT,

    /**
     * 订阅
     */
    SUBSCRIBE,

    /**
     * 呼梯
     */
    CALL_ELEVATOR,

    /**
     * 在电梯内呼梯
     */
    CALL_ELEVATOR_INSIDE,

    /**
     * 收到进梯消息的回复
     */
    ENTER_ELEVATOR_ACK,

    /**
     * 进梯失败,返回候梯点
     */
    ENTER_ELEVATOR_FAILED_RETURN_TO_WAITING_POINT,

    /**
     * 检查是否可以进梯
     */
    CHECK_PATH_TO_ELEVATOR,

    /**
     * 进梯成功
     */
    ENTER_ELEVATOR_COMPLETE,

    /**
     * 收到出梯消息的回复
     */
    LEAVE_ELEVATOR_ACK,

    /**
     * 出梯成功
     */
    LEAVE_ELEVATOR_COMPLETE,

    /**
     * 取消乘梯
     */
    CANCEL_ELEVATOR,

    /**
     * 切换地图
     */
    APPLY_MAP,

    /**
     * 初始化地图信息
     */
    INIT_POSE
}