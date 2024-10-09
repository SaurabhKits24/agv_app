package com.reeman.agv.calling.model

import com.google.gson.annotations.SerializedName

class MulticastSendInfo(
    @SerializedName("hostname")
    var hostname: String,
    @SerializedName("alias")
    var alias: String,
    @SerializedName("key")
    var key: String,
    @SerializedName("token")
    var token: String,
    @SerializedName("robotType")
    val robotType:Int
) {
    override fun toString(): String {
        return "MulticastSendInfo{" +
                "hostname='" + hostname + '\'' +
                ", alias='" + alias + '\'' +
                ", key='" + key + '\'' +
                ", token='" + token + '\'' +
                ", robotType='" + robotType + '\'' +
                '}'
    }
}