package com.pepela.minimap.sprite

import com.pepela.minimap.parser.Hero
import java.awt.image.BufferedImage

internal data class HeroSprites(
    private val radiantHeroes: Map<String, BufferedImage>,
    private val direHeroes: Map<String, BufferedImage>,
) {
    fun spriteFor(hero: Hero): BufferedImage? {
        val radiant = hero.team == 2 ||
                hero.name.contains("goodguys", ignoreCase = true) ||
                hero.name.contains("radiant", ignoreCase = true)
        return if (radiant) {
            radiantHeroes[hero.name]
        } else {
            direHeroes[hero.name]
        }
    }

    companion object {
        fun load(loader: Class<*>, heroes: MutableCollection<Hero>): HeroSprites {
            return with(heroes.toList()) {
                HeroSprites(
                    radiantHeroes = makeMap(loader, this, 2),
                    direHeroes = makeMap(loader, this, 3),
                )
            }

        }

        private fun makeMap(
            loader: Class<*>,
            heroes: List<Hero>,
            team: Int
        ): Map<String, BufferedImage> =
            heroes
                .filter { it.team == team }
                .mapNotNull { hero ->
                    val normalizedName = hero.name
                        .removePrefix("CDOTA_Unit_Hero_")
                        .replace("_", "")
                        .lowercase()
                    val path = "/heroes/$normalizedName.png"

                    runCatching {
                        hero.name to loader.loadResourceImage(path)
                    }.onFailure {
                        println("Failed to load image for hero ${hero.name}")
                    }.getOrNull()
                }
                .toMap()
    }
}
