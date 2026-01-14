package com.pointlessgames.agame

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.pointlessgames.agame.ui.LocalInnerPadding
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
        Scaffold(contentWindowInsets = WindowInsets.safeContent) { innerPadding ->
            CompositionLocalProvider(LocalInnerPadding provides innerPadding) {
                Navigator(Route.Game)
            }
        }
    }
}
