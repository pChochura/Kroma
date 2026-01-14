package com.pointlessgames.agame.game.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.agame.LocalNavigator
import com.pointlessgames.agame.game.GameViewModel
import com.pointlessgames.agame.game.GameViewModel.Event.GameFinished
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.ui.components.InlineLoader
import kotlinx.coroutines.launch

@Composable
internal fun TestLevelScreen(
    levelData: LevelData,
    viewModel: GameViewModel,
) {
    val navigator = LocalNavigator.current

    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        coroutineScope.launch {
            viewModel.events.collect {
                when (it) {
                    is GameFinished -> navigator.navigateBackFromTestLevel()
                    is GameViewModel.Event.GoToLevelCreator -> navigator.navigateToLevelCreator()
                }
            }
        }

        onPauseOrDispose { }
    }

    LaunchedEffect(levelData) {
        viewModel.loadLevels(listOf(levelData))
    }

    when (val state = uiState) {
        is GameViewModel.GameUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = { InlineLoader() },
        )

        is GameViewModel.GameUiState.Loaded -> LevelContent(
            showCreateLevelButton = false,
            uiState = state,
            viewModel = viewModel,
        )
    }
}
