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
            Empty -> colorEmpty
            else -> colors[value % colors.size]
        }

    companion object {
        val Wall = GridTile(-2)
        val Empty = GridTile(-1)

        const val MIN_VALUE = -2
        const val MAX_VALUE = 3

        val colorEmpty = Color(236, 218, 182)
        val colorCell1 = Color(218, 180, 157)
        val colorCell2 = Color(192, 133, 82)
        val colorCell3 = Color(137, 87, 55)

        private val colors = listOf(colorCell1, colorCell2, colorCell3)
    }
}
