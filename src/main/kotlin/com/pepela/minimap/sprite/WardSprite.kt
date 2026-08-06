package com.pepela.minimap.sprite

import com.pepela.minimap.parser.Ward
import java.awt.image.BufferedImage

internal data class WardSprites(
    private val radiantObserverWard: BufferedImage,
    private val direObserverWard: BufferedImage,
    private val radiantSentryWard: BufferedImage,
    private val direSentryWard: BufferedImage,
) {
    fun spriteFor(ward: Ward, type: WardType): BufferedImage {
        val radiant = ward.team == 2 ||
                ward.name.contains("goodguys", ignoreCase = true) ||
                ward.name.contains("radiant", ignoreCase = true)
        return when (type) {
            WardType.OBSERVER -> if (radiant) radiantObserverWard else direObserverWard
            WardType.SENTRY -> if (radiant) radiantSentryWard else direSentryWard
        }
    }

    companion object {
        fun load(loader: Class<*>): WardSprites {
            return WardSprites(
                radiantObserverWard = loader.loadResourceImage("/wards/radiant_observer.png"),
                direObserverWard = loader.loadResourceImage("/wards/dire_observer.png"),
                radiantSentryWard = loader.loadResourceImage("/wards/radiant_sentry.png"),
                direSentryWard = loader.loadResourceImage("/wards/dire_sentry.png")
            )
        }
    }
}

internal enum class WardType {
    OBSERVER,
    SENTRY,
    ;

    companion object {
        fun fromName(name: String): WardType {
            return when {
                name.contains("TrueSight", ignoreCase = true) -> SENTRY
                else -> OBSERVER
            }
        }
    }
}
