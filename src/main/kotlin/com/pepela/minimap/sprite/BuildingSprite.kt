package com.pepela.minimap.sprite

import com.pepela.minimap.parser.Building
import java.awt.image.BufferedImage


internal data class BuildingSprites(
    private val radiantTower: BufferedImage,
    private val direTower: BufferedImage,
    private val radiantRax: BufferedImage,
    private val direRax: BufferedImage,
    private val radiantAncient: BufferedImage,
    private val direAncient: BufferedImage,
) {
    fun spriteFor(building: Building, type: BuildingType): BufferedImage {
        val radiant = building.team == 2 ||
                building.name.contains("goodguys", ignoreCase = true) ||
                building.name.contains("radiant", ignoreCase = true)
        return when (type) {
            BuildingType.TOWER -> if (radiant) radiantTower else direTower
            BuildingType.RAX -> if (radiant) radiantRax else direRax
            BuildingType.ANCIENT -> if (radiant) radiantAncient else direAncient
        }
    }

    companion object {
        fun load(loader: Class<*>): BuildingSprites {
            return BuildingSprites(
                radiantTower = loader.loadResourceImage("/buildings/radiant_tower.png"),
                direTower = loader.loadResourceImage("/buildings/dire_tower.png"),
                radiantRax = loader.loadResourceImage("/buildings/radiant_rax.png"),
                direRax = loader.loadResourceImage("/buildings/dire_rax.png"),
                radiantAncient = loader.loadResourceImage("/buildings/radiant_ancient.png"),
                direAncient = loader.loadResourceImage("/buildings/dire_ancient.png")
            )
        }
    }
}

internal enum class BuildingType {
    TOWER,
    RAX,
    ANCIENT;

    companion object {
        fun fromName(name: String): BuildingType {
            return when {
                name.contains("rax", ignoreCase = true) ||
                        name.contains("barracks", ignoreCase = true) -> RAX

                name.contains("ancient", ignoreCase = true) ||
                        name.contains("fort", ignoreCase = true) -> ANCIENT

                else -> TOWER
            }
        }
    }
}
