package com.reeman.agv.contract;

import android.content.Context;
import android.content.Intent;

import com.reeman.agv.calling.event.CallingTaskEvent;
import com.reeman.agv.calling.event.ChargeTaskEvent;
import com.reeman.agv.calling.event.NormalTaskEvent;
import com.reeman.agv.calling.event.QRCodeTaskEvent;
import com.reeman.agv.calling.event.RouteTaskEvent;
import com.reeman.commons.event.ApplyMapEvent;
import com.reeman.commons.event.FixedPathResultEvent;
import com.reeman.commons.event.SensorsEvent;
import com.reeman.commons.event.WheelStatusEvent;
import com.reeman.agv.elevator.state.Step;
import com.reeman.commons.event.model.Room;
import com.reeman.agv.presenter.IPresenter;
import com.reeman.dao.repository.entities.RouteWithPoints;
import com.reeman.commons.state.TaskMode;
import com.reeman.agv.view.IView;
import com.reeman.agv.viewModel.TaskPauseInfoModel;

import java.util.List;

public interface TaskExecutingContract {

    interface Presenter extends IPresenter {

        /**
         * 开始任务
         *
         * @param context
         * @param intent
         */
        void startTask(Context context, Intent intent);

        /**
         * 导航取消回调
         *
         * @param code
         */
        void onNavigationCancelResult(int code);

        /**
         * 导航完成回调
         *
         * @param code
         * @param name
         * @param mileage
         */
        void onNavigationCompleteResult(int code, String name, float mileage);

        /**
         * 对接二维码结果
         *
         * @param result true:成功;false:失败
         * @param tag
         */
        void onQRCodeNavigationResult(boolean result, String tag);

        /**
         * 顶升结果
         *
         * @param action 1:抬起;0:放下
         * @param result 1:完成;0:未完成
         */
        void onLiftResult(int action, int result);

        /**
         * 开始导航结果回调
         *
         * @param code
         * @param name
         */
        void onNavigationStartResult(int code, String name);

        /**
         * 急停开关按下
         */
        void onEmergencyButtonDown();

        /**
         * 急停开关弹起
         */
        void onEmergencyButtonUp();

        /**
         * 暂停
         */
        void onPauseClick();

        /**
         * 恢复
         */
        void onResumeClick();

        /**
         * 去下一个点
         */
        void onGotoNextPointClick();

        /**
         * 取消
         */
        void onCancelClick();

        /**
         * 返回出品点
         */
        void onReturnProductPointClick();

        /**
         * 重新呼梯
         */
        void onRecallElevatorClick();

        /**
         * 跳过当前目标点
         */
        void onSkipCurrentTargetClick();

        /**
         * 顶升抬起
         */
        void onLiftUpClick();

        /**
         * 顶升放下
         */
        void onLiftDownClick();

        /**
         * 充电
         */
        void onPowerConnect();

        /**
         * 障碍物
         */
        void onEncounterObstacle();

        /**
         * 坐标
         *
         * @param position
         */
        void onPositionUpdate(double[] position);

        /**
         * 时间更新
         */
        void onTimeUpdate(long currentTimeMills);

        /**
         * 定位丢失
         */
        void onMissPose();

        /**
         * 对接充电桩失败
         */
        void onDockFailed();

        /**
         * 触发防跌
         */
        void onAntiFall();

        /**
         * 路线穿过的特殊区
         */
        void onSpecialPlan(List<Room> roomList);

        /**
         * 是否可以规划出导航路径
         *
         * @param success
         */
        void onGetPlanResult(boolean success);

        /**
         * 收到initpose(地图初始化完成)
         *
         * @param currentPosition
         */
        void onCustomInitPose(double[] currentPosition);

        /**
         * 更新固定路线全局路径
         *
         * @param event
         */
        void onShortDij(FixedPathResultEvent event);

        /**
         * 切换地图结果
         *
         * @param event
         */
        void onApplyMapResult(ApplyMapEvent event);

        /**
         * 传感器状态
         *
         * @param event
         */
        void onSensorsError(SensorsEvent event);

        /**
         * 轮子异常
         *
         * @param event
         */
        void onWheelError(WheelStatusEvent event);

        void onKeyUpCodeF2();

        void detailCustomCallingTask(CallingTaskEvent event);

        void detailCustomNormalTask(NormalTaskEvent event);

        void detailCustomRouteTask(RouteTaskEvent event);

        void detailCustomQRCodeTask(QRCodeTaskEvent event);

        void detailCustomChargeTask(String token);

        void detailCustomReturnTask(String token);

        void onWifiConnectionSuccess();

        void onGlobalPathEvent(List<Double> path);

    }

    interface View extends IView {


        /**
         * 带梯控开始导航
         *
         * @param targetFloor
         * @param targetPoint
         */
        void updateRunningView(String targetFloor, String targetPoint, Step step);

        /**
         * 到达目标点
         *
         * @param taskMode
         * @param routeName
         * @param startTime
         * @param currentPoint
         * @param nextFloor
         * @param nextPoint
         * @param showReturnBtn
         * @param showLiftUpBtn
         * @param showLiftDownBtn
         * @param hasNextPoint
         */
        void arrivedTargetPoint(
                TaskMode taskMode,
                String routeName,
                long startTime,
                String currentFloor,
                String currentPoint,
                String nextFloor,
                String nextPoint,
                boolean showReturnBtn,
                boolean showLiftUpBtn,
                boolean showLiftDownBtn,
                boolean hasNextPoint,
                boolean autoReturn
        );

        /**
         * 更新倒计时
         *
         * @param seconds
         */
        void updateCountDownTimer(long seconds);

        /**
         * 无法暂停任务,提示
         *
         * @param tip
         */
        void showCannotPauseTip(String tip);

        /**
         * 暂停任务
         */
        void showPauseTask(
                TaskPauseInfoModel model,
                String DialogTip
        );

        void updatePauseTip(String tip, boolean popupDialog, long countDowntime);

        /**
         * 结束任务
         *
         * @param result
         * @param prompt
         * @param voice
         * @param routeWithPoints
         */
        void showTaskFinishedView(int result, String prompt, String voice, RouteWithPoints routeWithPoints, TaskMode taskMode);

        /**
         * 顶升控制提示
         *
         * @param liftUp
         */
        void showManualLiftControlTip(boolean liftUp);

        /**
         * 顶升控制完成
         *
         * @param success true: 成功; false: 失败(超时)
         */
        void dismissManualLiftControlTip(boolean success);

        /**
         * 更新乘梯提示
         *
         * @param step
         * @param targetFloor
         */
        void updateTakeElevatorStep(Step step, String targetFloor);

        /**
         * 更新提示
         *
         * @param tip
         */
        void updateRunningTip(String tip);

        /**
         * 隐藏门控提示
         */
        void dismissRunningTip();

        /**
         * 提示顶升模块状态
         *
         * @param state
         */
        void showLiftModelState(int state);

        /**
         * 提示出梯相应失败的报错
         *
         * @param throwable
         */
        void showLeaveElevatorFailed(Throwable throwable);

    }
}
