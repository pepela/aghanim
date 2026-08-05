package com.pepela.minimap.parser

internal data class Ward(
    val handle: Int,
    val name: String,
    var x: Float = 0f,
    var y: Float = 0f,
    var health: Int = 0,
    var maxHealth: Int = 0,
    var alive: Boolean = true,
    var team: Int = 0,
)
