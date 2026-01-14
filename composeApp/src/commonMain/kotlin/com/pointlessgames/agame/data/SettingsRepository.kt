package com.pointlessgames.agame.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey

class SettingsRepository(
    private val appSettings: DataStore<Preferences>,
) {

    private val lastFinishedLevelKey = longPreferencesKey("last_finished_level")
}
