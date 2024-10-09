package com.reeman.points.model.custom

import com.reeman.points.model.request.MapsWithFixedPoints
import com.reeman.points.model.request.MapsWithPoints
import com.reeman.points.model.request.Path

class GenericPointsWithMap(
    val name:String,
    val alias:String,
    val pointList:List<GenericPoint>,
    val pathList:List<Path>?
){
    constructor(mapsWithPoints: MapsWithPoints):this(
        name = mapsWithPoints.name,
        alias = mapsWithPoints.alias,
        pointList = mapsWithPoints.points?.map { GenericPoint(it) }?: emptyList(),
        pathList = null
    )

    constructor(mapsWithFixedPoints: MapsWithFixedPoints):this(
        name = mapsWithFixedPoints.name,
        alias = mapsWithFixedPoints.alias,
        pointList = mapsWithFixedPoints.path.point?.map { GenericPoint(it) }?: emptyList(),
        pathList = mapsWithFixedPoints.path.path?: emptyList()
    )

    override fun toString(): String {
        return "GenericPointsWithMap(map='$name', alias='$alias', pointList=$pointList, pathList=$pathList)"
    }


}
