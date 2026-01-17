package com.pointlessgames.agame.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.pointlessgames.agame.ui.theme.DefaultSpacing
import kroma.composeapp.generated.resources.Res
import kroma.composeapp.generated.resources.decrement
import kroma.composeapp.generated.resources.icon_add
import kroma.composeapp.generated.resources.icon_remove
import kroma.composeapp.generated.resources.increment

@Composable
internal fun Counter(
    count: Int,
    minValue: Int,
    maxValue: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DefaultSpacing.current.medium),
    ) {
        IconButton(
            isEnabled = count > minValue,
            iconRes = Res.drawable.icon_remove,
            contentDescription = Res.string.decrement,
            onClick = onDecrement,
        )

        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineLarge,
        )

        IconButton(
            isEnabled = count < maxValue,
            iconRes = Res.drawable.icon_add,
            contentDescription = Res.string.increment,
            onClick = onIncrement,
        )
    }
}
