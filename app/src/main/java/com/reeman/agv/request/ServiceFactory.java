package com.reeman.agv.request;


import com.reeman.agv.plugins.RetrofitClient;
import com.reeman.agv.request.service.RobotService;

public class ServiceFactory {
    private static RobotService robotService;

    public static RobotService getRobotService() {
        if (robotService == null) {
            robotService = RetrofitClient.getClient().create(RobotService.class);
        }
        return robotService;
    }
}
