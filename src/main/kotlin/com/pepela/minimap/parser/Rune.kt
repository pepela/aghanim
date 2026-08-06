package com.pepela.minimap.parser

internal data class Rune(
    val handle: Int,
    val name: String,
    var isAvailable: Boolean = true,
    var x: Float = 0f,
    var y: Float = 0f,
)
