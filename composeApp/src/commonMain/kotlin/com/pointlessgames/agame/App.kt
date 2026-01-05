package com.pointlessgames.agame

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointlessgames.agame.ui.GameViewModel
import com.pointlessgames.agame.ui.LevelCreatorScreen
import com.pointlessgames.agame.ui.LevelScreen
import com.pointlessgames.agame.ui.LevelViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Color(243, 233, 220),
            onBackground = Color(77, 73, 69),
        ),
        shapes = MaterialTheme.shapes.copy(
            small = RoundedCornerShape(8f),
            medium = RoundedCornerShape(16f),
            large = RoundedCornerShape(24f),
        ),
    ) {
        val levelViewModel = viewModel { LevelViewModel() }
        val gameViewModel = viewModel { GameViewModel() }

        var isLevelCreatorOpen by remember { mutableStateOf(true) }

        Scaffold { innerPadding ->
            if (isLevelCreatorOpen) {
                LevelCreatorScreen(
                    innerPadding = innerPadding,
                    viewModel = levelViewModel,
                    onLevelCreated = {
                        gameViewModel.loadLevel(it)
                        isLevelCreatorOpen = false
                    },
                )
            } else {
                LevelScreen(
                    innerPadding = innerPadding,
                    viewModel = gameViewModel,
                    onLevelFinished = {
                        levelViewModel.reset()
                        isLevelCreatorOpen = true
                    }
                )
            }
        }
    }
}
