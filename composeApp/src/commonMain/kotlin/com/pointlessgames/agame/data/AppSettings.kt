package com.pointlessgames.agame.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

const val appSettingsFileName = "app_settings.preferences_pb"
lateinit var AppSettings: DataStore<Preferences>

fun initializeAppSettings(producePath: () -> String) {
    if (!::AppSettings.isInitialized) {
        AppSettings = PreferenceDataStoreFactory.createWithPath(
            produceFile = { producePath().toPath() },
        )
    }
}
