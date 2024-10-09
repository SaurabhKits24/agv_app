package com.reeman.agv.calling.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CallingModePointModel :Serializable{

    @SerializedName("elevatorModeSwitch")
    var elevatorModeSwitch= false

    @SerializedName("model")
    var model : Map<String,List<String>>?=null

    constructor(elevatorModeSwitch: Boolean, model: Map<String, List<String>>?) {
        this.elevatorModeSwitch = elevatorModeSwitch
        this.model = model
    }
}