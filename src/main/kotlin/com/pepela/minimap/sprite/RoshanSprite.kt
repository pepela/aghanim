package com.pepela.minimap.sprite

import java.awt.image.BufferedImage

internal data class RoshanSprite(
    private val roshan: BufferedImage,
) {
    fun sprite(): BufferedImage = roshan

    companion object {
        fun load(loader: Class<*>): RoshanSprite =
            RoshanSprite(roshan = loader.loadResourceImage("/roshan/roshan.png"))
    }
}
