package com.reeman.commons.event.model;


import java.io.Serializable;
import java.util.List;

public class Room implements Serializable {

    public String name;
    public List<Double> coordination;
    public int type;

    public Room(Room room){
        this.name = room.name;
        this.coordination = room.coordination;
        this.type = room.type;
    }

    public Room() {
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", coordination=" + coordination +
                ", type=" + type +
                '}';
    }
}
