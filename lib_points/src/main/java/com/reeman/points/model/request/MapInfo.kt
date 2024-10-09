package com.reeman.points.model.request

import com.google.gson.annotations.SerializedName

data class MapInfo(
    @SerializedName("name") val name: String,
    @SerializedName("alias") val alias: String
) {
    override fun toString(): String {
        return "MapInfo(name='$name', alias='$alias')"
    }
}
