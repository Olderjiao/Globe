package com.guanwei.globe.http

import com.guanwei.globe.MyApplication
import com.guanwei.globe.utils.NetUtil
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

class NetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()



        //无网络时 使用缓存
        if (NetUtil.isNetworkConnected(MyApplication.getAppContext().applicationContext)) {
            request = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
        }

        var response = chain.proceed(request)

        if (NetUtil.isNetworkConnected(MyApplication.getAppContext().applicationContext)) {
            // 有网络时，设置超时为0
            val maxStale = 0
            response.newBuilder()
                .header("Cache-Control", "public, max-age=$maxStale")
                .removeHeader("Pragma") // 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                .build()
        } else {
            // 无网络时，设置超时为3周
            val maxStale = 60 * 60 * 24 * 21
            response.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                .removeHeader("Pragma")
                .build()
        }

        return response
    }
}