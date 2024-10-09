package com.reeman.commons.settings;

import kotlin.Pair;


public class ElevatorSetting {

    public boolean open;

    //first:alias second:MD5
    public Pair<String, String> chargingPileMap;

    //first:alias second:MD5
    public Pair<String, String> productionPointMap;

    public int waitingElevatorTimeoutRetryTimeInterval;

    public int enterOrLeavePointUnreachableRetryTimeInterval;

    public boolean isDetectionSwitchOpen;

    public int generatePathCount;

    //电梯单开门
    public boolean isSingleDoor;

    //单网络
    public boolean isSingleNetwork;
    //梯外网络 first:ssid,second:pwd
    public Pair<String, String> outsideNetwork;
    //梯内网络 first:ssid,second:pwd
    public Pair<String, String> insideNetwork = new Pair<>("RBTEC6200","TK62002@22");

    public ElevatorSetting(boolean open, int waitingElevatorTimeoutRetryTimeInterval, int enterOrLeavePointUnreachableRetryTimeInterval, boolean isDetectionSwitchOpen, int generatePathCount, boolean isSingleDoor, boolean isSingleNetwork) {
        this.open = open;
        this.waitingElevatorTimeoutRetryTimeInterval = waitingElevatorTimeoutRetryTimeInterval;
        this.enterOrLeavePointUnreachableRetryTimeInterval = enterOrLeavePointUnreachableRetryTimeInterval;
        this.isDetectionSwitchOpen = isDetectionSwitchOpen;
        this.generatePathCount = generatePathCount;
        this.isSingleDoor = isSingleDoor;
        this.isSingleNetwork = isSingleNetwork;
    }

    public static ElevatorSetting getDefault() {
        return new ElevatorSetting(
                false,
                3,
                1,
                true,
                3,
                true,
                true

        );
    }

    @Override
    public String toString() {
        return "{" +
                "梯控开关=" + open +
                ", 充电桩地图=" + chargingPileMap +
                ", 出品点地图=" + productionPointMap +
                ", 超时重新呼梯时间=" + waitingElevatorTimeoutRetryTimeInterval +
                ", 检测到无法进入电梯重新进入电梯时间=" + enterOrLeavePointUnreachableRetryTimeInterval +
                ", 是否开启进梯检测=" + isDetectionSwitchOpen +
                ", 检测是否可以进入电梯次数=" + generatePathCount +
                ", 电梯是否单向开门=" + isSingleDoor +
                ", 是否使用单网络=" + isSingleNetwork +
                ", 梯外网络=" + outsideNetwork +
                ", 梯内网络=" + insideNetwork +
                '}';
    }
}
