package com.pointlessgames.kroma.dailyChallenge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.kroma.LocalNavigator
import com.pointlessgames.kroma.dailyChallenge.DailyChallengeViewModel
import com.pointlessgames.kroma.levelCreator.ui.LEVEL_FINISHED_RESULT_KEY
import com.pointlessgames.kroma.model.FinishableLevelData
import com.pointlessgames.kroma.ui.CustomIndicationNodeFactory
import com.pointlessgames.kroma.ui.LocalInnerPadding
import com.pointlessgames.kroma.ui.TiltedRoundedCornersShape
import com.pointlessgames.kroma.ui.components.Button
import com.pointlessgames.kroma.ui.components.InlineLoader
import com.pointlessgames.kroma.ui.components.Position
import com.pointlessgames.kroma.ui.components.ShapeButton
import com.pointlessgames.kroma.ui.dragIndication
import com.pointlessgames.kroma.ui.theme.DefaultCornerRadius
import com.pointlessgames.kroma.ui.theme.DefaultIconsSize
import com.pointlessgames.kroma.ui.theme.DefaultSpacing
import com.pointlessgames.kroma.utils.ResultEffect
import com.pointlessgames.kroma.utils.dateFormat
import kotlinx.datetime.format
import kroma.composeapp.generated.resources.Res
import kroma.composeapp.generated.resources.daily_challenge
import kroma.composeapp.generated.resources.easy
import kroma.composeapp.generated.resources.go_back
import kroma.composeapp.generated.resources.hard
import kroma.composeapp.generated.resources.icon_arrow_left
import kroma.composeapp.generated.resources.icon_done
import kroma.composeapp.generated.resources.medium
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DailyChallengeScreen(viewModel: DailyChallengeViewModel) {
    val navigator = LocalNavigator.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        return Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = { InlineLoader() },
        )
    }

    ResultEffect<FinishableLevelData>(resultKey = LEVEL_FINISHED_RESULT_KEY) {
        if (it.isFinished) {
            viewModel.markLevelAsFinished(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(LocalInnerPadding.current),
        verticalArrangement = Arrangement.spacedBy(DefaultSpacing.current.extraLarge),
    ) {
        TopBar(onBackClicked = navigator::navigateBack)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DefaultSpacing.current.small),
        ) {
            uiState.dates.forEach { date ->
                val isSelected = uiState.currentDate == date
                Button(
                    modifier = Modifier.weight(1f),
                    icon = null,
                    text = date.format(dateFormat),
                    defaultShape = if (isSelected) {
                        TiltedRoundedCornersShape(
                            rotation = 0f,
                            size = DefaultCornerRadius.current.full,
                        )
                    } else {
                        TiltedRoundedCornersShape(
                            rotation = 0f,
                            size = DefaultCornerRadius.current.small,
                        )
                    },
                    pressedShape = TiltedRoundedCornersShape(
                        rotation = 0f,
                        size = if (isSelected) {
                            DefaultCornerRadius.current.small
                        } else {
                            DefaultCornerRadius.current.medium
                        },
                    ),
                    defaultBackgroundColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    },
                    pressedBackgroundColor = if (isSelected) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    },
                    onClick = { viewModel.loadDate(date) },
                )
            }
        }

        uiState.sections.forEach { (complexity, levels) ->
            Section(
                title = when (complexity) {
                    0 -> stringResource(Res.string.easy)
                    1 -> stringResource(Res.string.medium)
                    else -> stringResource(Res.string.hard)
                },
                items = levels.map(FinishableLevelData::isFinished),
                onItemClicked = { navigator.navigateToTestLevel(levels[it].levelData) },
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    items: List<Boolean>,
    onItemClicked: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small,
            )
            .padding(DefaultSpacing.current.large),
        verticalArrangement = Arrangement.spacedBy(DefaultSpacing.current.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = DefaultSpacing.current.small,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalArrangement = Arrangement.spacedBy(
                space = DefaultSpacing.current.small,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            items.forEachIndexed { index, finished ->
                Box(
                    modifier = Modifier
                        .size(DefaultIconsSize.current.large)
                        .dragIndication()
                        .clickable(
                            onClick = { onItemClicked(index) },
                            role = Role.Button,
                            indication = CustomIndicationNodeFactory(
                                defaultShape = TiltedRoundedCornersShape(
                                    rotation = 0f,
                                    size = DefaultCornerRadius.current.small,
                                ),
                                pressedShape = TiltedRoundedCornersShape(
                                    rotation = 45f,
                                    size = DefaultCornerRadius.current.small,
                                ),
                                defaultBackgroundColor = if (finished) {
                                    MaterialTheme.colorScheme.inverseSurface
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                pressedBackgroundColor = MaterialTheme.colorScheme.primary,
                            ),
                            interactionSource = remember { MutableInteractionSource() },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (finished) {
                        Icon(
                            painter = painterResource(Res.drawable.icon_done),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    } else {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        ShapeButton(
            size = DefaultIconsSize.current.large,
            iconSize = DefaultIconsSize.current.small,
            icon = Res.drawable.icon_arrow_left,
            contentDescription = Res.string.go_back,
            defaultShape = TiltedRoundedCornersShape(45f, DefaultCornerRadius.current.medium),
            pressedShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.medium),
            defaultBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            pressedBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            tooltipPosition = Position.BELOW,
            onClick = onBackClicked,
        )

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(Res.string.daily_challenge),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}
