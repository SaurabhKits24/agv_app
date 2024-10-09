package com.reeman.agv.calling.model

import com.google.gson.annotations.SerializedName

class ResponseModel: BaseTaskModel {

    @SerializedName("code")
    var code = 0

    constructor(token: String?, body: String?, code: Int) : super(token, body) {
        this.code = code
    }

    constructor(body: String?, code: Int) : super(body) {
        this.code = code
    }

    override fun toString(): String {
        return "PointsResponseModel(code=$code)"
    }


}