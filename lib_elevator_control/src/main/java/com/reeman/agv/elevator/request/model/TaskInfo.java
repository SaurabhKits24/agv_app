package com.reeman.agv.elevator.request.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TaskInfo implements Serializable {
    @SerializedName("passenger")
    public PassengerDTO passenger;
    @SerializedName("from")
    public FromDTO from;
    @SerializedName("to")
    public ToDTO to;
    @SerializedName("at")
    public String at;
    @SerializedName("in")
    public String in;
    @SerializedName("out")
    public Object out;
    @SerializedName("status")
    public Object status;

    public static class PassengerDTO {
        @SerializedName("id")
        public String id;
    }

    public static class FromDTO {
        @SerializedName("value")
        public Integer value;
    }

    public static class ToDTO {
        @SerializedName("value")
        public Integer value;
    }

    @Override
    public String toString() {
        return "TaskInfo{" +
                "passenger=" + passenger +
                ", from=" + from +
                ", to=" + to +
                ", at='" + at + '\'' +
                ", in='" + in + '\'' +
                ", out=" + out +
                ", status=" + status +
                '}';
    }
}
