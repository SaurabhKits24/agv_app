package com.reeman.points.model.request

import com.google.gson.annotations.SerializedName

data class MapsWithPoints(
    @SerializedName("alias")
    val alias: String,
    @SerializedName("points")
    val points: List<Point>?,
    @SerializedName("name")
    val name: String
) {
    override fun toString(): String {
        return "MapsWithPoints(alias=$alias, points=$points, name='$name')"
    }
}