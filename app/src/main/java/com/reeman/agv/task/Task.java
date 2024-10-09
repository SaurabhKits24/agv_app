package com.reeman.agv.task;

import android.content.Intent;


import com.reeman.agv.calling.CallingInfo;
import com.reeman.dao.repository.entities.RouteWithPoints;
import com.reeman.commons.state.RobotInfo;
import com.reeman.commons.state.State;

import kotlin.Pair;

public interface Task {

    /**
     * 初始化任务信息
     *
     * @param intent
     */
    void initTask(Intent intent);

    /**
     * 获取下一个配送点
     *
     * @return
     */
    default Pair<String, String> getNextPointWithElevator() {
        return null;
    }

    /**
     * 获取下一个配送点
     *
     * @return
     */
    String getNextPointWithoutElevator();


    /**
     * 到达配送点
     *
     * @param pointName
     */
    default void arrivedPoint(String pointName) {
    }

    /**
     * 直接返回出品点时先清除所有点
     */
    default void clearAllPoints() {
    }

    /**
     * 是否还有未导航点位
     *
     * @return
     */
    boolean hasNext();

    /**
     * 查询机器人路线任务结束后前往充电桩还是出品点,必须先判断hasNext()是否返回false
     * @return
     */
    default String getRouteTaskFinishAction() {
        return null;
    }

    /**
     * 配送完成的点位数量
     *
     * @return
     */
    int getDeliveryPointSize();

    /**
     * 导航速度
     *
     * @return
     */
    String getSpeed();

    /**
     * 获取路线名称(路线模式)
     *
     * @return
     */
    default String getRouteName() {
        return "";
    }

    /**
     * 普通模式是否开启顶升控制
     *
     * @return
     */
    default boolean isNormalModeWithManualLiftControl() {
        return false;
    }

    /**
     * 开启顶升功能(二维码模式)
     *
     * @return
     */
    default boolean withLiftFunction() {
        return false;
    }

    /**
     * 是否带返回出品点的按钮
     *
     * @return
     */
    default boolean showReturnBtn() {
        return true;
    }

    /**
     * 是否显示跳过当前目标点的按钮
     * @return
     */
    default boolean showSkipCurrentTargetBtn(){
        return false;
    }

    /**
     * 跳过当前目标点
     */
    default void skipCurrentTarget(){

    }

    default long getCountDownTime() {
        return 0L;
    }

    default long getPlayArrivedTipTime(){
        return -1L;
    }

    /**
     * 已到达最后一个配送点且设置为到达最后一个点后原地停留不返回出品点
     *
     * @return
     */
    default boolean isArrivedLastPointAndStay() {
        return false;
    }

    /**
     * 二维码模式设置机器从二维码离开时的方向和距离
     */
    default void setOrientationAndDistanceCalibration() {
    }

    /**
     * 二维码模式设置机器宽度和激光宽度
     *
     * @param reset
     */
    default void setRobotWidthAndLidar(boolean reset) {
    }

    /**
     * 任务结束,重置所有参数
     */
    default void resetAllParameter(RobotInfo robotInfo, CallingInfo callingInfo) {
        robotInfo.setSwitchingMap(false);
        robotInfo.setRepositioning(false);
        robotInfo.setState(robotInfo.isCharging() ? State.CHARGING : State.IDLE);
        robotInfo.setGetPlan(false);
        robotInfo.setQRCodeNavigating(false);
        callingInfo.getHeartBeatInfo().setTaskExecuting(false);
        callingInfo.getHeartBeatInfo().setCurrentTask(null);
    }

    /**
     * 梯控模式时进梯前后设置机器大小
     *
     * @param expansion
     */
    default void updateRobotSize(boolean expansion) {

    }

    default RouteWithPoints getRouteTask() {
        return null;
    }

    default void resetStopNearByParameter() {

    }

    /**
     * 路线任务模式任务结束后是否重新开始
     * @return
     */
    default boolean isExecuteAgainSwitch(){
        return false;
    }

    default boolean shouldRemoveFirstCallingTask(){
        return false;
    }
}
