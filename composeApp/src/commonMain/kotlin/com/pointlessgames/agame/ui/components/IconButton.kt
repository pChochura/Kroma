package com.pointlessgames.agame.ui.components

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pointlessgames.agame.utils.DefaultSpacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal enum class Position { ABOVE, BELOW }

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun IconButton(
    isEnabled: Boolean,
    iconRes: DrawableResource,
    contentDescription: StringResource,
    onClick: () -> Unit,
    tint: Color = LocalContentColor.current,
    position: Position = Position.ABOVE,
    size: Dp = 32.dp,
) {
    BasicTooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = when (position) {
                Position.ABOVE -> TooltipAnchorPosition.Above
                Position.BELOW -> TooltipAnchorPosition.Below
            },
        ),
        tooltip = {
            Text(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .padding(
                        horizontal = DefaultSpacing.current.medium,
                        vertical = DefaultSpacing.current.small,
                    ),
                text = stringResource(contentDescription),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )
        },
        state = rememberBasicTooltipState(isPersistent = false),
        focusable = false,
    ) {
        IconButton(
            enabled = isEnabled,
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = tint,
                disabledContentColor = tint.copy(alpha = 0.2f),
            ),
        ) {
            Icon(
                modifier = Modifier.size(size),
                painter = painterResource(iconRes),
                contentDescription = null,
            )
        }
    }
}
