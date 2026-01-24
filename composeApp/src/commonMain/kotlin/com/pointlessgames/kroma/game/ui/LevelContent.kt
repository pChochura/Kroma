package com.pointlessgames.kroma.game.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import com.pointlessgames.kroma.game.GameViewModel
import com.pointlessgames.kroma.ui.LocalInnerPadding
import com.pointlessgames.kroma.ui.TiltedRoundedCornersShape
import com.pointlessgames.kroma.ui.components.GameGrid
import com.pointlessgames.kroma.ui.components.IconButton
import com.pointlessgames.kroma.ui.components.Position
import com.pointlessgames.kroma.ui.components.ShapeButton
import com.pointlessgames.kroma.ui.components.Tooltip
import com.pointlessgames.kroma.ui.theme.DefaultCornerRadius
import com.pointlessgames.kroma.ui.theme.DefaultIconsSize
import com.pointlessgames.kroma.ui.theme.DefaultSpacing
import kroma.composeapp.generated.resources.Res
import kroma.composeapp.generated.resources.go_back
import kroma.composeapp.generated.resources.go_to_next_level
import kroma.composeapp.generated.resources.go_to_previous_level
import kroma.composeapp.generated.resources.icon_arrow_left
import kroma.composeapp.generated.resources.icon_arrow_right
import kroma.composeapp.generated.resources.icon_lightbulb
import kroma.composeapp.generated.resources.icon_redo
import kroma.composeapp.generated.resources.icon_restart
import kroma.composeapp.generated.resources.icon_undo
import kroma.composeapp.generated.resources.no_hints_available
import kroma.composeapp.generated.resources.redo_previous_move
import kroma.composeapp.generated.resources.restart_the_level
import kroma.composeapp.generated.resources.show_a_hint
import kroma.composeapp.generated.resources.undo_last_move

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LevelContent(
    uiState: GameViewModel.UiState.Loaded,
    viewModel: GameViewModel,
) {
    val spacing = DefaultSpacing.current
    val cornerRadius = DefaultCornerRadius.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(LocalInnerPadding.current),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = spacing.extraLarge,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                ShapeButton(
                    size = DefaultIconsSize.current.large,
                    iconSize = DefaultIconsSize.current.small,
                    icon = Res.drawable.icon_arrow_left,
                    contentDescription = Res.string.go_back,
                    defaultShape = TiltedRoundedCornersShape(45f, cornerRadius.medium),
                    pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.medium),
                    defaultBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    pressedBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    tooltipPosition = Position.BELOW,
                    onClick = viewModel::onBackClicked,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = spacing.medium,
                    alignment = Alignment.CenterHorizontally,
                ),
            ) {
                IconButton(
                    isEnabled = uiState.canMovePreviousLevel,
                    iconRes = Res.drawable.icon_arrow_left,
                    contentDescription = Res.string.go_to_previous_level,
                    onClick = viewModel::onPreviousLevelClicked,
                    position = Position.BELOW,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AnimatedContent(
                    targetState = uiState.level + 1,
                    transitionSpec = {
                        val direction = if (initialState < targetState) 1 else -1

                        fadeIn() + slideInHorizontally { direction * it / 2 } togetherWith
                                fadeOut() + slideOutHorizontally { -direction * it / 2 } using
                                SizeTransform(false)
                    },
                ) {
                    Text(
                        text = "$it",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(
                    isEnabled = uiState.canMoveNextLevel,
                    iconRes = Res.drawable.icon_arrow_right,
                    contentDescription = Res.string.go_to_next_level,
                    onClick = viewModel::onNextLevelClicked,
                    position = Position.BELOW,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            viewModel.onDrag(dragAmount)
                        },
                        onDragEnd = viewModel::onDragEnd,
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                modifier = Modifier.wrapContentSize(),
                targetState = uiState,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { if (uiState.animationMovesForward) it / 2 else -it / 2 } togetherWith
                            fadeOut() + slideOutHorizontally { if (uiState.animationMovesForward) -it / 2 else it / 2 }
                },
                contentKey = { it.level },
                contentAlignment = Alignment.Center,
            ) {
                GameGrid(
                    width = it.levelData.width,
                    height = it.levelData.height,
                    maxSize = DpSize(
                        width = this@BoxWithConstraints.maxWidth,
                        height = this@BoxWithConstraints.maxHeight,
                    ),
                    currentPosition = it.levelData.currentPosition,
                    endingPosition = it.levelData.endingPosition,
                    possibleMoves = it.possibleMoves,
                    tiles = it.levelData.tiles,
                    onAnimationsFinished = viewModel::onAnimationsFinished,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = spacing.medium,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            IconButton(
                isPulsating = uiState.showNoHintsPopup,
                isEnabled = uiState.canUndo,
                iconRes = Res.drawable.icon_undo,
                contentDescription = Res.string.undo_last_move,
                onClick = viewModel::onUndoClicked,
            )
            IconButton(
                isEnabled = uiState.canRedo,
                iconRes = Res.drawable.icon_redo,
                contentDescription = Res.string.redo_previous_move,
                onClick = viewModel::onRedoClicked,
            )
            IconButton(
                isEnabled = uiState.canRestart,
                iconRes = Res.drawable.icon_restart,
                contentDescription = Res.string.restart_the_level,
                onClick = viewModel::onRestartClicked,
            )

            val tooltipState = rememberBasicTooltipState(isPersistent = true)
            LaunchedEffect(uiState.showNoHintsPopup) {
                if (uiState.showNoHintsPopup) {
                    tooltipState.show(MutatePriority.UserInput)
                } else {
                    tooltipState.dismiss()
                }
            }
            LaunchedEffect(tooltipState.isVisible) {
                if (!tooltipState.isVisible) {
                    viewModel.onNoHintsPopupDismissed()
                }
            }

            Tooltip(
                position = Position.ABOVE,
                contentDescription = Res.string.no_hints_available,
                allowUserInput = false,
                state = tooltipState,
            ) {
                IconButton(
                    isLoading = uiState.isLoadingHint,
                    isEnabled = uiState.canHint,
                    iconRes = Res.drawable.icon_lightbulb,
                    contentDescription = Res.string.show_a_hint,
                    onClick = viewModel::onHintClicked,
                )
            }
        }
    }
}
