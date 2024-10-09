package com.reeman.agv.elevator.request.model;

public class TakeElevatorInside extends Passenger {

    private final String to;

    public TakeElevatorInside(String passenger, String to) {
        super(passenger);
        this.to = to;
    }

    @Override
    public String toString() {
        return "TakeElevator{" +
                ", to='" + to + '\'' +
                "} " + super.toString();
    }
}
