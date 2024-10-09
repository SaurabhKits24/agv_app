package com.reeman.commons.model.request

data class ResponseWithTime(val code:Int, val msg:String, val data:Long){
    override fun toString(): String {
        return "ResponseWithTime(code=$code, msg='$msg', data=$data)"
    }
}
