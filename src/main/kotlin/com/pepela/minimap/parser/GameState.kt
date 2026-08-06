package com.pepela.minimap.parser

internal class GameState(
    val heroes: MutableMap<Int, Hero> = linkedMapOf(),
    val buildings: MutableMap<String, Building> = linkedMapOf(),
    val wards: MutableMap<String, Ward> = linkedMapOf(),
    val runes: MutableMap<String, Rune> = linkedMapOf(),
    var gameTime: Float = -90f,
) {
    fun copySnapshot(): GameState {
        val copy = GameState(gameTime = gameTime)
        heroes.forEach { (slot, hero) -> copy.heroes[slot] = hero.copy() }
        buildings.forEach { (name, building) -> copy.buildings[name] = building.copy() }
        wards.forEach { (name, ward) -> copy.wards[name] = ward.copy() }
        runes.forEach { (name, rune) -> copy.runes[name] = rune.copy() }
        return copy
    }
}
