package com.pointlessgames.agame.game.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import com.pointlessgames.agame.game.GameViewModel
import com.pointlessgames.agame.ui.LocalInnerPadding
import com.pointlessgames.agame.ui.components.GameGrid
import com.pointlessgames.agame.ui.components.IconButton
import com.pointlessgames.agame.ui.components.Position
import com.pointlessgames.agame.utils.DefaultSpacing
import kroma.composeapp.generated.resources.Res
import kroma.composeapp.generated.resources.go_to_level_creator
import kroma.composeapp.generated.resources.go_to_next_level
import kroma.composeapp.generated.resources.go_to_previous_level
import kroma.composeapp.generated.resources.icon_arrow_left
import kroma.composeapp.generated.resources.icon_arrow_right
import kroma.composeapp.generated.resources.icon_delete
import kroma.composeapp.generated.resources.icon_lightbulb
import kroma.composeapp.generated.resources.icon_redo
import kroma.composeapp.generated.resources.icon_restart
import kroma.composeapp.generated.resources.icon_undo
import kroma.composeapp.generated.resources.icon_wrench
import kroma.composeapp.generated.resources.redo_previous_move
import kroma.composeapp.generated.resources.remove_level
import kroma.composeapp.generated.resources.restart_the_level
import kroma.composeapp.generated.resources.show_a_hint
import kroma.composeapp.generated.resources.undo_last_move

@Composable
internal fun LevelContent(
    showCreateLevelButton: Boolean,
    uiState: GameViewModel.UiState.Loaded,
    viewModel: GameViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(LocalInnerPadding.current),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = DefaultSpacing.current.extraLarge,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (showCreateLevelButton) {
                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                    IconButton(
                        isEnabled = true,
                        iconRes = Res.drawable.icon_wrench,
                        contentDescription = Res.string.go_to_level_creator,
                        onClick = viewModel::onLevelCreatorClicked,
                        position = Position.BELOW,
                    )
                }

                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    IconButton(
                        isEnabled = true,
                        iconRes = Res.drawable.icon_delete,
                        contentDescription = Res.string.remove_level,
                        onClick = viewModel::onRemoveLevelClicked,
                        position = Position.BELOW,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = DefaultSpacing.current.medium,
                    alignment = Alignment.CenterHorizontally,
                ),
            ) {
                IconButton(
                    isEnabled = uiState.canMovePreviousLevel,
                    iconRes = Res.drawable.icon_arrow_left,
                    contentDescription = Res.string.go_to_previous_level,
                    onClick = viewModel::onPreviousLevelClicked,
                    position = Position.BELOW,
                )
                Text(
                    text = "${uiState.level + 1}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                IconButton(
                    isEnabled = uiState.canMoveNextLevel,
                    iconRes = Res.drawable.icon_arrow_right,
                    contentDescription = Res.string.go_to_next_level,
                    onClick = viewModel::onNextLevelClicked,
                    position = Position.BELOW,
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
                space = DefaultSpacing.current.medium,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            IconButton(
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
            IconButton(
                isEnabled = uiState.canHint,
                iconRes = Res.drawable.icon_lightbulb,
                contentDescription = Res.string.show_a_hint,
                onClick = viewModel::onHintClicked,
            )
        }
    }
}
