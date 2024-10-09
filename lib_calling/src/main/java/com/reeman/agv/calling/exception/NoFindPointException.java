package com.reeman.agv.calling.exception;

import java.util.List;

public class NoFindPointException extends IllegalStateException{

    private final List<String> points;

    public List<String> getPoints() {
        return points;
    }

    public NoFindPointException(List<String> points) {
        this.points = points;
    }
}
