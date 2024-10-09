package com.reeman.commons.settings;


public class ModeQRCodeSetting extends BaseModeSetting {

    //二维码模式下控制机器人宽度增量,导航默认值0.03
    public float length;

    public float width;

    //机器从二维码离开的方向和距离,导航默认值0.7
    public float orientationAndDistanceCalibration;

    //机器到达二维码后开始导航时的朝向,默认 false 倒退进入
    public boolean direction;

    //机器在二维码模式是否打开恢复模式,默认打开
    public boolean rotate;

    //二维码模式下抬起物品后修改激光宽度
    public float lidarWidth;

    //是否打开就近停靠
    public boolean stopNearby;
    //是否打开呼叫按键绑定
    public boolean callingBind;
    //是否打开顶升控制
    public boolean lift;

    public ModeQRCodeSetting(float speed, boolean startTaskCountDownSwitch, int startTaskCountDownTime, float length, float width, float orientationAndDistanceCalibration, boolean direction, boolean rotate, float lidarWidth, boolean stopNearby, boolean lift, boolean callingBind) {
        super(speed, startTaskCountDownSwitch, startTaskCountDownTime);
        this.length = length;
        this.width = width;
        this.orientationAndDistanceCalibration = orientationAndDistanceCalibration;
        this.direction = direction;
        this.rotate = rotate;
        this.lidarWidth = lidarWidth;
        this.stopNearby = stopNearby;
        this.lift = lift;
        this.callingBind = callingBind;
    }

    public static ModeQRCodeSetting getDefault(){
        return new ModeQRCodeSetting(
                0.4f,
                false,
                5,
                0,
                0,
                0.7f,
                false,
                true,
                0.3f,
                false,
                true,
                false
        );
    }

    @Override
    public String toString() {
        return "ModeQRCodeSetting{" +
                "length=" + length +
                ", width=" + width +
                ", orientationAndDistanceCalibration=" + orientationAndDistanceCalibration +
                ", direction=" + direction +
                ", rotate=" + rotate +
                ", lidarWidth=" + lidarWidth +
                ", stopNearby=" + stopNearby +
                ", lift=" + lift +
                ", speed=" + speed +
                ", startTaskCountDownSwitch=" + startTaskCountDownSwitch +
                ", startTaskCountDownTime=" + startTaskCountDownTime +
                '}';
    }
}
