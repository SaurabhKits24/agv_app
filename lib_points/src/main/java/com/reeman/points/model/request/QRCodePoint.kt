package com.reeman.points.model.request

import com.google.gson.annotations.SerializedName

data class QRCodePoint(
    @SerializedName("name")val name:String,
    @SerializedName("start")val start:String,
    @SerializedName("goal")val goal:String
){
    override fun toString(): String {
        return "QRCodePoint(name='$name', start='$start', goal='$goal')"
    }
}
