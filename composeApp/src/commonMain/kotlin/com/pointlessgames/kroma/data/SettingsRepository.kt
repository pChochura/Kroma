package com.pointlessgames.kroma.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Clock

class SettingsRepository(
    private val appSettings: DataStore<Preferences>,
) {
    private val lastHintsUsedKey = stringSetPreferencesKey("last_hints_used")

    private fun Set<String>.trimOldest(limit: Int = 10) =
        this.sortedDescending().take(limit).toSet()

    suspend fun addLastHintUsed(
        timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    ) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[lastHintsUsedKey] =
                    (prefs[lastHintsUsedKey].orEmpty() + timestamp.toString()).trimOldest(10)
            }
        }
    }

    suspend fun getCooldownUntilNextHint(): Long = withContext(Dispatchers.IO) {
        val currentTimestamp = Clock.System.now().toEpochMilliseconds()
        val windowDuration = (currentTimestamp - MAX_COOLDOWN)..currentTimestamp
        val timestamps = appSettings.data.first()[lastHintsUsedKey]
            .orEmpty()
            .map { it.toLong() }
            .filter { it in windowDuration }
            .sorted()

        if (timestamps.isEmpty()) {
            return@withContext 0L
        }

        val cooldownDuration = (BASE_COOLDOWN * 2.0.pow(timestamps.size - 1)).toLong()
        return@withContext max(0L, timestamps.last() + cooldownDuration - currentTimestamp)
    }

    private companion object {
        const val BASE_COOLDOWN = 5 * 1000L // 5 seconds
        const val MAX_COOLDOWN = 5 * 60 * 1000L // 5 minutes
    }
}
