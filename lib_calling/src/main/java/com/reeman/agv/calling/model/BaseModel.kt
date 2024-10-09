package com.reeman.agv.calling.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class BaseModel :Serializable {

    @SerializedName("token")
    var token:String?=null

    constructor(token: String?) {
        this.token = token
    }

    constructor()


    override fun toString(): String {
        return "BaseModel(token=$token)"
    }


}