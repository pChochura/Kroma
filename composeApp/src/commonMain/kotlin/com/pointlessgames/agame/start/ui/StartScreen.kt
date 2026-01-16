package com.pointlessgames.agame.start.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pointlessgames.agame.LocalNavigator
import com.pointlessgames.agame.model.GridTile
import com.pointlessgames.agame.ui.LocalInnerPadding
import com.pointlessgames.agame.ui.components.Position
import com.pointlessgames.agame.ui.components.Tooltip
import com.pointlessgames.agame.utils.DefaultSpacing
import kroma.composeapp.generated.resources.Res
import kroma.composeapp.generated.resources.continue_where_you_left_off
import kroma.composeapp.generated.resources.go_to_daily_challenge
import kroma.composeapp.generated.resources.go_to_level_creator
import kroma.composeapp.generated.resources.icon_calendar
import kroma.composeapp.generated.resources.icon_play
import kroma.composeapp.generated.resources.icon_wrench
import kroma.composeapp.generated.resources.start_game
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StartScreen() {
    val navigator = LocalNavigator.current
    val spacing = DefaultSpacing.current

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
            Tooltip(
                position = Position.ABOVE,
                contentDescription = Res.string.start_game,
            ) {
                IconButton(
                    modifier = Modifier
                        .width(128.dp)
                        .height(64.dp),
                    onClick = { navigator.navigateToGame() },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = GridTile.colorEmpty,
                        containerColor = GridTile.colorCell2,
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(Res.drawable.icon_play),
                        contentDescription = stringResource(Res.string.start_game),
                    )
                }
            }

            Text(
                text = stringResource(Res.string.continue_where_you_left_off),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .drawWithCache {
                        val cornerRadius = CornerRadius(16f, 16f)
                        onDrawBehind {
                            withTransform(transformBlock = { rotate(45f) }) {
                                drawRoundRect(
                                    color = GridTile.colorCell2.copy(alpha = 0.5f),
                                    cornerRadius = cornerRadius,
                                )
                            }
                        }
                    }
                    .clipToBounds()
                    .clickable(
                        onClick = { navigator.navigateToLevelCreator() },
                        role = Role.Button,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.drawable.icon_wrench),
                    contentDescription = stringResource(Res.string.go_to_level_creator),
                    tint = GridTile.colorCell3,
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .drawWithCache {
                        val cornerRadius = CornerRadius(16f, 16f)
                        onDrawBehind {
                            withTransform(transformBlock = { rotate(45f) }) {
                                drawRoundRect(
                                    color = GridTile.colorEmpty.copy(alpha = 0.5f),
                                    cornerRadius = cornerRadius,
                                )
                            }
                        }
                    }
                    .clipToBounds()
                    .clickable(
                        onClick = { },
                        role = Role.Button,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.drawable.icon_calendar),
                    contentDescription = stringResource(Res.string.go_to_daily_challenge),
                    tint = GridTile.colorCell3,
                )
            }
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
                        shape = MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.second,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
