package com.reeman.agv.elevator.exception;

import com.reeman.agv.elevator.state.Step;

public class CustomException extends IllegalStateException{

    public Step step;

    public String code;

    public String msg;

    public CustomException(Step step,String code, String msg) {
        this.step = step;
        this.code = code;
        this.msg = msg;
    }

    public CustomException(Step step, String code) {
        this.step = step;
        this.code = code;
    }
}
