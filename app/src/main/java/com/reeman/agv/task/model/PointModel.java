package com.reeman.agv.task.model;

import java.io.Serializable;
import java.util.Arrays;

public class PointModel implements Serializable {

    private final String name;
    private final String room;
    private final double[] position;
    private final String currentMap;
    private final String targetMap;

    private final boolean isAirShowerDoor;

    private PointModel(Builder builder) {
        this.name = builder.name;
        this.room = builder.room;
        this.position = builder.position;
        this.currentMap = builder.currentMap;
        this.targetMap = builder.targetMap;
        this.isAirShowerDoor = builder.isAirShowerDoor;
    }

    public String getName() {
        return name;
    }

    public String getRoom() {
        return room;
    }

    public double[] getPosition() {
        return position.clone();
    }

    public String getCurrentMap() {
        return currentMap;
    }

    public String getTargetMap() {
        return targetMap;
    }

    public boolean isAirShowerDoor() {
        return isAirShowerDoor;
    }

    public static class Builder {
        private String name;
        private String room;
        private double[] position;
        private String currentMap;
        private String targetMap;
        private boolean isAirShowerDoor;



        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder room(String room){
            this.room = room;
            return this;
        }
        public Builder position(double[] position) {
            this.position = position.clone();
            return this;
        }

        public Builder currentMap(String currentMap) {
            this.currentMap = currentMap;
            return this;
        }

        public Builder targetMap(String targetMap) {
            this.targetMap = targetMap;
            return this;
        }

        public Builder isAirShowerDoor(boolean isAirShowerDoor){
            this.isAirShowerDoor = isAirShowerDoor;
            return this;
        }

        public PointModel build() {
            return new PointModel(this);
        }
    }

    @Override
    public String toString() {
        return "PointModel{" +
                "name='" + name + '\'' +
                ", room='" + room + '\'' +
                ", position=" + Arrays.toString(position) +
                ", currentMap='" + currentMap + '\'' +
                ", targetMap='" + targetMap + '\'' +
                '}';
    }
}

