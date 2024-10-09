package com.reeman.agv.calling.exception;

public class NoRouteException extends IllegalStateException{

    public String route;

    public NoRouteException(String route) {
        this.route = route;
    }

    public NoRouteException() {
    }
}
