package com.pointlessgames.agame.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
internal data class GridTile(
    val value: Int,
    val showFromDirection: Direction? = null,
    val animationOffset: Int = 0,
) {
    val color: Color
        get() = when (this) {
            Wall -> Color.Transparent
            Empty -> Color(236, 218, 182)
            else -> colors[value % colors.size]
        }

    companion object {
        val Wall = GridTile(-2)
        val Empty = GridTile(-1)

        const val MIN_VALUE = -2
        const val MAX_VALUE = 3

        private val colors = listOf(
            Color(218, 180, 157),
            Color(192, 133, 82),
            Color(137, 87, 55),
        )
    }
}
