package com.reeman.points.request.service

import com.reeman.points.model.request.MapVO
import com.reeman.points.model.request.MapsWithFixedPoints
import com.reeman.points.model.request.MapsWithPoints
import com.reeman.points.model.request.MapsWithQRCodePoints
import com.reeman.points.model.request.PathModelPoint
import com.reeman.points.model.request.Point
import com.reeman.points.model.request.QRCodePoint
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {

    @GET
    fun fetchPointsAsync(@Url url:String): Observable<Map<String, List<Point>>>

    @GET
    fun fetchPointsSync(@Url url:String): Call<Map<String, List<Point>>>

    @GET
    fun fetchFixedPointsAsync(@Url url: String):Observable<PathModelPoint>

    @GET
    fun fetchQRCodePointsSync(@Url url:String):Call<Map<String,List<QRCodePoint>>>


    @GET
    fun fetchPointsWithMapsAsync(@Url url: String): Observable<List<MapsWithPoints>>

    @GET
    fun fetchFixedPointsWithMapsAsync(@Url url:String):Observable<List<MapsWithFixedPoints>>

    @GET
    fun fetchQRCodePointsWithMapsSync(@Url url:String):Call<List<MapsWithQRCodePoints>>

    @GET
    fun fetchFixedPathPointsByMapAsync(@Url url:String,@Query("name")map:String):Observable<PathModelPoint>

    @GET
    fun fetchPointsByMapAsync(@Url url:String,@Query("name")map:String):Observable<Map<String, List<Point>>>

    @GET
    fun fetchPointsByMapSync(@Url url:String,@Query("name")map:String):Call<Map<String, List<Point>>>

    @GET
    fun fetchMapListAsync(@Url url:String):Observable<List<MapVO>>


}