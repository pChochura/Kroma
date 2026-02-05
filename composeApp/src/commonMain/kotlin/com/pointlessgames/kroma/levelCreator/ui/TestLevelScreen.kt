package com.pointlessgames.kroma.levelCreator.ui

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
import com.pointlessgames.kroma.LocalNavigator
import com.pointlessgames.kroma.game.GameViewModel
import com.pointlessgames.kroma.game.GameViewModel.Event.GameFinished
import com.pointlessgames.kroma.game.GameViewModel.Event.GoBack
import com.pointlessgames.kroma.game.GameViewModel.Event.ShowTutorial
import com.pointlessgames.kroma.game.ui.LevelContent
import com.pointlessgames.kroma.model.FinishableLevelData
import com.pointlessgames.kroma.model.LevelData
import com.pointlessgames.kroma.ui.components.InlineLoader
import com.pointlessgames.kroma.utils.LocalResultEventBus
import kotlinx.coroutines.launch

internal const val LEVEL_FINISHED_RESULT_KEY = "level_finished"

@Composable
internal fun TestLevelScreen(
    levelData: LevelData,
    viewModel: GameViewModel,
) {
    val resultEventBus = LocalResultEventBus.current
    val navigator = LocalNavigator.current

    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        coroutineScope.launch {
            viewModel.events.collect {
                when (it) {
                    is GameFinished -> {
                        resultEventBus.sendResult(
                            resultKey = LEVEL_FINISHED_RESULT_KEY,
                            result = FinishableLevelData(
                                levelData = levelData,
                                isFinished = true,
                            ),
                        )
                        navigator.navigateBackFromTestLevel()
                    }

                    is GoBack -> {
                        resultEventBus.sendResult(
                            resultKey = LEVEL_FINISHED_RESULT_KEY,
                            result = FinishableLevelData(
                                levelData = levelData,
                                isFinished = false,
                            ),
                        )
                        navigator.navigateBack()
                    }

                    is ShowTutorial -> navigator.navigateToTutorial()
                }
            }
        }

        onPauseOrDispose { }
    }

    LaunchedEffect(levelData) {
        viewModel.loadLevels(listOf(levelData), isTestLevel = true)
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
