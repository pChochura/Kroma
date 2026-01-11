package com.pointlessgames.agame.ui.level

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointlessgames.agame.data.LevelRepository
import com.pointlessgames.agame.ui.components.InlineLoader
import com.pointlessgames.agame.ui.level.ui.LevelContent
import kotlinx.coroutines.launch

@Composable
internal fun LevelScreen(
    innerPadding: PaddingValues,
    onFinished: () -> Unit,
    viewModel: GameViewModel = viewModel { GameViewModel() },
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        coroutineScope.launch {
            viewModel.events.collect {
                when (it) {
                    is GameViewModel.Event.Finished -> onFinished()
                }
            }
        }

        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        val levels = LevelRepository().getLevels()
        if (levels.isEmpty()) {
            onFinished()
        } else {
            viewModel.loadLevels(levels)
        }
    }

    when (val state = uiState) {
        is GameViewModel.GameUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = { InlineLoader() },
        )

        is GameViewModel.GameUiState.Loaded -> LevelContent(
            showCreateLevelButton = true,
            uiState = state,
            innerPadding = innerPadding,
            viewModel = viewModel,
        )
    }
}
