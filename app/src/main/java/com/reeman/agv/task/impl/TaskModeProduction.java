package com.reeman.agv.task.impl;

import static com.reeman.agv.base.BaseApplication.ros;

import android.content.Intent;

import kotlin.Pair;

import com.reeman.agv.calling.CallingInfo;
import com.reeman.agv.calling.model.TaskInfo;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.settings.ReturningSetting;
import com.reeman.commons.state.RobotInfo;
import com.reeman.commons.state.TaskMode;
import com.reeman.agv.task.Task;
import com.reeman.points.model.custom.GenericPoint;
import com.reeman.points.utils.PointCacheInfo;

import timber.log.Timber;

public class TaskModeProduction implements Task {

    private ReturningSetting returningSetting;
    private final RobotInfo robotInfo;

    public TaskModeProduction() {
        robotInfo = RobotInfo.INSTANCE;
    }

    private String token;

    @Override
    public void initTask(Intent intent) {
        returningSetting = robotInfo.getReturningSetting();
        if (intent.hasExtra(Constants.TASK_TOKEN)) {
            token = intent.getStringExtra(Constants.TASK_TOKEN);
        }
        long currentTimeMillis = System.currentTimeMillis();
        CallingInfo.INSTANCE.getHeartBeatInfo().setCurrentTask(
                new TaskInfo(
                        currentTimeMillis,
                        currentTimeMillis,
                        TaskMode.MODE_START_POINT.ordinal(),
                        intent.getStringExtra(Constants.TASK_TOKEN),
                        PointCacheInfo.INSTANCE.getProductionPoint().getSecond().getName()
                )
        );
        ros.setTolerance(robotInfo.isStopNearbyOpen() ? 1 : 0);
        Timber.w("返航配置 : %s", returningSetting.toString());
    }

    @Override
    public Pair<String, String> getNextPointWithElevator() {
        Pair<String, GenericPoint> productionPoint = PointCacheInfo.INSTANCE.getProductionPoint();
        return new Pair<>(productionPoint.getFirst(), productionPoint.getSecond().getName());
    }

    @Override
    public String getNextPointWithoutElevator() {
        return PointCacheInfo.INSTANCE.getProductionPoint().getSecond().getName();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public int getDeliveryPointSize() {
        return 0;
    }

    @Override
    public String getSpeed() {
        return String.valueOf(returningSetting.gotoProductionPointSpeed);
    }

    @Override
    public boolean showReturnBtn() {
        return false;
    }

    @Override
    public void resetStopNearByParameter() {
        ros.setTolerance(robotInfo.isStopNearbyOpen() ? 1 : 0);
    }

    @Override
    public void resetAllParameter(RobotInfo robotInfo, CallingInfo callingInfo) {
        Task.super.resetAllParameter(robotInfo, callingInfo);
        ros.setTolerance(1);
    }

    @Override
    public void updateRobotSize(boolean expansion) {
        int robotType = robotInfo.getRobotType();
        if (robotType == 2 || robotType == 3) {//24/22.5
            ros.robotRadius(expansion ? 0.28f : 0);
        } else {
            if (expansion) {
                if (robotType == 1) {//56,36
                    ros.footprint(0.60f, 0.40f);
                } else if (robotType == 4 || robotType == 6 || robotType == 7) {//81,56
                    ros.footprint(0.85f, 0.60f);
                } else if (robotType == 5 || robotType == 8) {//70,47
                    ros.footprint(0.74f, 0.51f);
                }
            } else {
                ros.footprint(0, 0);
            }
        }
    }

    @Override
    public boolean shouldRemoveFirstCallingTask() {
        return token != null;
    }
}
