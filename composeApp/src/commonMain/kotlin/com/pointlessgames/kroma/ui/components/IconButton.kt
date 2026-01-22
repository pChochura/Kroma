package com.pointlessgames.kroma.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.pointlessgames.kroma.ui.TiltedRoundedCornersShape
import com.pointlessgames.kroma.ui.theme.DefaultCornerRadius
import com.pointlessgames.kroma.ui.theme.DefaultIconsSize
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun IconButton(
    isEnabled: Boolean,
    iconRes: DrawableResource,
    contentDescription: StringResource,
    onClick: () -> Unit,
    tint: Color = LocalContentColor.current,
    position: Position = Position.ABOVE,
) {
    ShapeButton(
        size = DefaultIconsSize.current.large,
        iconSize = DefaultIconsSize.current.medium,
        icon = iconRes,
        contentDescription = contentDescription,
        defaultShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.full),
        pressedShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.full),
        defaultBackgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0f),
        pressedBackgroundColor = MaterialTheme.colorScheme.tertiary,
        contentColor = tint.copy(alpha = if (isEnabled) 1f else 0.2f),
        isEnabled = isEnabled,
        dragForce = 0.03f,
        tooltipPosition = position,
        onClick = onClick,
    )
}
