package com.pepela.minimap.sprite

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

internal fun Class<*>.loadResourceImage(path: String): BufferedImage {
    val stream = getResourceAsStream(path) ?: error("Bundled resource not found: $path")
    stream.use {
        return ImageIO.read(it) ?: error("Bundled resource is not a readable image: $path")
    }
}
