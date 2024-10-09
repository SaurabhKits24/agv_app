package com.reeman.agv.elevator.request.model;


import java.io.Serializable;

public class Response implements Serializable {

    public String code;

    public String msg;
    public Object result;

    @Override
    public String toString() {
        return "Response{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", result=" + result +
                '}';
    }
}
