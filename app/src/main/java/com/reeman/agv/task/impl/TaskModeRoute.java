package com.reeman.agv.task.impl;

import static com.reeman.agv.base.BaseApplication.ros;

import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.reeman.agv.calling.CallingInfo;
import com.reeman.agv.calling.event.RouteTaskEvent;
import com.reeman.agv.calling.model.TaskDetails;
import com.reeman.agv.calling.model.TaskInfo;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.settings.CommutingTimeSetting;
import com.reeman.commons.settings.ReturningSetting;
import com.reeman.commons.state.TaskAction;
import com.reeman.commons.utils.TimeUtil;
import com.reeman.dao.repository.entities.RouteWithPoints;
import com.reeman.dao.repository.entities.PointsVO;
import com.reeman.commons.settings.ModeRouteSetting;
import com.reeman.commons.state.RobotInfo;
import com.reeman.commons.state.TaskMode;
import com.reeman.agv.task.Task;
import com.reeman.points.utils.PointCacheInfo;

import java.util.List;

import timber.log.Timber;

public class TaskModeRoute implements Task {

    private final Gson gson;

    private RouteWithPoints route;

    private ModeRouteSetting modeRouteSetting;
    private ReturningSetting returningSetting;

    private int arrivedPointSize;


    private final RobotInfo robotInfo;

    private List<PointsVO> targetPoints;

    private int targetPointListIndex;

    private String routeStr;

    private String token;

    public TaskModeRoute(Gson gson) {
        this.gson = gson;
        robotInfo = RobotInfo.INSTANCE;
    }

    @Override
    public void initTask(Intent intent) {
        modeRouteSetting = robotInfo.getModeRouteSetting();
        returningSetting = robotInfo.getReturningSetting();
        route = (RouteWithPoints) intent.getSerializableExtra(Constants.TASK_TARGET);
        if (route.isExecuteAgainSwitch()) {
            routeStr = new Gson().toJson(route);
        }
        if (intent.hasExtra(Constants.TASK_TOKEN)) {
            token = intent.getStringExtra(Constants.TASK_TOKEN);
        }
        targetPoints = route.getPointsVOList();
        Timber.w("路线模式配送配置 : %s \n点位信息 : %s \n返航配置 : %s", modeRouteSetting.toString(), gson.toJson(route), returningSetting.toString());
        long currentTimeMillis = System.currentTimeMillis();
        CallingInfo.INSTANCE.getHeartBeatInfo().setCurrentTask(
                new TaskInfo(
                        currentTimeMillis,
                        currentTimeMillis,
                        TaskMode.MODE_ROUTE.ordinal(),
                        token,
                        getNextPointWithoutElevator()
                )
        );
        ros.setTolerance(robotInfo.isStopNearbyOpen() ? 1 : 0);
    }

    @Override
    public boolean isExecuteAgainSwitch() {
        return route.isExecuteAgainSwitch();
    }

    @Override
    public String getNextPointWithoutElevator() {
        if (targetPoints == null || targetPoints.isEmpty() || targetPointListIndex >= targetPoints.size()) {
            if (route.getTaskFinishAction() == 1) {
                return PointCacheInfo.INSTANCE.getProductionPoint().getSecond().getName();
            }
            if (route.getTaskFinishAction() == 3) {
                return PointCacheInfo.INSTANCE.getChargePoint().getSecond().getName();
            }
            if (robotInfo.isLowPower()) {
                Timber.w("路线模式设置任务结束重新开始路线巡航时触发低电");
                return PointCacheInfo.INSTANCE.getChargePoint().getSecond().getName();
            }
            CommutingTimeSetting commutingTimeSetting = robotInfo.getCommutingTimeSetting();
            if (commutingTimeSetting.open && !TimeUtil.isCurrentInTimeScope(commutingTimeSetting.workingTime, commutingTimeSetting.afterWorkTime)) {
                Timber.w("路线模式设置任务结束重新开始路线巡航时触发下班");
                return PointCacheInfo.INSTANCE.getChargePoint().getSecond().getName();
            }
            targetPoints = route.getPointsVOList();
            targetPointListIndex = 0;
        }
        return targetPoints.get(targetPointListIndex).getPoint();
    }

    @Override
    public String getRouteTaskFinishAction() {
        if (route.getTaskFinishAction() == 1) {
            return TaskAction.production_point;
        } else {
            return TaskAction.charge_point;
        }
    }

    @Override
    public void arrivedPoint(String pointName) {
        arrivedPointSize++;
        targetPointListIndex++;
        CallingInfo.INSTANCE.getHeartBeatInfo().getCurrentTask().setTargetPoint(getNextPointWithoutElevator());
    }

    @Override
    public boolean hasNext() {
        CommutingTimeSetting commutingTimeSetting = robotInfo.getCommutingTimeSetting();
        boolean afterWork = commutingTimeSetting.open && !TimeUtil.isCurrentInTimeScope(commutingTimeSetting.workingTime, commutingTimeSetting.afterWorkTime);
        return (targetPoints != null && !targetPoints.isEmpty() && targetPointListIndex < targetPoints.size()) || (route.getTaskFinishAction() == 2 && !robotInfo.isLowPower() && !afterWork);
    }

    @Override
    public int getDeliveryPointSize() {
        return arrivedPointSize;
    }

    @Override
    public String getSpeed() {
        if (!hasNext()) {
            if (route.getTaskFinishAction() == 1) {
                Timber.w("任务结束,使用返回出品点的速度");
                return String.valueOf(returningSetting.gotoProductionPointSpeed);
            } else if (route.getTaskFinishAction() == 3) {
                Timber.w("任务结束,使用返回充电桩的速度");
                return String.valueOf(returningSetting.gotoChargingPileSpeed);
            } else {
                if (robotInfo.isLowPower()) {
                    Timber.w("触发低电,使用返回充电桩的速度");
                    return String.valueOf(returningSetting.gotoChargingPileSpeed);
                }
                CommutingTimeSetting commutingTimeSetting = robotInfo.getCommutingTimeSetting();
                if (commutingTimeSetting.open && !TimeUtil.isCurrentInTimeScope(commutingTimeSetting.workingTime, commutingTimeSetting.afterWorkTime)) {
                    Timber.w("下班,使用返回充电桩的速度");
                    return String.valueOf(returningSetting.gotoChargingPileSpeed);
                }
            }
        }
        return String.valueOf(modeRouteSetting.speed);
    }

    @Override
    public String getRouteName() {
        return route.getRouteName();
    }

    @Override
    public boolean showReturnBtn() {
        return route.getTaskFinishAction() == 1;
    }

    @Override
    public boolean showSkipCurrentTargetBtn() {
        CommutingTimeSetting commutingTimeSetting = robotInfo.getCommutingTimeSetting();
        boolean afterWork = commutingTimeSetting.open && !TimeUtil.isCurrentInTimeScope(commutingTimeSetting.workingTime, commutingTimeSetting.afterWorkTime);
        return targetPoints != null && targetPoints.size() > 1 || (route.getTaskFinishAction() == 2 && !robotInfo.isLowPower() && !afterWork);
    }

    @Override
    public void skipCurrentTarget() {
        targetPointListIndex++;
    }

    @Override
    public long getCountDownTime() {
        PointsVO pointsVO;
        if (targetPointListIndex >= targetPoints.size()) {
            pointsVO = targetPoints.get(targetPoints.size() - 1);
        } else {
            pointsVO = targetPoints.get(targetPointListIndex);
        }
        if (pointsVO.isOpenWaitingTime()) {
            return pointsVO.getWaitingTime();
        }
        return 0;
    }

    @Override
    public long getPlayArrivedTipTime() {
        int index;
        if (targetPointListIndex == 0) {
            index = targetPoints.size() - 1;
        } else {
            index = targetPointListIndex - 1;
        }
        PointsVO pointsVO = targetPoints.get(index);
        if (pointsVO.isOpenWaitingTime()) {
            return pointsVO.getWaitingTime();
        }
        return 0;
    }

    @Override
    public void clearAllPoints() {
        targetPoints = null;
    }

    @Override
    public void resetAllParameter(RobotInfo robotInfo, CallingInfo callingInfo) {
        Task.super.resetAllParameter(robotInfo, callingInfo);
        ros.setTolerance(1);
    }

    @Override
    public RouteWithPoints getRouteTask() {
        return routeStr != null ? new Gson().fromJson(routeStr, RouteWithPoints.class) : null;
    }

    @Override
    public boolean shouldRemoveFirstCallingTask() {
        TaskDetails firstCallingDetails = CallingInfo.INSTANCE.getFirstCallingDetails();
        Timber.w("队列中第一个任务 : %s", firstCallingDetails);
        return firstCallingDetails != null && firstCallingDetails.getKey().equals(token) && firstCallingDetails.getMode() == TaskMode.MODE_ROUTE;
    }
}
