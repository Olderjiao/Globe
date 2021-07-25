package com.guanwei.globe.utils

import android.animation.TypeEvaluator
import gov.nasa.worldwind.geom.Position

/**
 * 动画类
 */
class GlobeEvaluator : TypeEvaluator<Any> {
    override fun evaluate(fraction: Float, startValue: Any?, endValue: Any?): Any {
        val startP = startValue as Position
        val endP = endValue as Position

        val lat = startP.latitude + fraction * (endP.latitude - startP.latitude)
        val lon = startP.longitude + fraction * (endP.longitude - startP.longitude)
        val alt = startP.altitude + fraction * (endP.altitude - startP.altitude)

        return Position(lat, lon, alt)
    }
}