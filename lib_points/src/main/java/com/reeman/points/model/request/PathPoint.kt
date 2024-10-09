package com.reeman.points.model.request

import com.google.gson.annotations.SerializedName

data class PathPoint(
    @SerializedName("type")
     var type: String,

    @SerializedName(value = "vehicleOrientationAngle", alternate = ["a"])
    private var vehicleOrientationAngle: String,

    @SerializedName(value = "xPosition", alternate = ["x"])
    private var xPosition: String,

    @SerializedName(value = "yPosition", alternate = ["y"])
    private var yPosition: String,

    @SerializedName(value = "expand", alternate = ["ex"])
     var expand: String?,
    @SerializedName(value = "name")
     var name: String
) {
    fun getAngle() =
         vehicleOrientationAngle.toDouble() / 1000

    fun getXPosition() =
         xPosition.toDouble() / 1000


    fun getYPosition() =
        yPosition.toDouble() / 1000

    fun getPosition() =
        doubleArrayOf(getXPosition(), getYPosition(), getAngle())


    override fun toString(): String {
        return "PathPoint(type='$type', vehicleOrientationAngle='$vehicleOrientationAngle', xPosition='$xPosition', yPosition='$yPosition', expand=$expand, name='$name')"
    }
}