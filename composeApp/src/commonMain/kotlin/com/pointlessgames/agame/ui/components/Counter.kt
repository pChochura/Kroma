package com.pointlessgames.agame.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.pointlessgames.agame.DefaultSpacing
import game.composeapp.generated.resources.Res
import game.composeapp.generated.resources.decrement
import game.composeapp.generated.resources.ic_minus
import game.composeapp.generated.resources.ic_plus
import game.composeapp.generated.resources.increment

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
            iconRes = Res.drawable.ic_minus,
            contentDescription = Res.string.decrement,
            onClick = onDecrement,
        )

        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineLarge,
        )

        IconButton(
            isEnabled = count < maxValue,
            iconRes = Res.drawable.ic_plus,
            contentDescription = Res.string.increment,
            onClick = onIncrement,
        )
    }
}
