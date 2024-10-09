package com.reeman.agv.elevator.request.model;

public class TakeElevator extends Passenger {

    private final String from;

    private final String to;

    public TakeElevator(String passenger, String from, String to) {
        super(passenger);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "TakeElevator{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                "} " + super.toString();
    }
}
