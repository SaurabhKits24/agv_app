package com.reeman.dao.repository.entities;

import java.io.Serializable;
import java.util.Objects;

public class PointsVO implements Serializable {
    private String point;

    private boolean isOpenWaitingTime;

    private int waitingTime;

    public PointsVO(String point, boolean isOpenWaitingTime, int waitingTime) {
        this.point = point;
        this.isOpenWaitingTime = isOpenWaitingTime;
        this.waitingTime = waitingTime;
    }

    public PointsVO(PointsVO pointsVO) {
        this.point = pointsVO.point;
        this.isOpenWaitingTime = pointsVO.isOpenWaitingTime;
        this.waitingTime = pointsVO.waitingTime;
    }

    public PointsVO() {
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public boolean isOpenWaitingTime() {
        return isOpenWaitingTime;
    }

    public void setOpenWaitingTime(boolean openWaitingTime) {
        isOpenWaitingTime = openWaitingTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public PointsVO getDefault(String name){
        return new PointsVO(name,true,30);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PointsVO pointsVO = (PointsVO) o;

        if (isOpenWaitingTime != pointsVO.isOpenWaitingTime) return false;
        if (waitingTime != pointsVO.waitingTime) return false;
        return Objects.equals(point, pointsVO.point);
    }

    @Override
    public int hashCode() {
        int result = point != null ? point.hashCode() : 0;
        result = 31 * result + (isOpenWaitingTime ? 1 : 0);
        result = 31 * result + waitingTime;
        return result;
    }


    @Override
    public String toString() {
        return "PointsVO{" +
                "point=" + point +
                ", waitingTime=" + waitingTime +
                '}';
    }
}