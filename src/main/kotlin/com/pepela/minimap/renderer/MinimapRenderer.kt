package com.pepela.minimap.renderer

import com.pepela.minimap.parser.Building
import com.pepela.minimap.parser.GameState
import com.pepela.minimap.parser.Hero
import com.pepela.minimap.parser.Rune
import com.pepela.minimap.parser.Ward
import com.pepela.minimap.sprite.BuildingSprites
import com.pepela.minimap.sprite.BuildingType
import com.pepela.minimap.sprite.HeroSprites
import com.pepela.minimap.sprite.RuneSprites
import com.pepela.minimap.sprite.RuneType
import com.pepela.minimap.sprite.WardSprites
import com.pepela.minimap.sprite.WardType
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.roundToInt


internal class MinimapRenderer(
    private val minimap: BufferedImage,
    private val projection: DotaMapProjection = DotaMapProjection()
) {
    private val radiant = Color(78, 201, 108)
    private val dire = Color(235, 79, 74)
    private val neutral = Color(240, 220, 120)
    private val dead = Color(120, 120, 120, 175)
    private val buildingSprites = BuildingSprites.load(javaClass)
    private val wardSprites = WardSprites.load(javaClass)
    private var heroSprites: HeroSprites? = null
    private val runeSprites = RuneSprites.load(javaClass)

    fun render(state: GameState): BufferedImage {
        val image = BufferedImage(minimap.width, minimap.height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            graphics.drawImage(minimap, 0, 0, null)
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            graphics.font = Font(Font.SANS_SERIF, Font.BOLD, (minimap.width / 46).coerceAtLeast(12))

            if (state.heroes.size >= 10 && heroSprites == null) {
                heroSprites = HeroSprites.load(javaClass, state.heroes.values)
            }

            state.buildings.values.sortedBy { building -> building.name }
                .forEach { building -> drawBuilding(graphics, building) }
            state.heroes.values.sortedBy { hero -> hero.slot }.forEach { hero -> drawHero(graphics, hero) }
            state.wards.values.sortedBy { ward -> ward.name }.forEach { ward -> drawWard(graphics, ward) }
            state.runes.values.sortedBy { rune -> rune.name }.forEach { rune -> drawRune(graphics, rune) }
            drawClock(graphics, state.gameTime)
        } finally {
            graphics.dispose()
        }
        return image
    }

    private fun drawHero(graphics: Graphics2D, hero: Hero) {
        val pixelPoint = projection.toPixel(hero.x, hero.y, minimap.width, minimap.height)
        val heroColor = if (hero.alive) teamColor(hero.team, hero.slot) else dead

        val radius = (minimap.width / 35).coerceAtLeast(8)
        graphics.color = heroColor
        graphics.fillOval(pixelPoint.x - radius, pixelPoint.y - radius, radius * 2, radius * 2)
        graphics.drawOval(pixelPoint.x - radius, pixelPoint.y - radius, radius * 2, radius * 2)
        val sprite = heroSprites?.spriteFor(hero)
        sprite?.let {
            val image = if (hero.alive) it else it.grayscale()
            val size = 50
            val x = pixelPoint.x - size / 2
            val y = pixelPoint.y - size / 2
            val oldComposite = graphics.composite
            graphics.drawImage(image, x, y, size, size, null)
            graphics.composite = oldComposite
        }
    }

    private fun drawBuilding(graphics: Graphics2D, building: Building) {
        val pixelPoint = projection.toPixel(building.x, building.y, minimap.width, minimap.height)
        val type = BuildingType.fromName(building.name)
        val sprite = buildingSprites.spriteFor(building, type)
        val image = if (building.alive) sprite else sprite.grayscale()
        val size = buildingIconSize(type)
        val x = pixelPoint.x - size / 2
        val y = pixelPoint.y - size / 2
        val oldComposite = graphics.composite

        if (!building.alive) {
            graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f)
        }
        graphics.drawImage(image, x, y, size, size, null)
        graphics.composite = oldComposite
    }

    private fun drawWard(graphics: Graphics2D, ward: Ward) {
        if (!ward.alive) {
            return
        }
        val pixelPoint = projection.toPixel(ward.x, ward.y, minimap.width, minimap.height)
        val type = WardType.fromName(ward.name)
        val sprite = wardSprites.spriteFor(ward, type)
        val image = if (ward.alive) sprite else sprite.grayscale()
        val size = (minimap.width / 50).coerceAtLeast(16)
        val x = pixelPoint.x - size / 2
        val y = pixelPoint.y - size / 2
        val oldComposite = graphics.composite

        graphics.drawImage(image, x, y, size, size, null)
        graphics.composite = oldComposite
    }

    private fun drawRune(graphics: Graphics2D, rune: Rune) {
        if (rune.name.contains("unknown") || !rune.isAvailable) return
        val pixelPoint = projection.toPixel(rune.x, rune.y, minimap.width, minimap.height)
        val type = RuneType.fromName(rune.name)
        val sprite = runeSprites.spriteFor(type)
        val size = (minimap.width / 25).coerceAtLeast(16)
        val x = pixelPoint.x - size / 2
        val y = pixelPoint.y - size / 2
        val oldComposite = graphics.composite

        graphics.drawImage(sprite, x, y, size, size, null)
        graphics.composite = oldComposite
    }

    private fun buildingIconSize(type: BuildingType): Int {
        val divisor = when (type) {
            BuildingType.TOWER -> 30
            BuildingType.RAX -> 35
            BuildingType.ANCIENT -> 20
        }
        return (minimap.width / divisor).coerceAtLeast(16)
    }

    private fun drawClock(g: Graphics2D, gameTime: Float) {
        val total = gameTime.roundToInt()
        val sign = if (total < 0) "-" else ""
        val abs = kotlin.math.abs(total)
        val text = "%s%d:%02d".format(sign, abs / 60, abs % 60)
        val pad = 10
        val metrics = g.fontMetrics
        val width = metrics.stringWidth(text) + pad * 2
        val height = metrics.height + pad
        g.color = Color(0, 0, 0, 170)
        g.fillRoundRect(8, 8, width, height, 8, 8)
        g.color = Color.WHITE
        g.drawString(text, 8 + pad, 8 + pad / 2 + metrics.ascent)
    }

    private fun teamColor(team: Int, slot: Int): Color {
        return when (team) {
            2 -> radiant
            3 -> dire
            else -> if (slot < 5) radiant else dire
        }
    }
}
