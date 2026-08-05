package com.pepela.minimap.renderer

import kotlin.math.roundToInt

internal class DotaMapProjection(
    private val minX: Float = 7808f,
    private val maxX: Float = 24960f,
    private val minY: Float = 8192f,
    private val maxY: Float = 24576f,
) {
    fun toPixel(x: Float, y: Float, width: Int, height: Int): PixelPoint {
        val nx = ((x - minX) / (maxX - minX)).coerceIn(0f, 1f)
        val ny = ((maxY - y) / (maxY - minY)).coerceIn(0f, 1f)
        return PixelPoint((nx * (width - 1)).roundToInt(), (ny * (height - 1)).roundToInt())
    }
}
