package com.pointlessgames.agame

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.rememberNavBackStack
import com.pointlessgames.agame.ui.level.LevelScreen
import com.pointlessgames.agame.ui.levelCreator.LevelCreatorScreen
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
        Scaffold(
            contentWindowInsets = WindowInsets.safeContent,
        ) { innerPadding ->
            val backStack = rememberNavBackStack(
                configuration = navigationConfig,
                Route.Level,
            )

            Navigator(backStack = backStack) {
                entry<Route.LevelCreator> {
                    LevelCreatorScreen(
                        innerPadding = innerPadding,
                        onLevelCreated = {  },
                    )
                }
                entry<Route.Level> {
                    LevelScreen(innerPadding = innerPadding)
                }
            }
        }
    }
}
