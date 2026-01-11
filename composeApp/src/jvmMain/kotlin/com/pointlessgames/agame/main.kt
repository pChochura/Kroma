package com.pointlessgames.agame

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pointlessgames.agame.data.initializeRoomDatabase
import com.pointlessgames.agame.utils.JvmContext

fun main() {
    initializeRoomDatabase(JvmContext)

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "A Game",
            content = { App() },
        )
    }
}
