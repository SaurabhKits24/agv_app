//package com.reeman.dao.repository.entities
//
//import androidx.room.ColumnInfo
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//import com.google.gson.Gson
//import com.google.gson.annotations.SerializedName
//import com.google.gson.reflect.TypeToken
//import java.io.Serializable
//
//@Entity(tableName = "t_route_with_points")
//data class RouteWithPoints(
//    @PrimaryKey(autoGenerate = true)
//    var id: Long = 0,
//
//    @SerializedName("routeName")
//    @ColumnInfo(name = "t_route_name")
//    var routeName: String,
//
//    @SerializedName("taskFinishAction")
//    @ColumnInfo(name = "t_task_finish_action")
//    var taskFinishAction: Int = 0, //1:回出品点;2:重新开始路线巡航;3.回充电桩
//
//    @SerializedName("executeAgainSwitch")
//    @ColumnInfo(name = "t_execute_again_switch")
//    var executeAgainSwitch: Boolean = false, //执行完当前任务是否再次执行
//
//    @SerializedName("executeAgainTime")
//    @ColumnInfo(name = "t_execute_again_time")
//    var executeAgainTime: Int = 0, //再次执行任务的时间间隔
//
//    @SerializedName("pointsVOListJSONStr")
//    @ColumnInfo(name = "t_points_list_json_str")
//    var pointsVOListJSONStr: String? = null,
//
//    @SerializedName("navigationMode")
//    @ColumnInfo(name = "t_navigation_mode")
//    var navigationMode: Int = 0 //自动路线模式:1;固定路线模式:2;
//) : Serializable {
//
//    constructor(routeWithPoints: RouteWithPoints) : this(
//        id = routeWithPoints.id,
//        routeName = routeWithPoints.routeName,
//        taskFinishAction = routeWithPoints.taskFinishAction,
//        executeAgainSwitch = routeWithPoints.executeAgainSwitch,
//        executeAgainTime = routeWithPoints.executeAgainTime,
//        pointsVOListJSONStr = routeWithPoints.pointsVOListJSONStr,
//        navigationMode = routeWithPoints.navigationMode
//    )
//
//    constructor(
//        routeName: String,
//        taskFinishAction: Int,
//        executeAgainSwitch: Boolean,
//        executeAgainTime: Int,
//        pointsVOList: List<PointsVO>?,
//        navigationMode: Int
//    ) : this(
//        routeName = routeName,
//        taskFinishAction = taskFinishAction,
//        executeAgainSwitch = executeAgainSwitch,
//        executeAgainTime = executeAgainTime,
//        pointsVOListJSONStr = Gson().toJson(pointsVOList, object : TypeToken<List<PointsVO>>() {}.type),
//        navigationMode = navigationMode
//    )
//
//    constructor(routeName: String, taskFinishAction: Int, pointsVOList: List<PointsVO>?) : this(
//        routeName = routeName,
//        taskFinishAction = taskFinishAction,
//        pointsVOListJSONStr = Gson().toJson(pointsVOList, object : TypeToken<List<PointsVO>>() {}.type)
//    )
//
//    constructor(routeName: String, taskFinishAction: Int, executeAgainTime: Int, pointsVOList: List<PointsVO>?) : this(
//        routeName = routeName,
//        taskFinishAction = taskFinishAction,
//        executeAgainTime = executeAgainTime,
//        pointsVOListJSONStr = Gson().toJson(pointsVOList, object : TypeToken<List<PointsVO>>() {}.type)
//    )
//
//    fun getPointsVOList(): List<PointsVO> {
//        if (pointsVOListJSONStr.isNullOrBlank()){
//            return emptyList()
//        }
//        return Gson().fromJson(pointsVOListJSONStr, object : TypeToken<List<PointsVO>>() {}.type)
//    }
//
//    fun isPointVOListEmpty(): Boolean {
//        val pointsVOList: List<PointsVO>? = Gson().fromJson(pointsVOListJSONStr, object : TypeToken<List<PointsVO>>() {}.type)
//        if (pointsVOList.isNullOrEmpty()) {
//            return true
//        }
//        val pointsVOListNew = pointsVOList.filter { it.point.isNotEmpty() }
//        return pointsVOListNew.isEmpty()
//    }
//
//    fun setPointsVOList(pointsVOList: List<PointsVO>?) {
//        pointsVOListJSONStr = pointsVOList?.let {
//            Gson().toJson(it, object : TypeToken<List<PointsVO>>() {}.type)
//        }?:""
//    }
//
//
//    companion object {
//        fun getDefault(routeName: String, navigationMode: Int): RouteWithPoints {
//            val pointsVOS = listOf<PointsVO>()
//            return RouteWithPoints(
//                routeName = routeName,
//                taskFinishAction = 1,
//                executeAgainSwitch = false,
//                executeAgainTime = 10,
//                pointsVOList = pointsVOS,
//                navigationMode = navigationMode
//            )
//        }
//    }
//
//    override fun toString(): String {
//        return "RouteWithPoints(routeName='$routeName', taskFinishAction=$taskFinishAction, executeAgainSwitch=$executeAgainSwitch, executeAgainTime=$executeAgainTime, pointsVOListJSONStr='$pointsVOListJSONStr', navigationMode=$navigationMode)"
//    }
//}
