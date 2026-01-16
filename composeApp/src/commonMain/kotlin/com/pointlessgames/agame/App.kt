package com.pointlessgames.agame

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pointlessgames.agame.ui.LocalInnerPadding
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Color(243, 233, 220),
            onBackground = Color(50, 50, 50),
            surface = Color(243, 233, 220),
            onSurface = Color(50, 50, 50),
            onSurfaceVariant = Color(137, 137, 137),
        ),
        shapes = MaterialTheme.shapes.copy(
            small = RoundedCornerShape(8f),
            medium = RoundedCornerShape(16f),
            large = RoundedCornerShape(24f),
        ),
        typography = MaterialTheme.typography.copy(
            headlineLarge = TextStyle(
                fontSize = 32.sp,
                lineHeight = 39.sp,
                fontWeight = FontWeight.Medium,
            ),
            labelMedium = TextStyle(
                fontSize = 16.sp,
                lineHeight = 19.5f.sp,
                fontWeight = FontWeight.Normal,
            ),
        ),
    ) {
        Scaffold(contentWindowInsets = WindowInsets.safeContent) { innerPadding ->
            CompositionLocalProvider(LocalInnerPadding provides innerPadding) {
                Navigator(Route.Start)
            }
        }
    }
}
