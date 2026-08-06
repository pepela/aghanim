package com.pepela.minimap.parser

import skadistats.clarity.event.Insert
import skadistats.clarity.model.Entity
import skadistats.clarity.model.Vector
import skadistats.clarity.processor.entities.Entities
import skadistats.clarity.processor.entities.OnEntityDeleted
import skadistats.clarity.processor.entities.UsesEntities
import skadistats.clarity.processor.reader.OnTickEnd
import skadistats.clarity.processor.runner.SimpleRunner
import skadistats.clarity.source.MappedFileSource
import java.nio.file.Path
import kotlin.math.floor

@UsesEntities
internal class ReplayParser(
    private val onSnapshot: (GameState) -> Unit
) {
    @Insert
    private lateinit var entities: Entities

    private val state = GameState()
    private var previousSecond = Int.MIN_VALUE
    private var fallbackReplaySeconds = 0f

    fun parse(replayPath: Path) {
        MappedFileSource(replayPath.toString()).use { source ->
            SimpleRunner(source).runWith(this)
        }
    }

    @OnTickEnd
    protected fun onTickEnd(synthetic: Boolean) {
        if (synthetic) return

        val gameTime = readGameTime() ?: run {
            fallbackReplaySeconds += 1f / 30f
            fallbackReplaySeconds
        }
        val second = floor(gameTime.toDouble()).toInt()
        if (second == previousSecond) return

        previousSecond = second
        state.gameTime = gameTime
        refreshHeroes()
        refreshBuildings()
        refreshWards()
        refreshRunes()
        onSnapshot(state.copySnapshot())
    }

    @OnEntityDeleted(classPattern = ".*")
    protected fun onEntityDeleted(entity: Entity) {
        if (entity.isBuilding()) {
            val name = entity.buildingName() ?: return
            state.buildings[name]?.alive = false
            state.buildings[name]?.health = 0
        }

        if (entity.isRune()) {
            val name = entity.runeName() ?: return
            state.runes[name]?.isAvailable = false
        }
    }

    private fun refreshHeroes() {
        val selected = selectedHeroEntities()
        if (selected.isNotEmpty()) {
            val activeSlots = selected.map { it.first }.toSet()
            state.heroes.keys.retainAll(activeSlots)
            selected.forEach { (slot, hero) ->
                val pos = hero.position() ?: return@forEach
                state.heroes[slot] = Hero(
                    handle = hero.handle,
                    slot = slot,
                    name = hero.heroName(),
                    x = pos.first,
                    y = pos.second,
                    alive = hero.isAliveUnit(),
                    team = hero.teamNumber(),
                )
            }
            return
        }

        var slot = 0
        state.heroes.clear()
        entityList { it.isHero() }
            .sortedBy { it.handle }
            .forEach { hero ->
                val pos = hero.position() ?: return@forEach
                state.heroes[slot] = Hero(
                    handle = hero.handle,
                    slot = slot,
                    name = hero.heroName(),
                    x = pos.first,
                    y = pos.second,
                    alive = hero.isAliveUnit(),
                    team = hero.teamNumber(),
                )
                slot += 1
            }
    }

    private fun refreshBuildings() {
        entityList { it.isBuilding() }.forEach { building ->
            val name = building.buildingName() ?: return@forEach
            val position = building.position()
            val health = building.firstInt("m_iHealth", "m_iHealth.0000") ?: 0
            val maxHealth = building.firstInt("m_iMaxHealth", "m_iMaxHealth.0000") ?: 0
            val alive = building.isAliveUnit() && (maxHealth <= 0 || health > 0)
            state.buildings[name] = Building(
                handle = building.handle,
                name = name,
                x = position?.first ?: state.buildings[name]?.x ?: 0f,
                y = position?.second ?: state.buildings[name]?.y ?: 0f,
                health = health,
                maxHealth = maxHealth,
                alive = alive,
                team = building.teamNumber(),
            )
        }
    }

    private fun refreshWards() {
        entityList { entity -> entity.isWard() }.forEach { ward ->
            val name = ward.wardName() ?: return@forEach
            val position = ward.position()
            val health = ward.firstInt("m_iHealth", "m_iHealth.0000") ?: 0
            val maxHealth = ward.firstInt("m_iMaxHealth", "m_iMaxHealth.0000") ?: 0
            val alive = ward.isAliveUnit() && (maxHealth <= 0 || health > 0)
            state.wards[name] = Ward(
                handle = ward.handle,
                name = name,
                x = position?.first ?: state.buildings[name]?.x ?: 0f,
                y = position?.second ?: state.buildings[name]?.y ?: 0f,
                health = health,
                maxHealth = maxHealth,
                alive = alive,
                team = ward.teamNumber(),
            )
        }
    }

    private fun refreshRunes() {
        entityList { entity -> entity.isRune() }.forEach { rune ->
            val name = rune.runeName() ?: return@forEach
            val position = rune.position()
            state.runes[name] = Rune(
                handle = rune.handle,
                name = name,
                x = position?.first ?: state.buildings[name]?.x ?: 0f,
                y = position?.second ?: state.buildings[name]?.y ?: 0f,
            )
        }
    }

    private fun selectedHeroEntities(): List<Pair<Int, Entity>> {
        val playerResource = entities.getByDtName("CDOTA_PlayerResource")
            ?: entities.getByDtName("DT_DOTA_PlayerResource")
            ?: return emptyList()

        return (0 until 10).mapNotNull { slot ->
            val idx = slot.toString().padStart(4, '0')
            val handle = playerResource.firstInt(
                "m_vecPlayerTeamData.$idx.m_hSelectedHero",
                "m_hSelectedHero.$idx"
            ) ?: return@mapNotNull null
            val hero = entities.getByHandle(handle) ?: return@mapNotNull null
            slot to hero
        }
    }

    private fun readGameTime(): Float? {
        val rules = entities.getByDtName("CDOTAGamerulesProxy")
            ?: entities.getByDtName("DT_DOTAGamerulesProxy")
            ?: return null
        val gameTime = rules.firstFloat(
            "m_pGameRules.m_fGameTime",
            "m_fGameTime",
            "DT_DOTAGamerules.m_fGameTime"
        ) ?: return null
        val gameStart = rules.firstFloat(
            "m_pGameRules.m_flGameStartTime",
            "m_flGameStartTime",
            "DT_DOTAGamerules.m_flGameStartTime"
        ) ?: 0f
        return if (gameStart != 0f) gameTime - gameStart else gameTime
    }

    private fun entityList(predicate: (Entity) -> Boolean): List<Entity> {
        val result = mutableListOf<Entity>()
        val iterator = entities.getAllByPredicate { entity ->
            entity != null && entity.isExistent && entity.isActive && predicate(entity)
        }
        while (iterator.hasNext()) result += iterator.next()
        return result
    }

    private fun Entity.isHero(): Boolean {
        val dt = dtClass.dtName
        return dt.contains("Hero", ignoreCase = true) && !dt.contains("Illusion", ignoreCase = true)
    }

    private fun Entity.isBuilding(): Boolean {
        val dt = dtClass.dtName
        val name = buildingName().orEmpty()
        return dt.contains("Tower", ignoreCase = true) ||
                dt.contains("Barracks", ignoreCase = true) ||
                dt.contains("Fort", ignoreCase = true) ||
                name.contains("tower", ignoreCase = true) ||
                name.contains("rax", ignoreCase = true) ||
                name.contains("fort", ignoreCase = true)
    }

    private fun Entity.isWard(): Boolean {
        val dt = dtClass.dtName
        return dt.contains("Observer_ward", ignoreCase = true)
    }

    private fun Entity.isRune(): Boolean {
        return dtClass.dtName == "CDOTA_Item_Rune"
    }

    private fun Entity.heroName(): String {
        return firstString("m_iName", "m_iszUnitName", "m_pEntity.m_name")
            ?.takeIf { it.isNotBlank() }
            ?: dtClass.dtName
                .substringAfterLast("CDOTA_Unit_Hero_", dtClass.dtName)
                .substringAfterLast("DT_DOTA_Unit_Hero_", dtClass.dtName)
    }

    private fun Entity.buildingName(): String? {
        val explicit = firstString("m_iName", "m_iszUnitName", "m_pEntity.m_name")
            ?.takeIf { it.isNotBlank() }
        if (explicit != null) return explicit
        if (!dtClass.dtName.contains("Tower", true) &&
            !dtClass.dtName.contains("Barracks", true) &&
            !dtClass.dtName.contains("Fort", true)
        ) return null
        return "${dtClass.dtName}_${handle}"
    }

    private fun Entity.wardName(): String? {
        val explicit = firstString("m_iName", "m_iszUnitName", "m_pEntity.m_name")
            ?.takeIf { it.isNotBlank() }
        if (explicit != null) return explicit
        if (!dtClass.dtName.contains("Observer_ward", true) &&
            !dtClass.dtName.contains("Sentryward", true)
        ) return null
        return "${dtClass.dtName}_${handle}"
    }

    private fun Entity.runeName(): String? {
        val explicit = firstString("m_iName", "m_iszUnitName", "m_pEntity.m_name")
            ?.takeIf { it.isNotBlank() }
        if (explicit != null) return explicit

        if (!dtClass.dtName.contains("Rune", true))
            return null

        val type = firstInt("m_iRuneType") ?: return null

        return when (type) {
            0 -> "Amplify"
            1 -> "Haste"
            2 -> "Illusion"
            3 -> "Invisibility"
            4 -> "Regeneration"
            5 -> "Bounty"
            6 -> "Arcane"
            7 -> "Water"
            9 -> "Shield"
            else -> "Unknown Rune ($type)"
        } + "_$handle"
    }

    private fun Entity.position(): Pair<Float, Float>? {
        val cellX = firstFloat("CBodyComponent.m_cellX", "m_cellX")
        val cellY = firstFloat("CBodyComponent.m_cellY", "m_cellY")
        val vecX = firstFloat("CBodyComponent.m_vecX", "m_vecX")
        val vecY = firstFloat("CBodyComponent.m_vecY", "m_vecY")
        if (cellX != null && cellY != null && vecX != null && vecY != null) {
            return (cellX * 128f + vecX) to (cellY * 128f + vecY)
        }

        val vec = firstVector("m_vecOrigin", "CBodyComponent.m_vecAbsOrigin")
        if (cellX != null && cellY != null && vec != null && vec.dimension >= 2) {
            return (cellX * 128f + vec.getElement(0)) to (cellY * 128f + vec.getElement(1))
        }
        if (vec != null && vec.dimension >= 2) {
            return vec.getElement(0) to vec.getElement(1)
        }
        return null
    }

    private fun Entity.isAliveUnit(): Boolean {
        val lifeState = firstInt("m_lifeState", "m_iLifeState")
        if (lifeState != null) return lifeState == 0
        val health = firstInt("m_iHealth")
        return health == null || health > 0
    }

    private fun Entity.teamNumber(): Int {
        return firstInt("m_iTeamNum", "m_iTeam", "m_nTeamNumber") ?: 0
    }

    private fun Entity.firstString(vararg names: String): String? = firstProperty<String>(*names)

    private fun Entity.firstVector(vararg names: String): Vector? = firstProperty<Vector>(*names)

    private fun Entity.firstInt(vararg names: String): Int? = firstNumber(*names)?.toInt()

    private fun Entity.firstFloat(vararg names: String): Float? = firstNumber(*names)?.toFloat()

    private fun Entity.firstNumber(vararg names: String): Number? {
        for (name in names) {
            val value = firstProperty<Any>(name) ?: continue
            if (value is Number) return value
        }
        return null
    }

    private inline fun <reified T> Entity.firstProperty(vararg names: String): T? {
        for (name in names) {
            if (!hasProperty(name)) continue
            val value = runCatching { getProperty<Any>(name) }.getOrNull() ?: continue
            if (value is T) return value
        }
        return null
    }
}
