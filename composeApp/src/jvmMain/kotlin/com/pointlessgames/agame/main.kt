package com.pointlessgames.agame

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pointlessgames.agame.di.initKoin

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kroma",
        content = { App() },
    )
}
