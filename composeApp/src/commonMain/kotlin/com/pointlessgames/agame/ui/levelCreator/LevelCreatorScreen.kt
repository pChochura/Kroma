package com.pointlessgames.agame.ui.levelCreator

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.PopupPositionProvider
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.ui.components.Counter
import com.pointlessgames.agame.ui.components.GameGrid
import com.pointlessgames.agame.ui.components.IconButton
import com.pointlessgames.agame.ui.components.Position
import com.pointlessgames.agame.utils.DefaultSpacing
import game.composeapp.generated.resources.Res
import game.composeapp.generated.resources.end_of_the_level
import game.composeapp.generated.resources.generate_the_level
import game.composeapp.generated.resources.ic_dot
import game.composeapp.generated.resources.ic_generate
import game.composeapp.generated.resources.ic_play
import game.composeapp.generated.resources.ic_restart
import game.composeapp.generated.resources.ic_save
import game.composeapp.generated.resources.restart_the_level
import game.composeapp.generated.resources.save_the_level
import game.composeapp.generated.resources.start_of_the_level
import game.composeapp.generated.resources.test_the_level
import kotlinx.coroutines.launch
import com.pointlessgames.agame.model.Position as GridTilePosition

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LevelCreatorScreen(
    innerPadding: PaddingValues,
    onLevelCreated: (LevelData) -> Unit,
    viewModel: LevelCreatorViewModel = viewModel { LevelCreatorViewModel() },
) {
    val density = LocalDensity.current
    val spacing = DefaultSpacing.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var currentTooltipPosition by remember { mutableStateOf<GridTilePosition?>(null) }
    val tooltipState = rememberBasicTooltipState(isPersistent = true)

    LifecycleResumeEffect(Unit) {
        coroutineScope.launch {
            viewModel.events.collect {
                when (it) {
                    is LevelCreatorViewModel.Event.LevelCreated -> onLevelCreated(it.levelData)
                }
            }
        }

        onPauseOrDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                isEnabled = true,
                iconRes = Res.drawable.ic_restart,
                contentDescription = Res.string.restart_the_level,
                onClick = viewModel::onRestartClicked,
                position = Position.BELOW,
            )
            IconButton(
                isEnabled = true,
                iconRes = Res.drawable.ic_generate,
                contentDescription = Res.string.generate_the_level,
                onClick = viewModel::onGenerateLevelClicked,
                position = Position.BELOW,
            )
            IconButton(
                isEnabled = true,
                iconRes = Res.drawable.ic_save,
                contentDescription = Res.string.save_the_level,
                onClick = viewModel::onSaveLevelClicked,
                position = Position.BELOW,
            )
            IconButton(
                isEnabled = true,
                iconRes = Res.drawable.ic_play,
                contentDescription = Res.string.test_the_level,
                onClick = viewModel::onStartClicked,
                position = Position.BELOW,
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .padding(spacing.extraLarge),
            contentAlignment = Alignment.Center,
        ) {
            val maxSize = remember {
                DpSize(
                    width = this@BoxWithConstraints.maxWidth,
                    height = this@BoxWithConstraints.maxHeight,
                )
            }

            BasicTooltipBox(
                positionProvider = remember {
                    object : PopupPositionProvider {
                        override fun calculatePosition(
                            anchorBounds: IntRect,
                            windowSize: IntSize,
                            layoutDirection: LayoutDirection,
                            popupContentSize: IntSize,
                        ): IntOffset {
                            val position = currentTooltipPosition ?: return IntOffset.Zero

                            val tileGap = spacing.extraSmall
                            val tileSizeByWidth =
                                (maxSize.width - (uiState.width - 1) * tileGap) / uiState.width
                            val tileSizeByHeight =
                                (maxSize.height - (uiState.height - 1) * tileGap) / uiState.height
                            val tileSize = minOf(tileSizeByWidth, tileSizeByHeight)

                            val x = with(density) { (position.x * (tileSize + tileGap)).toPx() }
                            val y = with(density) { (position.y * (tileSize + tileGap)).toPx() }

                            val offsetX =
                                (popupContentSize.width - with(density) { tileSize.toPx() }) / 2

                            return IntOffset(
                                x = (anchorBounds.left + x - offsetX).toInt(),
                                y = (anchorBounds.top + y - popupContentSize.height).toInt(),
                            )
                        }
                    }
                },
                tooltip = {
                    Row(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.inverseSurface,
                                shape = MaterialTheme.shapes.medium,
                            )
                            .padding(spacing.extraSmall),
                        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            isEnabled = true,
                            iconRes = Res.drawable.ic_dot,
                            contentDescription = Res.string.start_of_the_level,
                            tint = Color(236, 218, 182),
                            onClick = {
                                viewModel.onStartChanged(currentTooltipPosition)
                                tooltipState.dismiss()
                            },
                        )
                        IconButton(
                            isEnabled = true,
                            iconRes = Res.drawable.ic_dot,
                            contentDescription = Res.string.end_of_the_level,
                            tint = MaterialTheme.colorScheme.background,
                            onClick = {
                                viewModel.onEndChanged(currentTooltipPosition)
                                tooltipState.dismiss()
                            },
                        )
                    }
                },
                state = tooltipState,
                focusable = true,
                enableUserInput = false,
            ) {
                GameGrid(
                    width = uiState.width,
                    height = uiState.height,
                    maxSize = maxSize,
                    currentPosition = uiState.startingPosition,
                    endingPosition = uiState.endingPosition,
                    possibleMoves = emptySet(),
                    tiles = uiState.gridTiles,
                    onTileClicked = viewModel::onTileClicked,
                    onTileLongClicked = {
                        currentTooltipPosition = it
                        coroutineScope.launch { tooltipState.show() }
                    },
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                Text(
                    text = "Width: ",
                    style = MaterialTheme.typography.titleLarge,
                )
                Counter(
                    count = uiState.width,
                    minValue = 1,
                    maxValue = 12,
                    onIncrement = viewModel::onWidthIncrement,
                    onDecrement = viewModel::onWidthDecrement,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                Text(
                    text = "Height: ",
                    style = MaterialTheme.typography.titleLarge,
                )
                Counter(
                    count = uiState.height,
                    minValue = 1,
                    maxValue = 12,
                    onIncrement = viewModel::onHeightIncrement,
                    onDecrement = viewModel::onHeightDecrement,
                )
            }
        }
    }
}
