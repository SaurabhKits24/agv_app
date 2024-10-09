package com.reeman.agv.elevator.request.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ElevatorIdResult implements Serializable {


    @SerializedName("elevatorId")
    public Long elevatorId;

    @Override
    public String toString() {
        return "ElevatorIdResult{" +
                "elevatorId=" + elevatorId +
                '}';
    }
}
