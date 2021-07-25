package com.guanwei.globe.http

data class BaseResponse<T>(var msg: String, var code: String, var data: T)