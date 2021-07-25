package com.guanwei.globe.utils

import com.orhanobut.logger.LogLevel
import com.orhanobut.logger.Logger

class LogUtil {
    companion object {
        /**
         * 初始化入口
         */
        fun init(isLogEnable: Boolean) {
            Logger.init("LogHttpInfo").apply {
                hideThreadInfo()
                logLevel(if (isLogEnable) LogLevel.FULL else LogLevel.NONE)
                methodOffset(2)
            }
        }

        fun d(message: String) {
            Logger.d(message)
        }

        fun i(message: String) {
            Logger.i(message)
        }
        
    }

}