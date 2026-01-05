package com.pointlessgames.agame.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun IconButton(
    isEnabled: Boolean,
    iconRes: DrawableResource,
    onClick: () -> Unit,
    size: Dp = 32.dp,
) {
    IconButton(
        enabled = isEnabled,
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onBackground,
            disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
        ),
    ) {
        Icon(
            modifier = Modifier.size(size),
            painter = painterResource(iconRes),
            contentDescription = null,
        )
    }
}
