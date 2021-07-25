package com.guanwei.globe.http.service

import com.guanwei.globe.http.BaseResponse
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface MainService {

    @POST("api/publish/shp/geoJson")
    fun postLocation(@Body json: RequestBody): Observable<BaseResponse<Any>>
}