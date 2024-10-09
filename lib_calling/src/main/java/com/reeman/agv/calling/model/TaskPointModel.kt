package com.reeman.agv.calling.model

import java.io.Serializable

class TaskPointModel(val map:String?,val point:String) : Serializable {


    override fun toString(): String {
        return "CallingModelTaskPointModel(map=$map, point=$point)"
    }


}