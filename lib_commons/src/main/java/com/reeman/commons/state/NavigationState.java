package com.reeman.commons.state;

public enum NavigationState {
    FAILURE(-1),
    INITIAL(0),
    START(1),
    PAUSE(2),
    COMPLETE(3),
    CANCEL(4),
    RESUME(5),
    RECEIVE(6);

    private final int value;

    NavigationState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
