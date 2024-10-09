package com.reeman.points.model.request

import com.google.gson.annotations.SerializedName

data class Point(
    @SerializedName("name")val name:String,
    @SerializedName("pose")val pose: Pose,
    @SerializedName("type")val type:String
){
    fun getPosition() = doubleArrayOf(pose.x,pose.y,pose.theta)
}

data class Pose(
    @SerializedName("x")val x:Double,
    @SerializedName("y")val y:Double,
    @SerializedName("theta")val theta:Double
)
