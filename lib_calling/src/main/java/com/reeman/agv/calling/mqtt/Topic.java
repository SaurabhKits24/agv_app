package com.reeman.agv.calling.mqtt;

public class Topic {

    /**
     * 手机端获取点位的topic
     * @param hostname
     * @return
     */
    public static String topicRequestPointsStart(String hostname){
        return "reeman/calling/phone/" + hostname + "/v2/points/request";
    }

    public static String topicTaskStart(String hostname){
        return "reeman/calling/phone/" + hostname + "/v2/task";
    }

    /**
     * 开始任务响应
     * @param hostname
     * @return
     */
    public static String topicStartTaskResponse(String hostname){
        return "reeman/calling/robot/"+hostname+"/v2/task/response";
    }

    /**
     * 机器人端心跳
     * @param hostname
     * @return
     */
    public static String topicRobotHeartBeat(String hostname){
        return "reeman/calling/robot/"+hostname+"/v2/heartbeat";
    }

    /**
     * 手机端心跳
     * @param hostname
     * @return
     */
    public static String topicPhoneHeartBeat(String hostname){
        return "reeman/calling/phone/"+hostname+"/v2/heartbeat";
    }

    /**
     * 手机端获取呼叫模式点位
     * @param hostname
     * @return
     */
    public static String subscribeRequestCallingModelPoints(String hostname){
        return "reeman/calling/phone/" + hostname + "/v2/points/request/calling_model";
    }

    /**
     * 手机端下发呼叫模式任务
     * @param hostname
     * @return
     */
    public static String subscribeCallingModelTask(String hostname){
        return "reeman/calling/phone/" + hostname + "/v2/task/calling_model";
    }

    /**
     * 机器人端推送呼叫模式点位
     * @param hostname
     * @return
     */
    public static String publishCallingModelPoints(String hostname){
        return "reeman/calling/robot/"+hostname+"/v2/points/response/calling_model";
    }

    /**
     * 手机端获取普通模式点位
     * @param hostname
     * @return
     */
    public static String subscribeRequestNormalModelPoints(String hostname){
        return "reeman/calling/phone/" + hostname + "/v2/points/request/normal_model";
    }

    /**
     * 手机端下发普通模式任务
     * @param hostname
     * @return
     */
    public static String subscribeNormalModelTask(String hostname){
        return "reeman/calling/phone/" + hostname + "/v2/task/normal_model";
    }

    /**
     * 机器人端推送普通模式点位
     * @param hostname
     * @return
     */
    public static String publishNormalModelPoints(String hostname){
        return "reeman/calling/robot/"+hostname+"/v2/points/response/normal_model";
    }

    /**
     * 手机端获取路线模式路线列表
     * @param hostname
     * @return
     */
    public static String subscribeRequestRouteModelPoints(String hostname){
        return "reeman/calling/phone/" + hostname + "/v2/points/request/route_model";
    }

    /**
     * 手机端下发路线模式任务
     * @param hostname
     * @return
     */
    public static String subscribeRouteModelTask(String hostname){
        return "reeman/calling/phone/" + hostname + "/v2/task/route_model";
    }

    /**
     * 机器人端推送路线模式路线名称
     * @param hostname
     * @return
     */
    public static String publishRouteModelPoints(String hostname){
        return "reeman/calling/robot/"+hostname+"/v2/points/response/route_model";
    }

    /**
     * 手机端获取二维码模式点位列表
     * @param hostname
     * @return
     */
    public static String subscribeRequestQRCodeModelPoints(String hostname){
        return "reeman/calling/phone/" + hostname + "/v2/points/request/qrcode_model";
    }

    /**
     * 手机端下发二维码模式任务
     * @param hostname
     * @return
     */
    public static String subscribeQRCodeModelTask(String hostname){
        return "reeman/calling/phone/" + hostname + "/v2/task/qrcode_model";
    }

    /**
     * 机器人端推送二维码模式点位
     * @param hostname
     * @return
     */
    public static String publishQRCodeModelPoints(String hostname){
        return "reeman/calling/robot/"+hostname+"/v2/points/response/qrcode_model";
    }

    /**
     * 手机端下发回充任务
     * @param hostname
     * @return
     */
    public static String subscribeChargeModelTask(String hostname){
        return "reeman/calling/phone/"+hostname+"/v2/task/charge_model";
    }

    /**
     * 手机端下发返航任务
     * @param hostname
     * @return
     */
    public static String subscribeReturnModelTask(String hostname){
        return "reeman/calling/phone/"+hostname+"/v2/task/return_model";
    }
}
