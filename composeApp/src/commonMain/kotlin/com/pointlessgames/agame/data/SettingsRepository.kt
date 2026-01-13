package com.pointlessgames.agame.data

import androidx.datastore.preferences.core.longPreferencesKey

class SettingsRepository {

    private val lastFinishedLevelKey = longPreferencesKey("last_finished_level")
}
