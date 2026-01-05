package com.pointlessgames.agame.utils

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density

internal fun CornerSize.toCornerRadius(size: Size, density: Float) =
    CornerRadius(toPx(size, Density(density)))

internal fun CacheDrawScope.filledRoundedRect(
    shape: CornerBasedShape,
) = RoundRect(
    left = 0f,
    top = 0f,
    right = this.size.width,
    bottom = this.size.height,
    topLeftCornerRadius = shape.topStart.toCornerRadius(this.size, density),
    topRightCornerRadius = shape.topEnd.toCornerRadius(this.size, density),
    bottomLeftCornerRadius = shape.bottomStart.toCornerRadius(this.size, density),
    bottomRightCornerRadius = shape.bottomEnd.toCornerRadius(this.size, density),
)
