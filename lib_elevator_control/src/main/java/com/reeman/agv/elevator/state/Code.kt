package com.reeman.agv.elevator.state

object Code {
    /**
     * 请求成功
     */
    const val SUCCESS = "1"

    /**
     * 梯控网关离线
     * 提示:
     * 已获取梯控平台分配的电梯 ID 为【elevatorId】，但因梯控网关不在线而无法正常调度电梯，建议联系按以下步骤排查问题：
     * 第一步，请工作人员登录梯控网关确认梯控网关是否正常上网且已正常连接梯控平台；
     * 第二步，若第一步没问题，请联系梯控平台方案厂家获取支持.
     */
    const val ELEVATOR_OFFLINE = "0"

    /**
     * 机器人和电梯绑定状态异常
     * 提示:
     * 机器人【robotId】未绑定到电梯 ID 为【elevatorId】的电梯，请工作人员检查梯控平台确认机器人与梯控平台的电梯的绑定关系。
     */
    const val ELEVATOR_NOT_FOUND = "ElevatorNotFoundException"

    /**
     * 一般在取消乘梯接口才收到该状态码。
     * 无需处理
     */
    const val REQUEST_NOT_FOUND = "RequestNotFoundException"

    /**
     * 一般发起乘梯请求接口才收到该状态码
     * 已经存在该机器人【passenger】搭乘电梯【elevatorId】请求
     * 取消乘梯,重试
     */
    const val REQUEST_ALREADY_EXIST = "RequestAlreadyExistException"

    /**
     * 一般在乘梯完成接口才收到该状态码
     * 情况 1：未发起乘梯请求就调用进出梯完成接口
     * 情况 2：未通知进出梯就调用进出梯完成接口（电梯抵达楼层但未开门时调用了该接口）
     * 逻辑问题
     */
    const val REQUEST_FINISHED_NOT_ALLOWED = "RequestFinishedNotAllowedException"

    /**
     * 出发楼层和目标楼层相同
     * 逻辑问题
     */
    const val INVALID_FLOOR = "InvalidFloorException"

    /**
     * 一般发起乘梯请求接口才收到该状态码
     * 提示:
     * 未找到合适的电梯给乘客【${robotId}】，建议联系按以下步骤排查问题:
     * 第一步，请工作人员登录梯控平台确保梯控机器人已绑定到电梯、确保梯控网关已经绑定到电梯；
     * 第二步，确认梯控平台配置的电梯最低楼层和最高楼层与实际相符；
     * 第三步，若第一、二步都没问题，请联系梯控平台方案厂家获取支持。
     */
    const val NO_ELEVATOR_AVAILABLE = "NoElevatorAvailableException"

    /**
     * 解析host失败
     */
    const val PARSE_MQTT_HOST_FAILED = "ParseMqttHostException"

    /**
     * mqtt连接失败
     * 检查网络连接,重试
     */
    const val CONNECT_MQTT_FAILED = "ConnectMQTTException"

    /**
     * mqtt订阅失败
     * 重连,重新订阅
     */
    const val SUBSCRIBE_FAILED = "SubscribeException"

    /**
     * 响应体为null
     * 提示
     */
    const val RESPONSE_BODY_NULL = "ResponseBodyNullException"

    /**
     * 请求失败(http响应码不在[200,300)中)
     * 提示
     */
    const val REQUEST_FAIL = "RequestFailedException"

    /**
     * 请求失败
     * 检查本地网络是否正常,重试
     */
    const val IO_EXCEPTION = "IOException"

    /**
     * 解析响应体失败
     * 提示
     */
    const val JSON_SYNTAX_EXCEPTION = "JsonSyntaxException"

    /**
     * 未处理/未知的异常
     * 提示
     */
    const val UNKNOWN_EXCEPTION = "UnknownException"

    /**
     * 网关离线
     */
    const val THING_NOT_ONLINE_EXCEPTION = "thingNotOnlineException"
}