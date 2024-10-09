package com.reeman.points.model.request

import com.google.gson.annotations.SerializedName

data class MapsWithQRCodePoints(
    @SerializedName("name")val name:String,
    @SerializedName("alias")val alias:String,
    @SerializedName("tags")val qrCodePoints:List<QRCodePoint>
){
    override fun toString(): String {
        return "MapsWithQRCodePoints(name='$name', alias='$alias', qrCodePoints=$qrCodePoints)"
    }
}
