package com.pointlessgames.kroma

import androidx.compose.ui.window.ComposeUIViewController
import com.pointlessgames.kroma.di.initKoin
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        initKoin()
        App()
    }
}
