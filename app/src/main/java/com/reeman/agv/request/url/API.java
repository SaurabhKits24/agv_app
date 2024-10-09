package com.reeman.agv.request.url;

public class API {

    public static String taskRecordAPI(String hostname) {
        return "https://navi.rmbot.cn/openapispring/deliveryrobots/upload/" + hostname + "/navigation/logs";
    }

    public static String taskListRecordAPI(String hostname) {
        return "https://navi.rmbot.cn/openapispring/deliveryrobots/upload/" + hostname + "/navigation/logs/list";
    }

    public static String hardwareFaultAPI(String hostname) {
        return "https://navi.rmbot.cn/openapispring/deliveryrobots/upload/" + hostname + "/fault/logs";
    }

    public static String heartbeatAPI(String hostname) {
        return "https://navi.rmbot.cn/openapispring/deliveryrobots/upload/" + hostname;
    }

    public static String batteryLogAPI(String hostname) {
        return "https://navi.rmbot.cn/openapispring/deliveryrobots/upload/" + hostname + "/battery/logs";
    }

    public static String notifyAPI() {
        return "https://navi.rmbot.cn/openapispring/notify2/upload";
    }

    public static String tokenAPI(){
        return "https://navi.rmbot.cn/openapispring/tokens";
    }

    public static String getServerTimeAPI(){
        return "https://navi.rmbot.cn/openapispring/time";
    }
}