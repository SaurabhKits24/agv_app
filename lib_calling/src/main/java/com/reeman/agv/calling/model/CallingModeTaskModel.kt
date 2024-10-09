package com.reeman.agv.calling.model

class CallingModeTaskModel(val isButtonCalling:Boolean, val taskPointModel:TaskPointModel,
                           mToken:String) : BaseModel(token = mToken) {


    override fun toString(): String {
        return "CallingModeTaskModel(callingModelTaskPointModel=$taskPointModel) ${super.toString()}"
    }
}