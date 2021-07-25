package com.guanwei.globe.http

import com.guanwei.globe.utils.LogUtil
import com.guanwei.globe.utils.MyJsonUtil
import okhttp3.logging.HttpLoggingInterceptor

/**
 * 对OkHttp打印进行二次封装
 */
class HttpLogger : HttpLoggingInterceptor.Logger {

    override fun log(message: String) {
        var msg = message

        // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
        if ((msg.startsWith("{") && msg.endsWith("}"))
            || (msg.startsWith("[") && msg.endsWith("]"))) {
            msg = MyJsonUtil.formatJson(MyJsonUtil.decodeUnicode(msg))
        }
        LogUtil.d(msg)
    }
}