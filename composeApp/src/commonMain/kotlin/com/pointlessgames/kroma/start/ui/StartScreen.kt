package com.pointlessgames.kroma.start.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.kroma.LocalNavigator
import com.pointlessgames.kroma.model.GridTile
import com.pointlessgames.kroma.start.StartViewModel
import com.pointlessgames.kroma.ui.LocalInnerPadding
import com.pointlessgames.kroma.ui.TiltedRoundedCornersShape
import com.pointlessgames.kroma.ui.components.ShapeButton
import com.pointlessgames.kroma.ui.theme.DefaultCornerRadius
import com.pointlessgames.kroma.ui.theme.DefaultIconsSize
import com.pointlessgames.kroma.ui.theme.DefaultSpacing
import kroma.composeapp.generated.resources.Res
import kroma.composeapp.generated.resources.continue_where_you_left_off
import kroma.composeapp.generated.resources.go_to_daily_challenge
import kroma.composeapp.generated.resources.go_to_level_creator
import kroma.composeapp.generated.resources.icon_calendar
import kroma.composeapp.generated.resources.icon_play
import kroma.composeapp.generated.resources.icon_wrench
import kroma.composeapp.generated.resources.start_game
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StartScreen(
    viewModel: StartViewModel,
) {
    val navigator = LocalNavigator.current
    val spacing = DefaultSpacing.current
    val iconsSizes = DefaultIconsSize.current
    val cornerRadius = DefaultCornerRadius.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(LocalInnerPadding.current),
    ) {
        Logo()

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShapeButton(
                size = iconsSizes.extraLarge,
                iconSize = iconsSizes.medium,
                icon = Res.drawable.icon_play,
                contentDescription = Res.string.start_game,
                defaultShape = TiltedRoundedCornersShape(0f, cornerRadius.medium),
                pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.large),
                defaultBackgroundColor = MaterialTheme.colorScheme.primary,
                pressedBackgroundColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = if (uiState.isTutorialFinished) {
                    navigator::navigateToGame
                } else {
                    navigator::navigateToTutorial
                },
            )

            Text(
                modifier = Modifier.fillMaxWidth(0.5f),
                text = stringResource(Res.string.continue_where_you_left_off),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(spacing.extraLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShapeButton(
                size = iconsSizes.large,
                iconSize = iconsSizes.small,
                icon = Res.drawable.icon_wrench,
                contentDescription = Res.string.go_to_level_creator,
                defaultShape = TiltedRoundedCornersShape(45f, cornerRadius.medium),
                pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.medium),
                defaultBackgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                pressedBackgroundColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                onClick = navigator::navigateToLevelCreator,
            )

            ShapeButton(
                size = iconsSizes.large,
                iconSize = iconsSizes.small,
                icon = Res.drawable.icon_calendar,
                contentDescription = Res.string.go_to_daily_challenge,
                defaultShape = TiltedRoundedCornersShape(-45f, cornerRadius.medium),
                pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.medium),
                defaultBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                pressedBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = navigator::navigateToLevelCreator,
            )
        }
    }
}

@Composable
private fun Logo() {
    val letters = listOf(
        "K" to (GridTile.colorCell1 to GridTile.colorEmpty),
        "R" to (GridTile.colorEmpty to GridTile.colorCell3),
        "O" to (GridTile.colorEmpty to GridTile.colorCell3),
        "M" to (GridTile.colorEmpty to GridTile.colorCell3),
        "A" to (GridTile.colorCell2 to MaterialTheme.colorScheme.background),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DefaultSpacing.current.extraLarge,
                vertical = 128.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(DefaultSpacing.current.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        letters.forEach { (letter, colors) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(
                        color = colors.first,
                        shape = MaterialTheme.shapes.small,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.second,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
