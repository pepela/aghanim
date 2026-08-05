package com.pepela.minimap.renderer

import com.pepela.minimap.parser.GameState

internal fun GameState.toJsonLine(frame: Int): String {
    return buildString {
        append("{\"frame\":").append(frame)
        append(",\"gameTime\":").append(gameTime)
        append(",\"heroes\":[")
        heroes.values.sortedBy { hero -> hero.slot }.forEachIndexed { index, hero ->
            if (index > 0) append(',')
            append("{\"slot\":").append(hero.slot)
            append(",\"handle\":").append(hero.handle)
            append(",\"name\":\"").append(hero.name.jsonEscape()).append('"')
            append(",\"x\":").append(hero.x)
            append(",\"y\":").append(hero.y)
            append(",\"alive\":").append(hero.alive)
            append(",\"team\":").append(hero.team)
            append('}')
        }
        append(']')
        append(",\"buildings\":[")
        buildings.values.sortedBy { building -> building.name }.forEachIndexed { index, building ->
            if (index > 0) append(',')
            append("{\"handle\":").append(building.handle)
            append(",\"name\":\"").append(building.name.jsonEscape()).append('"')
            append(",\"x\":").append(building.x)
            append(",\"y\":").append(building.y)
            append(",\"health\":").append(building.health)
            append(",\"maxHealth\":").append(building.maxHealth)
            append(",\"alive\":").append(building.alive)
            append(",\"team\":").append(building.team)
            append('}')
        }
        append("]")
        append(",\"wards\":[")
        wards.values.sortedBy { ward -> ward.name }.forEachIndexed { index, ward ->
            if (index > 0) append(',')
            append("{\"handle\":").append(ward.handle)
            append(",\"name\":\"").append(ward.name.jsonEscape()).append('"')
            append(",\"x\":").append(ward.x)
            append(",\"y\":").append(ward.y)
            append(",\"health\":").append(ward.health)
            append(",\"maxHealth\":").append(ward.maxHealth)
            append(",\"alive\":").append(ward.alive)
            append(",\"team\":").append(ward.team)
            append('}')
        }
        append("]}")
    }
}

private fun String.jsonEscape(): String {
    return buildString(length) {
        for (ch in this@jsonEscape) {
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (ch.code < 32) append("\\u%04x".format(ch.code)) else append(ch)
            }
        }
    }
}
