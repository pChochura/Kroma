package com.pointlessgames.agame.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.agame.DefaultSpacing
import com.pointlessgames.agame.ui.components.IconButton
import game.composeapp.generated.resources.Res
import game.composeapp.generated.resources.ic_arrow_left
import game.composeapp.generated.resources.ic_arrow_right
import game.composeapp.generated.resources.ic_redo
import game.composeapp.generated.resources.ic_undo
import kotlinx.coroutines.launch

@Composable
internal fun LevelScreen(
    innerPadding: PaddingValues,
    viewModel: GameViewModel,
    onLevelFinished: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = DefaultSpacing.current.extraLarge,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            GameGrid(
                width = uiState.width,
                height = uiState.height,
                maxSize = DpSize(
                    width = this@BoxWithConstraints.maxWidth,
                    height = this@BoxWithConstraints.maxHeight,
                ),
                currentPosition = uiState.currentPosition,
                endingPosition = uiState.endingPosition,
                possibleMoves = uiState.possibleMoves,
                tiles = uiState.gridTiles,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    space = DefaultSpacing.current.medium,
                    alignment = Alignment.CenterHorizontally,
                ),
            ) {
                IconButton(
                    isEnabled = uiState.canUndo,
                    iconRes = Res.drawable.ic_undo,
                    onClick = viewModel::onUndoClicked,
                )
                IconButton(
                    isEnabled = uiState.canRedo,
                    iconRes = Res.drawable.ic_redo,
                    onClick = viewModel::onRedoClicked,
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = DefaultSpacing.current.medium,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            IconButton(
                isEnabled = uiState.level > 1,
                iconRes = Res.drawable.ic_arrow_left,
                onClick = viewModel::onPreviousLevelClicked,
            )

            Text(
                text = "${uiState.level}",
                style = MaterialTheme.typography.titleLarge,
            )

            IconButton(
                isEnabled = true,
                iconRes = Res.drawable.ic_arrow_right,
                onClick = viewModel::onNextLevelClicked,
            )
        }
    }
}
