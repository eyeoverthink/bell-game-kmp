package dev.eyeoverthink.bellgame.ui

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.round

/** Format [value] with a fixed number of decimal places. Works in common code. */
fun formatFixed(value: Double, decimals: Int): String {
    require(decimals in 0..6)
    val negative = value < 0.0
    var scale = 1L
    repeat(decimals) {
        scale *= 10L
    }
    val scaled = round(abs(value) * scale).toLong()
    val whole = scaled / scale
    val body = if (decimals == 0) {
        whole.toString()
    } else {
        val fraction = (scaled % scale).toString().padStart(decimals, '0')
        "$whole.$fraction"
    }
    return if (negative && scaled != 0L) "-$body" else body
}

/** Format a fraction in the unit interval as a percent with two decimals. */
fun formatPercent(rate: Double): String = formatFixed(rate * 100.0, 2) + "%"

/** Radians to a whole-degree label. */
fun formatDegrees(radians: Double): String = formatFixed(radians * 180.0 / PI, 0) + "°"
