package com.reeman.points.model.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PathModelPoint(
    @SerializedName("point") val point: List<PathPoint>?,
    @SerializedName("path") val path: List<Path>?
) : Serializable{


    override fun toString(): String {
        return "PathModelPoint(point=$point, path=$path)"
    }
}