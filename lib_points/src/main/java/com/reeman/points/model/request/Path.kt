package com.reeman.points.model.request

import com.google.gson.annotations.SerializedName

data class Path(
    @SerializedName(value = "sourcePoint", alternate = ["sp"])
    var sourcePoint: String,

    @SerializedName(value = "destinationPoint", alternate = ["dp"])
    var destinationPoint: String,

    @SerializedName(value = "pathWidth", alternate = ["w"])
    private var pathWidth: String,

    @SerializedName(value = "expand", alternate = ["ex"])
    var expand: String? = null,
    @SerializedName(value = "length", alternate = ["len"])
    private var length: String,
    @SerializedName(value="name")
    private var name:String
) {

    fun getPathWidth(): Double {
        return pathWidth.toDoubleOrNull()?.div(100) ?: 1.0
    }

    fun getLength(): Int {
        return length.toInt()
    }

    override fun toString(): String {
        return "Path(sourcePoint='$sourcePoint', destinationPoint='$destinationPoint', pathWidth=$pathWidth, expand=$expand, length='$length', name='$name')"
    }
}
