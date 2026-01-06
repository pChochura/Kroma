package com.pointlessgames.agame.ui.level

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointlessgames.agame.Route
import com.pointlessgames.agame.ui.components.GameGrid
import com.pointlessgames.agame.ui.components.IconButton
import com.pointlessgames.agame.ui.components.InlineLoader
import com.pointlessgames.agame.ui.components.Position
import com.pointlessgames.agame.utils.DefaultSpacing
import game.composeapp.generated.resources.Res
import game.composeapp.generated.resources.go_to_next_level
import game.composeapp.generated.resources.go_to_previous_level
import game.composeapp.generated.resources.ic_arrow_left
import game.composeapp.generated.resources.ic_arrow_right
import game.composeapp.generated.resources.ic_hint
import game.composeapp.generated.resources.ic_redo
import game.composeapp.generated.resources.ic_restart
import game.composeapp.generated.resources.ic_undo
import game.composeapp.generated.resources.redo_previous_move
import game.composeapp.generated.resources.restart_the_level
import game.composeapp.generated.resources.show_a_hint
import game.composeapp.generated.resources.undo_last_move
import kotlinx.coroutines.launch

@Composable
internal fun LevelScreen(
    innerPadding: PaddingValues,
    level: Route.Level,
    onLevelFinished: () -> Unit,
    viewModel: GameViewModel = viewModel { GameViewModel() },
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(level) {
        viewModel.loadLevel(level.level, level.levelData)
    }

    LifecycleResumeEffect(Unit) {
        coroutineScope.launch {
            viewModel.events.collect {
                when (it) {
                    is GameViewModel.Event.LevelFinished -> onLevelFinished()
                }
            }
        }

        onPauseOrDispose { }
    }

    when (val state = uiState) {
        is GameViewModel.GameUiState.Loaded -> LevelContent(
            uiState = state,
            innerPadding = innerPadding,
            viewModel = viewModel,
        )

        GameViewModel.GameUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = { InlineLoader() },
        )
    }
}

@Composable
private fun LevelContent(
    uiState: GameViewModel.GameUiState.Loaded,
    innerPadding: PaddingValues,
    viewModel: GameViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = DefaultSpacing.current.extraLarge,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = DefaultSpacing.current.medium,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            IconButton(
                isEnabled = false,
                iconRes = Res.drawable.ic_arrow_left,
                contentDescription = Res.string.go_to_previous_level,
                onClick = viewModel::onUndoClicked,
                position = Position.BELOW,
            )
            Text(
                text = "${uiState.level}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            IconButton(
                isEnabled = false,
                iconRes = Res.drawable.ic_arrow_right,
                contentDescription = Res.string.go_to_next_level,
                onClick = viewModel::onRedoClicked,
                position = Position.BELOW,
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            viewModel.onDrag(dragAmount)
                        },
                        onDragEnd = viewModel::onDragEnd,
                    )
                }
                .padding(DefaultSpacing.current.extraLarge),
            contentAlignment = Alignment.Center,
        ) {
            GameGrid(
                width = uiState.levelData.width,
                height = uiState.levelData.height,
                maxSize = DpSize(
                    width = this@BoxWithConstraints.maxWidth,
                    height = this@BoxWithConstraints.maxHeight,
                ),
                currentPosition = uiState.levelData.currentPosition,
                endingPosition = uiState.levelData.endingPosition,
                possibleMoves = uiState.possibleMoves,
                tiles = uiState.levelData.tiles,
                onAnimationsFinished = viewModel::onAnimationsFinished,
            )
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
                iconRes = Res.drawable.ic_undo,
                contentDescription = Res.string.undo_last_move,
                onClick = viewModel::onUndoClicked,
            )
            IconButton(
                isEnabled = uiState.canRedo,
                iconRes = Res.drawable.ic_redo,
                contentDescription = Res.string.redo_previous_move,
                onClick = viewModel::onRedoClicked,
            )
            IconButton(
                isEnabled = uiState.canRestart,
                iconRes = Res.drawable.ic_restart,
                contentDescription = Res.string.restart_the_level,
                onClick = viewModel::onRestartClicked,
            )
            IconButton(
                isEnabled = uiState.canHint,
                iconRes = Res.drawable.ic_hint,
                contentDescription = Res.string.show_a_hint,
                onClick = viewModel::onHintClicked,
            )
        }
    }
}
