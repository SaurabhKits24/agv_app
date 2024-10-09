package com.reeman.dao.repository.entities;

import android.text.TextUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "t_route_with_points")
public class RouteWithPoints implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @SerializedName("routeName")
    @ColumnInfo(name = "t_route_name")
    private String routeName;

    @SerializedName("taskFinishAction")
    @ColumnInfo(name = "t_task_finish_action")
    private int taskFinishAction; //1:回出品点;2:重新开始路线巡航;3.回充电桩

    @SerializedName("executeAgainSwitch")
    @ColumnInfo(name = "t_execute_again_switch")
    private boolean executeAgainSwitch;//执行完当前任务是否再次执行

    @SerializedName("executeAgainTime")
    @ColumnInfo(name = "t_execute_again_time")
    private int executeAgainTime;//再次执行任务的时间间隔

    @SerializedName("pointsVOListJSONStr")
    @ColumnInfo(name = "t_points_list_json_str")
    private String pointsVOListJSONStr;

    @SerializedName("navigationMode")
    @ColumnInfo(name = "t_navigation_mode")
    private int navigationMode;//自动路线模式:1;固定路线模式:2;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getExecuteAgainTime() {
        return executeAgainTime;
    }

    public void setExecuteAgainTime(int executeAgainTime) {
        this.executeAgainTime = executeAgainTime;
    }

    public boolean isExecuteAgainSwitch() {
        return executeAgainSwitch;
    }

    public void setExecuteAgainSwitch(boolean executeAgainSwitch) {
        this.executeAgainSwitch = executeAgainSwitch;
    }

    public int getNavigationMode() {
        return navigationMode;
    }

    public void setNavigationMode(int navigationMode) {
        this.navigationMode = navigationMode;
    }

    public RouteWithPoints(RouteWithPoints routeWithPoints) {
        this.id = routeWithPoints.id;
        this.routeName = routeWithPoints.routeName;
        this.taskFinishAction = routeWithPoints.taskFinishAction;
        this.executeAgainSwitch = routeWithPoints.executeAgainSwitch;
        this.executeAgainTime = routeWithPoints.executeAgainTime;
        this.pointsVOListJSONStr = routeWithPoints.pointsVOListJSONStr;
        this.navigationMode = routeWithPoints.navigationMode;
    }

    public RouteWithPoints(String routeName, int taskFinishAction, boolean executeAgainSwitch, int executeAgainTime, List<PointsVO> pointsVOList, int navigationMode) {
        this.routeName = routeName;
        this.taskFinishAction = taskFinishAction;
        this.executeAgainSwitch = executeAgainSwitch;
        this.executeAgainTime = executeAgainTime;
        this.pointsVOListJSONStr = new Gson().toJson(pointsVOList, new TypeToken<List<PointsVO>>() {
        }.getType());
        this.navigationMode = navigationMode;
    }

    public RouteWithPoints(String name, int taskFinishAction, List<PointsVO> pointsVOList) {
        this.routeName = name;
        this.taskFinishAction = taskFinishAction;
        this.pointsVOListJSONStr = new Gson().toJson(pointsVOList, new TypeToken<List<PointsVO>>() {
        }.getType());
    }

    public RouteWithPoints(String name, int taskFinishAction, int executeAgainTime, List<PointsVO> pointsVOList) {
        this.routeName = name;
        this.taskFinishAction = taskFinishAction;
        this.executeAgainTime = executeAgainTime;
        this.pointsVOListJSONStr = new Gson().toJson(pointsVOList, new TypeToken<List<PointsVO>>() {
        }.getType());
    }

    public RouteWithPoints() {
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public int getTaskFinishAction() {
        return taskFinishAction;
    }

    public void setTaskFinishAction(int taskFinishAction) {
        this.taskFinishAction = taskFinishAction;
    }


    public String getPointsVOListJSONStr() {
        return pointsVOListJSONStr;
    }

    public void setPointsVOListJSONStr(String pointsVOListJSONStr) {
        this.pointsVOListJSONStr = pointsVOListJSONStr;
    }

    public List<PointsVO> getPointsVOList() {
        return new Gson().fromJson(pointsVOListJSONStr,new TypeToken<List<PointsVO>>() {
        }.getType());
    }

    public void setPointsVOList(List<PointsVO> pointsVOList) {
        this.pointsVOListJSONStr = new Gson().toJson(pointsVOList, new TypeToken<List<PointsVO>>() {
        }.getType());
    }

    public static RouteWithPoints getDefault(String routeName, int navigationMode) {
        List<PointsVO> pointsVOS = new ArrayList<>();
        return new RouteWithPoints(routeName,
                1,
                false,
                10,
                pointsVOS,
                navigationMode
        );
    }
    @Override
    public String toString() {
        return "RouteWithPoints{" +
                ", routeName='" + routeName + '\'' +
                ", taskFinishAction=" + taskFinishAction +
                ", executeAgainSwitch=" + executeAgainSwitch +
                ", executeAgainTime=" + executeAgainTime +
                ", pointsVOListJSONStr='" + pointsVOListJSONStr + '\'' +
                ", navigationMode=" + navigationMode +
                '}';
    }
}