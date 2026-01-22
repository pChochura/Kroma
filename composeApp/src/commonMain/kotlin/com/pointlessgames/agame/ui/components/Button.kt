package com.pointlessgames.agame.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.pointlessgames.agame.ui.CustomIndicationNodeFactory
import com.pointlessgames.agame.ui.TiltedRoundedCornersShape
import com.pointlessgames.agame.ui.dragIndication
import com.pointlessgames.agame.ui.theme.DefaultIconsSize
import com.pointlessgames.agame.ui.theme.DefaultSpacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun Button(
    icon: DrawableResource?,
    text: String,
    defaultShape: TiltedRoundedCornersShape,
    pressedShape: TiltedRoundedCornersShape,
    defaultBackgroundColor: Color,
    pressedBackgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    val spacing = DefaultSpacing.current

    Row(
        modifier = Modifier
            .dragIndication()
            .clickable(
                onClick = onClick,
                role = Role.Button,
                indication = CustomIndicationNodeFactory(
                    defaultShape = defaultShape,
                    pressedShape = pressedShape,
                    defaultBackgroundColor = defaultBackgroundColor,
                    pressedBackgroundColor = pressedBackgroundColor,
                ),
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(
                horizontal = spacing.medium,
                vertical = spacing.small,
            ),
        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                modifier = Modifier.size(DefaultIconsSize.current.small),
                painter = painterResource(icon),
                contentDescription = null,
                tint = contentColor,
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}
