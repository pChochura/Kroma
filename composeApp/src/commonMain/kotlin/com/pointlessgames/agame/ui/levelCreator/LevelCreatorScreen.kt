package com.pointlessgames.agame.ui.levelCreator

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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.ui.components.Counter
import com.pointlessgames.agame.ui.components.GameGrid
import com.pointlessgames.agame.ui.components.IconButton
import com.pointlessgames.agame.utils.DefaultSpacing
import game.composeapp.generated.resources.Res
import game.composeapp.generated.resources.ic_play
import game.composeapp.generated.resources.test_the_level
import kotlinx.coroutines.launch

@Composable
internal fun LevelCreatorScreen(
    innerPadding: PaddingValues,
    onLevelCreated: (LevelData) -> Unit,
    viewModel: LevelCreatorViewModel = viewModel { LevelCreatorViewModel() },
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(DefaultSpacing.current.extraLarge),
        contentAlignment = Alignment.Center,
    ) {
        GameGrid(
            width = uiState.width,
            height = uiState.height,
            maxSize = DpSize(
                width = this@BoxWithConstraints.maxWidth,
                height = this@BoxWithConstraints.maxHeight,
            ),
            currentPosition = uiState.startingPosition,
            endingPosition = uiState.endingPosition,
            possibleMoves = emptySet(),
            tiles = uiState.gridTiles,
            onTileClicked = viewModel::onTileClicked,
        )

        Row(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(DefaultSpacing.current.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DefaultSpacing.current.medium),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DefaultSpacing.current.medium),
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
                    horizontalArrangement = Arrangement.spacedBy(DefaultSpacing.current.medium),
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

            IconButton(
                isEnabled = true,
                iconRes = Res.drawable.ic_play,
                contentDescription = Res.string.test_the_level,
                size = 64.dp,
                onClick = viewModel::onStartClicked,
            )
        }
    }
}
