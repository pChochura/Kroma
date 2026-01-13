package com.pointlessgames.agame

import androidx.compose.ui.window.ComposeUIViewController
import com.pointlessgames.agame.data.appSettingsFileName
import com.pointlessgames.agame.data.initializeAppDatabase
import com.pointlessgames.agame.data.initializeAppSettings
import com.pointlessgames.agame.utils.IosContext
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    initializeAppDatabase(IosContext)
    initializeAppSettings {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        requireNotNull(documentDirectory).path + "/$appSettingsFileName"
    }

    return ComposeUIViewController { App() }
}
