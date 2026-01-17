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
import com.pointlessgames.agame.game.GameViewModel.Event.GoBack
import com.pointlessgames.agame.ui.components.InlineLoader
import kotlinx.coroutines.launch

@Composable
internal fun GameScreen(viewModel: GameViewModel) {
    val navigator = LocalNavigator.current

    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        coroutineScope.launch {
            viewModel.events.collect {
                when (it) {
                    is GoBack -> navigator.navigateBack()
                    is GameFinished -> navigator.navigateToFinishedGame()
                }
            }
        }

        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.loadStoredLevels()
    }

    when (val state = uiState) {
        is GameViewModel.UiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = { InlineLoader() },
        )

        is GameViewModel.UiState.Loaded -> LevelContent(
            uiState = state,
            viewModel = viewModel,
        )
    }
}
