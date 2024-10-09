package com.reeman.points.request.url

object Url {
    fun getPoints(ip: String) = "http://$ip/reeman/position"

    fun getFixedPoints(ip: String) = "http://$ip/reeman/path_model"

    fun getQRCodePoints(ip: String) = "http://$ip/reeman/agv_tag"


    fun getPointsWithMaps(ip: String) = "http://$ip/reeman/all_position"

    fun getFixedPointsWithMaps(ip:String)  = "http://$ip/reeman/all_path_model"

    fun getQRCodePointsWithMaps(ip:String) = "http://$ip/reeman/all_agv_tag"

    fun getPointsByMap(ip:String) = "http://$ip/reeman/map_position"

    fun getMapList(ip:String) = "http://$ip/reeman/map_list"
}