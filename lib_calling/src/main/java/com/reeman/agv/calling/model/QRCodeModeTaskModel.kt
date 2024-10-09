package com.reeman.agv.calling.model

import kotlinx.serialization.Serializable


@Serializable
class QRCodeModeTaskModel(val map:String? = null,val point:String) {
    override fun toString(): String {
        return "QRCodeModeTaskModel(map=$map, point=$point)"
    }
}