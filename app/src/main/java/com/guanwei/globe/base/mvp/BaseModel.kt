package com.guanwei.globe.base.mvp

import com.guanwei.globe.http.ApiEngine
import retrofit2.Retrofit

interface BaseModel {
    val mRetrofit: Retrofit
        get() = ApiEngine.get().getRetrofit()
}