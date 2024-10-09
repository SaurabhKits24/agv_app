package com.reeman.agv.task.impl;

import static com.reeman.agv.base.BaseApplication.ros;

import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.reeman.agv.calling.CallingInfo;
import com.reeman.agv.calling.event.CallingTaskEvent;
import com.reeman.agv.calling.model.TaskDetails;
import com.reeman.agv.calling.setting.ModeCallingSetting;
import com.reeman.commons.constants.Constants;
import com.reeman.agv.calling.model.TaskInfo;
import com.reeman.commons.state.RobotInfo;
import com.reeman.commons.state.TaskMode;
import com.reeman.agv.task.Task;

import kotlin.Pair;
import timber.log.Timber;

public class TaskModeCalling implements Task {

    private final Gson gson;

    private final RobotInfo robotInfo;

    private final CallingInfo callingInfo;

    private CallingTaskEvent callingTaskEvent;

    private ModeCallingSetting callingSetting;

    public TaskModeCalling(Gson gson) {
        this.gson = gson;
        this.robotInfo = RobotInfo.INSTANCE;
        this.callingInfo = CallingInfo.INSTANCE;
    }

    @Override
    public void initTask(Intent intent) {
        callingSetting = callingInfo.getCallingModeSetting();
        String callingTaskInfo = intent.getStringExtra(Constants.TASK_TARGET);
        callingTaskEvent = gson.fromJson(callingTaskInfo, CallingTaskEvent.class);
        Timber.w("呼叫模式配送配置 : %s \n 点位信息 : %s", callingSetting.toString(), callingTaskInfo);
        TaskDetails taskDetails = callingInfo.getTaskDetailsByToken(callingTaskEvent.getToken());
        if (taskDetails != null){
            Pair<String, String> point = callingTaskEvent.getPoint();
            String target = point.getSecond();
            if (!TextUtils.isEmpty(point.getFirst())){
                target = point.getFirst()+" - "+point.getSecond();
            }
            callingInfo.getHeartBeatInfo().setCurrentTask(
                    new TaskInfo(taskDetails.getFirstCallingTime(),System.currentTimeMillis(),TaskMode.MODE_CALLING.ordinal(),callingTaskEvent.getToken(),target)
            );
        }
        ros.setTolerance(robotInfo.isStopNearbyOpen() ? 1 : 0);
    }

    @Override
    public Pair<String, String> getNextPointWithElevator() {
        return callingTaskEvent.getPoint();
    }

    @Override
    public String getNextPointWithoutElevator() {
        return callingTaskEvent.getPoint().getSecond();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public int getDeliveryPointSize() {
        return 1;
    }

    @Override
    public String getSpeed() {
        return String.valueOf(callingSetting.speed);
    }

    @Override
    public long getCountDownTime() {
        return callingSetting.waitingTime;
    }

    @Override
    public boolean showReturnBtn() {
        return false;
    }

    @Override
    public void resetAllParameter(RobotInfo robotInfo,CallingInfo callingInfo) {
        Task.super.resetAllParameter(robotInfo,callingInfo);
        ros.setTolerance(1);
    }

    @Override
    public boolean shouldRemoveFirstCallingTask() {
        return true;
    }
}
