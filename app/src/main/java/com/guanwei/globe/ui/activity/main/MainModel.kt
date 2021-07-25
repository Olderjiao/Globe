package com.guanwei.globe.ui.activity.main

import com.guanwei.globe.http.BaseResponse
import com.guanwei.globe.http.service.MainService
import io.reactivex.Observable
import okhttp3.RequestBody

class MainModel : MainContract.MainModel {
    override fun postLocation(json: RequestBody): Observable<BaseResponse<Any>> {
        return mRetrofit!!.create(MainService::class.java).postLocation(json)
    }

}