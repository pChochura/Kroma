package com.pointlessgames.agame

import androidx.compose.ui.window.ComposeUIViewController
import com.pointlessgames.agame.data.initializeRoomDatabase
import com.pointlessgames.agame.utils.IosContext
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initializeRoomDatabase(IosContext)

    return ComposeUIViewController { App() }
}
