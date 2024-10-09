package com.reeman.points.model.request

import com.google.gson.annotations.SerializedName

data class MapsWithFixedPoints(
    @SerializedName("alias") val alias: String,
    @SerializedName("path") val path: PathModelPoint,
    @SerializedName("name")
    val name: String
) {
    override fun toString(): String {
        return "MapsWithFixedPoints(alias=$alias, path=$path, name='$name')"
    }
}