package com.reeman.agv.elevator.request.model;

import java.io.Serializable;

public class Passenger implements Serializable {

    private final String passenger;

    public Passenger(String passenger) {
        this.passenger = passenger;
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "passenger='" + passenger + '\'' +
                '}';
    }
}
