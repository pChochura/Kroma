package com.pointlessgames.agame.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import com.pointlessgames.agame.ui.CustomIndicationNodeFactory
import com.pointlessgames.agame.ui.TiltedRoundedCornersShape
import com.pointlessgames.agame.ui.dragIndication
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ShapeButton(
    size: Dp,
    iconSize: Dp,
    icon: DrawableResource,
    contentDescription: StringResource,
    defaultShape: TiltedRoundedCornersShape,
    pressedShape: TiltedRoundedCornersShape,
    defaultBackgroundColor: Color,
    pressedBackgroundColor: Color,
    contentColor: Color,
    isEnabled: Boolean = true,
    dragForce: Float = 0.1f,
    tooltipPosition: Position = Position.ABOVE,
    onClick: () -> Unit,
) {
    Tooltip(
        position = tooltipPosition,
        contentDescription = contentDescription,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .dragIndication(isEnabled, dragForce)
                .clickable(
                    enabled = isEnabled,
                    onClick = onClick,
                    role = Role.Button,
                    indication = CustomIndicationNodeFactory(
                        defaultShape = defaultShape,
                        pressedShape = pressedShape,
                        defaultBackgroundColor = defaultBackgroundColor,
                        pressedBackgroundColor = pressedBackgroundColor,
                    ),
                    interactionSource = remember { MutableInteractionSource() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(icon),
                contentDescription = stringResource(contentDescription),
                tint = contentColor,
            )
        }
    }
}
