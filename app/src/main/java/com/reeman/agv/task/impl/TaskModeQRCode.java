package com.reeman.agv.task.impl;

import static com.reeman.agv.base.BaseApplication.ros;

import android.content.Intent;
import android.text.TextUtils;

import kotlin.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.reeman.agv.calling.CallingInfo;
import com.reeman.agv.calling.model.TaskDetails;
import com.reeman.agv.calling.model.TaskInfo;
import com.reeman.commons.constants.Constants;
import com.reeman.commons.settings.ModeQRCodeSetting;
import com.reeman.commons.settings.ReturningSetting;
import com.reeman.commons.state.RobotInfo;
import com.reeman.commons.state.TaskMode;
import com.reeman.agv.task.Task;
import com.reeman.points.model.custom.GenericPoint;
import com.reeman.points.utils.PointCacheInfo;

import java.util.List;

import timber.log.Timber;

public class TaskModeQRCode implements Task {

    private final Gson gson;

    private ModeQRCodeSetting modeQRCodeSetting;
    private ReturningSetting returningSetting;
    private boolean isArrivedFirstPoint;
    private int arrivedPointSize;

    private final RobotInfo robotInfo;


    private List<Pair<Pair<String, String>, Pair<String, String>>> qrCodePointsPairList;

    private int currentPairIndex = 0;

    private String token;


    public TaskModeQRCode(Gson gson) {
        this.gson = gson;
        robotInfo = RobotInfo.INSTANCE;
    }

    @Override
    public void initTask(Intent intent) {
        modeQRCodeSetting = robotInfo.getModeQRCodeSetting();
        returningSetting = robotInfo.getReturningSetting();
        String taskTarget = intent.getStringExtra(Constants.TASK_TARGET);
        token = intent.getStringExtra(Constants.TASK_TOKEN);
        qrCodePointsPairList = gson.fromJson(taskTarget, new TypeToken<List<Pair<Pair<String, String>, Pair<String, String>>>>() {
        }.getType());
        if (modeQRCodeSetting.lift && robotInfo.isLiftModelInstalled()) {
            ros.ioControl(4);
            ros.setTolerance(modeQRCodeSetting.stopNearby ? 1 : 0);
        } else {
            ros.setTolerance(robotInfo.isStopNearbyOpen() ? 1 : 0);
        }
        ros.avoidObstacle(modeQRCodeSetting.rotate);
        ros.agvMode(modeQRCodeSetting.direction ? 1 : 0);
        Timber.w("标签码模式配送配置 : %s \n 点位信息 : %s\n返航配置 : %s", modeQRCodeSetting.toString(), taskTarget, returningSetting.toString());
        long currentTimeMillis = System.currentTimeMillis();
        long createTime = currentTimeMillis;
        if (!TextUtils.isEmpty(token)) {
            TaskDetails taskDetails = CallingInfo.INSTANCE.getTaskDetailsByToken(token);
            if (taskDetails != null) {
                createTime = taskDetails.getFirstCallingTime();
            }
        }
        CallingInfo.INSTANCE.getHeartBeatInfo().setCurrentTask(
                new TaskInfo(
                        createTime,
                        currentTimeMillis,
                        TaskMode.MODE_QRCODE.ordinal(),
                        token,
                        getNextPoint()
                )
        );
    }

    private String getNextPoint() {
        if (robotInfo.isElevatorMode()) {
            Pair<String, String> nextPointWithElevator = getNextPointWithElevator();
            return nextPointWithElevator.getFirst() + " - " + nextPointWithElevator.getSecond();
        }
        return getNextPointWithoutElevator();
    }

    @Override
    public String getNextPointWithoutElevator() {
        if (qrCodePointsPairList == null || currentPairIndex < 0 || currentPairIndex > qrCodePointsPairList.size() - 1) {
            return PointCacheInfo.INSTANCE.getProductionPoint().getSecond().getName();
        }
        Pair<Pair<String, String>, Pair<String, String>> pair = qrCodePointsPairList.get(currentPairIndex);
        if (isArrivedFirstPoint) {
            return pair.getSecond().getSecond();
        }
        return pair.getFirst().getSecond();
    }

    @Override
    public Pair<String, String> getNextPointWithElevator() {
        if (qrCodePointsPairList == null || currentPairIndex < 0 || currentPairIndex > qrCodePointsPairList.size() - 1) {
            Pair<String, GenericPoint> productionPoint = PointCacheInfo.INSTANCE.getProductionPoint();
            return new Pair<>(productionPoint.getFirst(), productionPoint.getSecond().getName());
        }
        Pair<Pair<String, String>, Pair<String, String>> pair = qrCodePointsPairList.get(currentPairIndex);
        if (isArrivedFirstPoint) {
            return pair.getSecond();
        }
        return pair.getFirst();
    }

    @Override
    public void arrivedPoint(String pointName) {
        arrivedPointSize++;
        if (isArrivedFirstPoint) {
            currentPairIndex++;
            isArrivedFirstPoint = false;
        } else {
            isArrivedFirstPoint = true;
        }
        CallingInfo.INSTANCE.getHeartBeatInfo().getCurrentTask().setTargetPoint(getNextPoint());
    }

    @Override
    public boolean hasNext() {
        return qrCodePointsPairList != null && currentPairIndex != -1 && currentPairIndex < qrCodePointsPairList.size();
    }

    @Override
    public int getDeliveryPointSize() {
        return arrivedPointSize;
    }

    @Override
    public String getSpeed() {
        return String.valueOf(hasNext() ? modeQRCodeSetting.speed : returningSetting.gotoProductionPointSpeed);
    }

    @Override
    public boolean withLiftFunction() {
        return modeQRCodeSetting.lift && robotInfo.isLiftModelInstalled();
    }

    @Override
    public void clearAllPoints() {
        currentPairIndex = -1;
    }

    @Override
    public void setOrientationAndDistanceCalibration() {
        ros.agvMove(modeQRCodeSetting.direction ? -modeQRCodeSetting.orientationAndDistanceCalibration : modeQRCodeSetting.orientationAndDistanceCalibration);
    }

    @Override
    public boolean showReturnBtn() {
        return false;
    }

    @Override
    public void setRobotWidthAndLidar(boolean reset) {
        if (reset) {
            ros.footprint(0, 0);
            ros.lidarMin(Constants.DEFAULT_QRCODE_MODE_LIDAR_WIDTH_WITHOUT_THING);
        } else {
            ros.footprint(modeQRCodeSetting.length, modeQRCodeSetting.width);
            ros.lidarMin(modeQRCodeSetting.lidarWidth);
        }
    }

    @Override
    public void resetAllParameter(RobotInfo robotInfo, CallingInfo callingInfo) {
        Task.super.resetAllParameter(robotInfo, callingInfo);
        ros.agvMode(Constants.DEFAULT_QRCODE_MODE_DIRECTION);
        ros.agvMove(Constants.DEFAULT_ORIENTATION_AND_DISTANCE_CALIBRATION);
        ros.avoidObstacle(true);
        ros.setTolerance(1);
        Timber.w("将二维码模式相关配置恢复默认值");
    }

    @Override
    public boolean shouldRemoveFirstCallingTask() {
        TaskDetails firstTaskDetails = CallingInfo.INSTANCE.getFirstCallingDetails();
        if (firstTaskDetails != null && firstTaskDetails.getKey().equals(token) && firstTaskDetails.getMode() == TaskMode.MODE_QRCODE) {
            return true;
        }
        return false;
    }
}
