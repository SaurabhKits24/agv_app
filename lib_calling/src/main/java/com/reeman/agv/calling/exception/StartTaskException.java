package com.reeman.agv.calling.exception;

/**
 * 无法开始任务
 */
public class StartTaskException extends IllegalStateException{
    private final int code;

    private int reasonCode;

    public int getCode() {
        return code;
    }

    public int getReasonCode() {
        return reasonCode;
    }

    public StartTaskException(int code, int reasonCode) {
        this.code = code;
        this.reasonCode = reasonCode;
    }

    public StartTaskException(int code) {
        this.code = code;
    }
}
