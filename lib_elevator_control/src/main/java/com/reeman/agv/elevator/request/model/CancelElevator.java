package com.reeman.agv.elevator.request.model;

public class CancelElevator extends Passenger {


    private final String cause;

    public CancelElevator(String passenger, String cause) {
        super(passenger);
        this.cause = cause;
    }

    @Override
    public String toString() {
        return "CancelElevator{" +
                "cause='" + cause + '\'' +
                "} " + super.toString();
    }
}
