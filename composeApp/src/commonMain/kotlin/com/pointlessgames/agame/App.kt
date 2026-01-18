package com.pointlessgames.agame

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pointlessgames.agame.ui.LocalInnerPadding
import com.pointlessgames.agame.ui.theme.DefaultColors
import com.pointlessgames.agame.ui.theme.DefaultCornerRadius
import kroma.composeapp.generated.resources.Poppins_Bold
import kroma.composeapp.generated.resources.Poppins_Regular
import kroma.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val fontFamily = FontFamily(
        Font(Res.font.Poppins_Bold, FontWeight.Bold),
        Font(Res.font.Poppins_Regular, FontWeight.Normal),
    )

    val colors = DefaultColors.current
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = colors.ochre,
            onPrimary = colors.beige,
            primaryContainer = colors.rose,
            onPrimaryContainer = colors.brown,

            secondary = colors.rose,
            onSecondary = colors.brown,

            tertiary = colors.tan,
            onTertiary = colors.brown,

            background = colors.beige,
            onBackground = colors.brown,

            surface = colors.beige,
            onSurface = colors.brown,

            surfaceVariant = colors.tan,
            surfaceContainerLow = colors.softCream,
            surfaceContainer = colors.tan,
            surfaceContainerHigh = colors.honeyOak,
            surfaceContainerHighest = colors.rose,

            onSurfaceVariant = colors.brown,

            inverseSurface = colors.brown,
            inverseOnSurface = colors.beige,
            inversePrimary = colors.rose,

            outline = colors.ochre,
        ),
        shapes = MaterialTheme.shapes.copy(
            small = RoundedCornerShape(DefaultCornerRadius.current.small),
            medium = RoundedCornerShape(DefaultCornerRadius.current.medium),
            large = RoundedCornerShape(DefaultCornerRadius.current.large),
        ),
        typography = MaterialTheme.typography.copy(
            headlineLarge = TextStyle(
                fontSize = 32.sp,
                lineHeight = 39.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = fontFamily,
            ),
            labelMedium = TextStyle(
                fontSize = 16.sp,
                lineHeight = 19.5f.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = fontFamily,
                letterSpacing = 2.sp,
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
