package com.pointlessgames.agame

import androidx.compose.ui.window.ComposeUIViewController
import com.pointlessgames.agame.di.initKoin
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        initKoin()
        App()
    }
}
