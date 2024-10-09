package com.reeman.agv.calling.model

import com.google.gson.annotations.SerializedName

open class BaseTaskModel : BaseModel {

    @SerializedName("body")
    var body:String?=null

    constructor(token: String?, body: String?) : super(token) {
        this.body = body
    }

    constructor(body: String?) : super() {
        this.body = body
    }


    override fun toString(): String {
        return "BaseTaskModel(token=$token,body=$body)"
    }


}