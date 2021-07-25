package com.guanwei.globe.rxjava

import com.blankj.utilcode.util.LogUtils
import com.guanwei.globe.http.ResultException
import com.guanwei.globe.http.exception.ApiException
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * 实现重写返回方法
 */
abstract class BaseObserver<T> : Observer<T> {
    override fun onSubscribe(d: Disposable) {

    }

    override fun onNext(t: T) {
        onSuccess(t)
    }

    override fun onError(e: Throwable) {
        if (e is ResultException) {
            onField("${e.msg}  错误码为：${e.code}")
        } else {
            val errorMsg = ApiException.handleException(e).msg
            error(errorMsg)
        }
    }

    override fun onComplete() {

    }

    /**
     * 请求成功
     */
    abstract fun onSuccess(success: T)

    /**
     * 返回出错
     */
    abstract fun onField(msg: String)

    private fun error(s: String) {
        LogUtils.a("MY_TAG", "BaseObserver__onError===>${s}")
    }

}