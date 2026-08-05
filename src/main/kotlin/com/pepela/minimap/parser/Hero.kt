package com.pepela.minimap.parser

internal data class Hero(
    val handle: Int,
    val slot: Int,
    var name: String = "",
    var x: Float = 0f,
    var y: Float = 0f,
    var alive: Boolean = true,
    var team: Int = 0,
)
