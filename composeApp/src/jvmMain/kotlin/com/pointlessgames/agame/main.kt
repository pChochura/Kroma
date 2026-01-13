package com.pointlessgames.agame

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pointlessgames.agame.data.appSettingsFileName
import com.pointlessgames.agame.data.initializeAppDatabase
import com.pointlessgames.agame.data.initializeAppSettings
import com.pointlessgames.agame.utils.JvmContext
import java.io.File

fun main() {
    initializeAppDatabase(JvmContext)
    initializeAppSettings {
        File(System.getProperty("java.io.tmpdir"), appSettingsFileName).absolutePath
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "A Game",
            content = { App() },
        )
    }
}
