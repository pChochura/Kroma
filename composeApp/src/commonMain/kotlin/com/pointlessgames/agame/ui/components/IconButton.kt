package com.pointlessgames.agame.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    Tooltip(
        position = position,
        contentDescription = contentDescription,
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
                contentDescription = stringResource(contentDescription),
            )
        }
    }
}
