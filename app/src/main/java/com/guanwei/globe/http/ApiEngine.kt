package com.guanwei.globe.http

import com.guanwei.globe.BuildConfig
import com.guanwei.globe.MyApplication
import com.guanwei.globe.utils.LogUtil
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class ApiEngine private constructor() {
    private var retrofit: Retrofit? = null

    //线程安全的单例获取
    companion object {
        private var apiEngine: ApiEngine? = null
            get() {
                if (field == null) {
                    field = ApiEngine()
                }
                return field
            }

        @Synchronized
        fun get(): ApiEngine {
            return apiEngine!!
        }
    }

    init {
        //日志拦截器
        val loggingInterceptor = HttpLoggingInterceptor(HttpLogger())
        //头部信息与内容信息全部打印
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        //缓存
        val size = 1024 * 1024 * 100
        val file = File(MyApplication.getAppContext().cacheDir, "OkHttpCache")
        val cache = Cache(file, size.toLong())
        val okHttp = OkHttpClient.Builder().run {
            connectTimeout(12, TimeUnit.SECONDS)
            readTimeout(12, TimeUnit.SECONDS)
            writeTimeout(12, TimeUnit.SECONDS)
            addNetworkInterceptor(NetworkInterceptor())
            addInterceptor(loggingInterceptor)
            cache(cache)
            build()
        }

        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASEURL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    }

    fun getRetrofit(): Retrofit = retrofit!!

}