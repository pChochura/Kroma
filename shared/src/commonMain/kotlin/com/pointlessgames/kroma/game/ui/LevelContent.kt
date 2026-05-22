package com.pointlessgames.kroma.game.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import com.pointlessgames.kroma.game.GameViewModel
import com.pointlessgames.kroma.model.Direction
import com.pointlessgames.kroma.model.LevelData
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
import com.pointlessgames.kroma.utils.readableDuration
import kroma.shared.generated.resources.Res
import kroma.shared.generated.resources.go_back
import kroma.shared.generated.resources.go_to_next_level
import kroma.shared.generated.resources.go_to_previous_level
import kroma.shared.generated.resources.icon_arrow_left
import kroma.shared.generated.resources.icon_arrow_right
import kroma.shared.generated.resources.icon_lightbulb
import kroma.shared.generated.resources.icon_question_mark
import kroma.shared.generated.resources.icon_redo
import kroma.shared.generated.resources.icon_restart
import kroma.shared.generated.resources.icon_undo
import kroma.shared.generated.resources.no_hints_available
import kroma.shared.generated.resources.redo_previous_move
import kroma.shared.generated.resources.restart_the_level
import kroma.shared.generated.resources.show_a_hint
import kroma.shared.generated.resources.show_tutorial
import kroma.shared.generated.resources.undo_last_move

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LevelContent(
    uiState: GameViewModel.UiState.Loaded,
    viewModel: GameViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(LocalInnerPadding.current)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        viewModel.onDrag(dragAmount)
                    },
                    onDragEnd = viewModel::onDragEnd,
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = DefaultSpacing.current.extraLarge,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        TopBar(
            level = uiState.level,
            canMovePreviousLevel = uiState.canMovePreviousLevel,
            canMoveNextLevel = uiState.canMoveNextLevel,
            onPreviousLevelClicked = viewModel::onPreviousLevelClicked,
            onNextLevelClicked = viewModel::onNextLevelClicked,
            onBackClicked = viewModel::onBackClicked,
            onTutorialClicked = viewModel::onTutorialClicked,
        )

        GameBoard(
            animationMovesForward = uiState.animationMovesForward,
            levelData = uiState.levelData,
            possibleMoves = uiState.possibleMoves,
            onAnimationsFinished = viewModel::onAnimationsFinished,
        )

        BottomBar(
            canUndo = uiState.canUndo,
            canRedo = uiState.canRedo,
            canRestart = uiState.canRestart,
            canHint = uiState.canHint,
            hintCooldown = uiState.hintCooldown,
            isLoadingHint = uiState.isLoadingHint,
            showNoHintsPopup = uiState.showNoHintsPopup,
            onUndoClicked = viewModel::onUndoClicked,
            onRedoClicked = viewModel::onRedoClicked,
            onRestartClicked = viewModel::onRestartClicked,
            onHintClicked = viewModel::onHintClicked,
            onNoHintsPopupDismissed = viewModel::onNoHintsPopupDismissed,
        )
    }
}

@Composable
internal fun TopBar(
    level: Int,
    canMovePreviousLevel: Boolean,
    canMoveNextLevel: Boolean,
    onPreviousLevelClicked: () -> Unit,
    onNextLevelClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onTutorialClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
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

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = DefaultSpacing.current.medium,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            IconButton(
                isEnabled = canMovePreviousLevel,
                iconRes = Res.drawable.icon_arrow_left,
                contentDescription = Res.string.go_to_previous_level,
                onClick = onPreviousLevelClicked,
                position = Position.BELOW,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AnimatedContent(
                targetState = level + 1,
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
                isEnabled = canMoveNextLevel,
                iconRes = Res.drawable.icon_arrow_right,
                contentDescription = Res.string.go_to_next_level,
                onClick = onNextLevelClicked,
                position = Position.BELOW,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ShapeButton(
            size = DefaultIconsSize.current.large,
            iconSize = DefaultIconsSize.current.small,
            icon = Res.drawable.icon_question_mark,
            contentDescription = Res.string.show_tutorial,
            defaultShape = TiltedRoundedCornersShape(-45f, DefaultCornerRadius.current.medium),
            pressedShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.medium),
            defaultBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            pressedBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            tooltipPosition = Position.BELOW,
            onClick = onTutorialClicked,
        )
    }
}

@Composable
internal fun ColumnScope.GameBoard(
    animationMovesForward: Boolean,
    levelData: LevelData,
    possibleMoves: Set<Direction>,
    onAnimationsFinished: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            modifier = Modifier.wrapContentSize(),
            targetState = levelData,
            transitionSpec = {
                fadeIn() + slideInHorizontally { if (animationMovesForward) it / 2 else -it / 2 } togetherWith
                        fadeOut() + slideOutHorizontally { if (animationMovesForward) -it / 2 else it / 2 }
            },
            contentKey = { it.id },
            contentAlignment = Alignment.Center,
        ) {
            GameGrid(
                width = it.width,
                height = it.height,
                maxSize = DpSize(
                    width = this@BoxWithConstraints.maxWidth,
                    height = this@BoxWithConstraints.maxHeight,
                ),
                currentPosition = it.currentPosition,
                endingPosition = it.endingPosition,
                possibleMoves = possibleMoves,
                tiles = it.tiles,
                onAnimationsFinished = onAnimationsFinished,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BottomBar(
    canUndo: Boolean,
    canRedo: Boolean,
    canRestart: Boolean,
    canHint: Boolean,
    hintCooldown: Long,
    isLoadingHint: Boolean,
    showNoHintsPopup: Boolean,
    onUndoClicked: () -> Unit,
    onRedoClicked: () -> Unit,
    onRestartClicked: () -> Unit,
    onHintClicked: () -> Unit,
    onNoHintsPopupDismissed: () -> Unit,
) {
    val spacing = DefaultSpacing.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = spacing.medium,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        IconButton(
            isPulsating = showNoHintsPopup,
            isEnabled = canUndo,
            iconRes = Res.drawable.icon_undo,
            contentDescription = Res.string.undo_last_move,
            onClick = onUndoClicked,
        )
        IconButton(
            isEnabled = canRedo,
            iconRes = Res.drawable.icon_redo,
            contentDescription = Res.string.redo_previous_move,
            onClick = onRedoClicked,
        )
        IconButton(
            isEnabled = canRestart,
            iconRes = Res.drawable.icon_restart,
            contentDescription = Res.string.restart_the_level,
            onClick = onRestartClicked,
        )

        val tooltipState = rememberBasicTooltipState(isPersistent = true)
        LaunchedEffect(showNoHintsPopup) {
            if (showNoHintsPopup) {
                tooltipState.show(MutatePriority.UserInput)
            } else {
                tooltipState.dismiss()
            }
        }
        LaunchedEffect(tooltipState.isVisible) {
            if (!tooltipState.isVisible) {
                onNoHintsPopupDismissed()
            }
        }

        Tooltip(
            position = Position.ABOVE,
            contentDescription = Res.string.no_hints_available,
            allowUserInput = false,
            state = tooltipState,
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                IconButton(
                    isLoading = isLoadingHint,
                    isEnabled = canHint,
                    iconRes = Res.drawable.icon_lightbulb,
                    contentDescription = Res.string.show_a_hint,
                    onClick = onHintClicked,
                )

                AnimatedContent(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small,
                        ),
                    targetState = hintCooldown,
                    contentAlignment = Alignment.Center,
                    transitionSpec = {
                        fadeIn() + slideInVertically { -it / 2 } togetherWith
                                fadeOut() + slideOutVertically { it / 2 }
                    },
                ) { cooldown ->
                    if (cooldown > 0L) {
                        Text(
                            modifier = Modifier
                                .padding(spacing.extraSmall),
                            text = cooldown.readableDuration(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
