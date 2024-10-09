package com.reeman.commons.exceptions

class ElevatorNetworkNotSettException(val isOutSideNetworkNotSet:Boolean,val isInsideNetWorkNotSet:Boolean):IllegalStateException() {
    override fun toString(): String {
        return "ElevatorNetworkNotSettException(isOutSideNetworkNotSet=$isOutSideNetworkNotSet, isInsideNetWorkNotSet=$isInsideNetWorkNotSet)"
    }
}