package com.pointlessgames.kroma

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pointlessgames.kroma.di.initKoin

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kroma",
        content = { App() },
    )
}
