package com.pepela.minimap.renderer

import java.awt.image.BufferedImage
import java.util.IdentityHashMap
import kotlin.math.roundToInt

private val grayscaleCache = IdentityHashMap<BufferedImage, BufferedImage>()

internal fun BufferedImage.grayscale(): BufferedImage {
    return grayscaleCache.getOrPut(this) {
        val gray = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val argb = getRGB(x, y)
                val alpha = argb ushr 24 and 0xff
                val red = argb ushr 16 and 0xff
                val green = argb ushr 8 and 0xff
                val blue = argb and 0xff
                val luma = (red * 0.299f + green * 0.587f + blue * 0.114f).roundToInt()
                gray.setRGB(x, y, alpha shl 24 or (luma shl 16) or (luma shl 8) or luma)
            }
        }
        gray
    }
}
