package com.guanwei.globe.http.exception

import com.google.gson.JsonParseException
import com.google.gson.JsonSerializer
import org.json.JSONException
import retrofit2.HttpException
import java.io.NotSerializableException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException

class ApiException constructor(throwable: Throwable, code: Int) : Exception() {


    private var code: Int = code
    var msg: String = throwable.message.toString()

    companion object {
        //对应状态码
        private val UNAUTHORIZED: Int = 401
        private val FORBIDDEN: Int = 403
        private val NOT_FOUND: Int = 404
        private val REQUEST_TIMEOUT: Int = 408
        private val INTERNAL_SERVER_ERROR: Int = 500
        private val BAD_GETAWAY: Int = 502
        private val SERVICE_UNAVAILABLE: Int = 503
        private val GATEWAY_TIMEOUT: Int = 504

        fun handleException(e: Throwable): ApiException {
            var throwable = e
            var exc: Throwable? = null
            //获取根源异常
            while (throwable.cause != null) {
                exc = throwable
                throwable = throwable.cause!!
            }

            var ex: ApiException

            if (exc is HttpException) {//http错误
                var httpException: HttpException = exc
                ex = ApiException(exc, httpException.code())

                when (httpException.code()) {
                    UNAUTHORIZED,
                    FORBIDDEN,
                    NOT_FOUND,
                    REQUEST_TIMEOUT,
                    GATEWAY_TIMEOUT,
                    INTERNAL_SERVER_ERROR,
                    BAD_GETAWAY,
                    SERVICE_UNAVAILABLE -> {
                    }
                    else -> {
                        ex.msg = "默认网络异常"
                    }
                }
                return ex
            } else if (exc is SocketTimeoutException) {
                ex = ApiException(exc, ERROR.TIMEOUT_ERROR)
                ex.msg = "网络连接超时，请检查您的网络状态，稍后重试！"
                return ex
            } else if (exc is ConnectException) {
                ex = ApiException(exc, ERROR.TIMEOUT_ERROR)
                ex.msg = "网络连接异常，请检查您的网络状态，稍后重试！"
                return ex
            } else if (exc is TimeoutException) {
                ex = ApiException(exc, ERROR.TIMEOUT_ERROR)
                ex.msg = "网络连接超时，请检查您的网络状态，稍后重试！"
                return ex
            } else if (exc is UnknownHostException) {
                ex = ApiException(exc, ERROR.TIMEOUT_ERROR)
                ex.msg = "网络连接异常，请检查您的网络状态，稍后重试！"
                return ex
            } else if (exc is NullPointerException) {
                ex = ApiException(exc, ERROR.NULL_POINTER_EXCEPTION)
                ex.msg = "空指针异常"
                return ex
            } else if (exc is SSLHandshakeException) {
                ex = ApiException(exc, ERROR.SSL_ERROR)
                ex.msg = "证书验证失败"
                return ex
            } else if (exc is ClassCastException) {
                ex = ApiException(exc, ERROR.CAST_ERROR)
                ex.msg = "类型转换错误"
                return ex
            } else if (exc is JsonParseException
                || exc is JSONException
                || exc is JsonSerializer<*>
                || exc is NotSerializableException
                || exc is ParseException
            ) {
                ex = ApiException(exc, ERROR.PARSE_ERROR)
                ex.msg = "解析错误"
                return ex
            } else if (exc is IllegalStateException) {
                ex = ApiException(exc, ERROR.ILLEGAL_STATE_ERROR)
                ex.msg = exc.message!!
                return ex
            } else {
                ex = ApiException(exc!!, ERROR.UNKNOWN)
                ex.msg = "未知错误"
                return ex
            }

        }
    }


    /*** 约定异常  */
    object ERROR {
        /*** 未知错误  */
        const val UNKNOWN = 1000

        /*** 连接超时  */
        const val TIMEOUT_ERROR = 1001

        /*** 空指针错误  */
        const val NULL_POINTER_EXCEPTION = 1002

        /*** 证书出错  */
        const val SSL_ERROR = 1003

        /*** 类转换错误  */
        const val CAST_ERROR = 1004

        /*** 解析错误  */
        const val PARSE_ERROR = 1005

        /*** 非法数据异常  */
        const val ILLEGAL_STATE_ERROR = 1006
    }

}