package com.pepela.minimap.sprite

import java.awt.image.BufferedImage

internal data class RuneSprites(
    private val arcane: BufferedImage,
    private val amplify: BufferedImage,
    private val bounty: BufferedImage,
    private val haste: BufferedImage,
    private val illusion: BufferedImage,
    private val invisibility: BufferedImage,
    private val regeneration: BufferedImage,
    private val shield: BufferedImage,
    private val water: BufferedImage,
) {
    fun spriteFor(type: RuneType): BufferedImage {
        return when (type) {
            RuneType.ARCANE -> arcane
            RuneType.AMPLIFY -> amplify
            RuneType.BOUNTY -> bounty
            RuneType.HASTE -> haste
            RuneType.ILLUSION -> illusion
            RuneType.INVISIBILITY -> invisibility
            RuneType.REGENERATION -> regeneration
            RuneType.SHIELD -> shield
            RuneType.WATER -> water
        }
    }

    companion object {
        fun load(loader: Class<*>): RuneSprites {
            return RuneSprites(
                arcane = loader.loadResourceImage("/rune/rune_arcane.png"),
                amplify = loader.loadResourceImage("/rune/rune_amplify.png"),
                bounty = loader.loadResourceImage("/rune/rune_bounty.png"),
                haste = loader.loadResourceImage("/rune/rune_haste.png"),
                illusion = loader.loadResourceImage("/rune/rune_illusion.png"),
                invisibility = loader.loadResourceImage("/rune/rune_invisibility.png"),
                regeneration = loader.loadResourceImage("/rune/rune_regeneration.png"),
                shield = loader.loadResourceImage("/rune/rune_shield.png"),
                water = loader.loadResourceImage("/rune/rune_water.png"),
            )
        }
    }
}

internal enum class RuneType {
    ARCANE,
    AMPLIFY,
    BOUNTY,
    HASTE,
    ILLUSION,
    INVISIBILITY,
    REGENERATION,
    SHIELD,
    WATER,
    ;

    companion object {
        fun fromName(type: String): RuneType {
            return when {
                type.contains("arcane", ignoreCase = true) -> ARCANE
                type.contains("amplify", ignoreCase = true) -> AMPLIFY
                type.contains("bounty", ignoreCase = true) -> BOUNTY
                type.contains("haste", ignoreCase = true) -> HASTE
                type.contains("illusion", ignoreCase = true) -> ILLUSION
                type.contains("invisibility", ignoreCase = true) -> INVISIBILITY
                type.contains("regeneration", ignoreCase = true) -> REGENERATION
                type.contains("shield", ignoreCase = true) -> SHIELD
                type.contains("water", ignoreCase = true) -> WATER
                else -> WATER
            }
        }
    }
}