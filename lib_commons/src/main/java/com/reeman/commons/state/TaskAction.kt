package com.reeman.commons.state

object TaskAction {
    //充电点
    const val charge_point = "charge_point"

    //出品点
    const val production_point = "production_point"

    //候梯点
    const val waiting_elevator = "elevator_wait"

    //进梯点
    const val enter_elevator = "elevator_enter"

    //电梯内
    const val inside_elevator = "elevator_inside"

    //出梯点
    const val leave_elevator = "elevator_leave"

    const val leave_elevator_position = "elevator_leave_position"

    //门控开门坐标
    const val door_front_position = "door_front_position"

    //门控开门点
    const val door_front_point = "door_front_point"

    //门控关门坐标
    const val door_back_position = "door_back_position"

    //配送点
    const val delivery_point = "delivery_point"

    //路线点
    const val route_point = "route_point"

    //agv点
    const val agv_point = "agv_point"

    //agv对接失败后的重试导航点
    const val agv_retry_point = "agv_retry_point"

    const val calling_point = "calling_point"

    //检查能否进入电梯
    const val check_inside_elevator_point_reachable = "check_inside_elevator_point_reachable"
    //检查是否能离开电梯
    const val check_can_leave_elevator = "check_can_leave_elevator"
    //离开电梯前往进梯点
    const val leave_elevator_to_enter_elevator_point = "leave_elevator_to_enter_elevator_point"
    //检查能否前往出梯点
    const val check_leave_elevator_point_reachable = "check_leave_elevator_point_reachable"


}