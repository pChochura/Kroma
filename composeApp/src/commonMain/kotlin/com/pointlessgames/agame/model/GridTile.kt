package com.pointlessgames.agame.model

import androidx.compose.ui.graphics.Color

internal data class GridTile(
    val value: Int,
    val showFromDirection: Direction? = null,
    val animationOffset: Int = 0,
) {
    val color: Color
        get() = listOf(
            Color(218, 180, 157),
            Color(192, 133, 82),
            Color(137, 87, 55),
        ).getOrElse(value) { Color(236, 218, 182) }

    companion object {
        val Empty = GridTile(-1)
    }
}
