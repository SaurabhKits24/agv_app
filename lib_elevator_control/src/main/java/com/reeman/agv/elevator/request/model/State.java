package com.reeman.agv.elevator.request.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class State implements Serializable {


    @SerializedName("name")
    public String name;
    @SerializedName("highest")
    public HighestDTO highest;
    @SerializedName("lowest")
    public LowestDTO lowest;
    @SerializedName("currentFloor")
    public CurrentFloorDTO currentFloor;
    @SerializedName("nextDirection")
    public String nextDirection;
    @SerializedName("state")
    public String state;
    @SerializedName("stateMode")
    public StateModeDTO stateMode;
    @SerializedName("binding")
    public List<BindingDTO> binding;
    @SerializedName("requests")
    public Object requests;
    @SerializedName("toBeNotified")
    public List<?> toBeNotified;
    @SerializedName("notified")
    public NotifiedDTO notified;
    @SerializedName("transferPassengers")
    public List<?> transferPassengers;
    @SerializedName("onPassage")
    public List<OnPassageDTO> onPassage;
    @SerializedName("pressedFloor")
    public List<?> pressedFloor;
    @SerializedName("layerStatus")
    public Integer layerStatus;
    @SerializedName("peopleStatus")
    public Object peopleStatus;
    @SerializedName("floor")
    public Integer floor;
    @SerializedName("currentDirection")
    public String currentDirection;
    @SerializedName("latestTime")
    public String latestTime;
    @SerializedName("fontDoorStatus")
    public Integer fontDoorStatus;
    @SerializedName("backDoorStatus")
    public Integer backDoorStatus;
    @SerializedName("tenantId")
    public String tenantId;

    public static class HighestDTO {
        @SerializedName("value")
        public Integer value;
    }

    public static class LowestDTO {
        @SerializedName("value")
        public Integer value;
    }

    public static class CurrentFloorDTO {
        @SerializedName("value")
        public Integer value;
    }

    public static class StateModeDTO {
    }

    public static class NotifiedDTO {
        @SerializedName("id")
        public String id;
    }

    public static class BindingDTO {
        @SerializedName("id")
        public String id;
    }

    public static class OnPassageDTO {
        @SerializedName("id")
        public String id;
    }
}
