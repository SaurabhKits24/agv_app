package com.reeman.agv.task.impl;

import static com.reeman.agv.base.BaseApplication.ros;

import android.content.Intent;

import kotlin.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.reeman.agv.calling.CallingInfo;
import com.reeman.agv.calling.model.TaskDetails;
import com.reeman.agv.calling.model.TaskInfo;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.settings.ModeNormalSetting;
import com.reeman.commons.settings.ReturningSetting;
import com.reeman.commons.state.RobotInfo;
import com.reeman.commons.state.TaskMode;
import com.reeman.agv.task.Task;
import com.reeman.points.model.custom.GenericPoint;
import com.reeman.points.utils.PointCacheInfo;

import java.util.List;

import timber.log.Timber;

public class TaskModeNormal implements Task {

    private final Gson gson;

    private final RobotInfo robotInfo;


    private List<Pair<String, String>> pointList;

    private int currentPointListIndex = 0;

    private int arrivedPointSize;

    public TaskModeNormal(Gson gson) {
        this.gson = gson;
        this.robotInfo = RobotInfo.INSTANCE;
    }

    private ModeNormalSetting modeNormalSetting;

    private ReturningSetting returningSetting;

    private String token;

    @Override
    public void initTask(Intent intent) {
        returningSetting = robotInfo.getReturningSetting();
        modeNormalSetting = robotInfo.getModeNormalSetting();
        String taskTarget = intent.getStringExtra(Constants.TASK_TARGET);
        if (intent.hasExtra(Constants.TASK_TOKEN)) {
            token = intent.getStringExtra(Constants.TASK_TOKEN);
        }
        pointList = gson.fromJson(taskTarget, new TypeToken<List<Pair<String, String>>>() {
        }.getType());
        Timber.w("普通模式配送配置 : %s \n 点位信息 : %s\n返航配置 : %s", modeNormalSetting.toString(), taskTarget, returningSetting.toString());

        long currentTimeMillis = System.currentTimeMillis();

        CallingInfo.INSTANCE.getHeartBeatInfo().setCurrentTask(
                new TaskInfo(
                        currentTimeMillis,
                        currentTimeMillis,
                        TaskMode.MODE_NORMAL.ordinal(),
                        token,
                        getNextPoint()
                )
        );
        if (robotInfo.isNormalModeWithManualLiftControl()) {
            ros.avoidObstacle(modeNormalSetting.rotate);
            ros.setTolerance(modeNormalSetting.stopNearBy ? 1 : 0);
        } else {
            ros.setTolerance(robotInfo.isStopNearbyOpen() ? 1 : 0);
        }
    }

    private String getNextPoint() {
        if (robotInfo.isElevatorMode()) {
            Pair<String, String> nextPoint = getNextPointWithElevator();
            return nextPoint.getFirst() + " - " + nextPoint.getSecond();
        }
        return getNextPointWithoutElevator();
    }

    /**
     * 获取下一个配送点(带梯控)
     *
     * @return
     */
    @Override
    public Pair<String, String> getNextPointWithElevator() {
        if (pointList == null || currentPointListIndex < 0 || currentPointListIndex > pointList.size() - 1) {
            Pair<String, GenericPoint> productionPoint = PointCacheInfo.INSTANCE.getProductionPoint();
            return new Pair<>(productionPoint.getFirst(), productionPoint.getSecond().getName());
        }
        return pointList.get(currentPointListIndex);
    }

    /**
     * 获取下一个配送点
     *
     * @return
     */
    @Override
    public String getNextPointWithoutElevator() {
        if (pointList == null || currentPointListIndex < 0 || currentPointListIndex > pointList.size() - 1) {
            return PointCacheInfo.INSTANCE.getProductionPoint().getSecond().getName();
        }
        return pointList.get(currentPointListIndex).getSecond();
    }

    /**
     * 到达点位后从待配送列表中移除点位
     *
     * @param pointName
     */
    @Override
    public void arrivedPoint(String pointName) {
        arrivedPointSize++;
        currentPointListIndex++;
        CallingInfo.INSTANCE.getHeartBeatInfo().getCurrentTask().setTargetPoint(getNextPoint());
    }

    @Override
    public boolean showSkipCurrentTargetBtn() {
        return pointList != null && currentPointListIndex != -1 && currentPointListIndex < pointList.size() - 1;
    }

    @Override
    public void skipCurrentTarget() {
        currentPointListIndex++;
    }

    @Override
    public boolean hasNext() {
        return pointList != null && currentPointListIndex != -1 && currentPointListIndex < pointList.size();
    }

    @Override
    public int getDeliveryPointSize() {
        return arrivedPointSize;
    }

    @Override
    public void setRobotWidthAndLidar(boolean reset) {
        if (robotInfo.isNormalModeWithManualLiftControl()) {
            if (reset) {
                ros.footprint(0, 0);
                ros.lidarMin(Constants.DEFAULT_QRCODE_MODE_LIDAR_WIDTH_WITHOUT_THING);
            } else {
                ros.footprint(modeNormalSetting.length, modeNormalSetting.width);
                ros.lidarMin(modeNormalSetting.lidarWidth);
            }
        }
    }

    @Override
    public String getSpeed() {
        return String.valueOf(hasNext() ? modeNormalSetting.speed : returningSetting.gotoProductionPointSpeed);
    }

    @Override
    public boolean isNormalModeWithManualLiftControl() {
        return robotInfo.isNormalModeWithManualLiftControl();
    }

    @Override
    public long getCountDownTime() {
        return modeNormalSetting.waitingCountDownTimerOpen ? modeNormalSetting.waitingTime : 0;
    }

    @Override
    public long getPlayArrivedTipTime() {
        return modeNormalSetting.waitingCountDownTimerOpen ? modeNormalSetting.playArrivedTipTime : -1;
    }

    @Override
    public void clearAllPoints() {
        currentPointListIndex = -1;
    }

    @Override
    public boolean isArrivedLastPointAndStay() {
        return !hasNext() && modeNormalSetting.finishAction == 1;
    }

    @Override
    public void resetAllParameter(RobotInfo robotInfo, CallingInfo callingInfo) {
        Task.super.resetAllParameter(robotInfo, callingInfo);
//        updateRobotSize(false);
        if (robotInfo.isNormalModeWithManualLiftControl()) {
            ros.avoidObstacle(true);
        }
        ros.setTolerance(1);
        Timber.w("将普通模式相关配置恢复默认值");
    }

    @Override
    public void resetStopNearByParameter() {
        if (robotInfo.isNormalModeWithManualLiftControl()) {
            ros.setTolerance(modeNormalSetting.stopNearBy ? 1 : 0);
        } else {
            ros.setTolerance(robotInfo.isStopNearbyOpen() ? 1 : 0);
        }
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
        TaskDetails firstCallingDetails = CallingInfo.INSTANCE.getFirstCallingDetails();
        Timber.w("队列中第一个任务 : %s", firstCallingDetails);
        return firstCallingDetails != null && firstCallingDetails.getKey().equals(token) && firstCallingDetails.getMode() == TaskMode.MODE_NORMAL;
    }
}
