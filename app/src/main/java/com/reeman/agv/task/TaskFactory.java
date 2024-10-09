package com.reeman.agv.task;


import com.google.gson.Gson;
import com.reeman.commons.state.TaskMode;
import com.reeman.agv.task.impl.TaskModeCalling;
import com.reeman.agv.task.impl.TaskModeCharge;
import com.reeman.agv.task.impl.TaskModeNormal;
import com.reeman.agv.task.impl.TaskModeProduction;
import com.reeman.agv.task.impl.TaskModeQRCode;
import com.reeman.agv.task.impl.TaskModeRoute;

public class TaskFactory {

    public static Task create(TaskMode taskMode, Gson gson) {
        switch (taskMode) {
            case MODE_NORMAL:
                return new TaskModeNormal(gson);
            case MODE_ROUTE:
                return new TaskModeRoute(gson);
            case MODE_QRCODE:
                return new TaskModeQRCode(gson);
            case MODE_CHARGE:
                return new TaskModeCharge();
            case MODE_START_POINT:
                return new TaskModeProduction();
            case MODE_CALLING:
                return new TaskModeCalling(gson);
        }
        throw new IllegalStateException("no mode");
    }
}
