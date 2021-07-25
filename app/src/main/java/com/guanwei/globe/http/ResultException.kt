package com.guanwei.globe.http

import java.lang.RuntimeException

class ResultException constructor(errCode: String, errMsg: String) : RuntimeException() {
    var code: String = errCode
    var msg: String = errMsg
}