package com.pointlessgames.agame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pointlessgames.agame.data.appSettingsFileName
import com.pointlessgames.agame.data.initializeAppDatabase
import com.pointlessgames.agame.data.initializeAppSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )
        super.onCreate(savedInstanceState)
        initializeAppDatabase(applicationContext)
        initializeAppSettings {
            applicationContext.filesDir.resolve(appSettingsFileName).absolutePath
        }

        setContent { App() }
    }
}
