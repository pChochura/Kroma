package com.pointlessgames.kroma.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.time.Clock

class SettingsRepository(
    private val appSettings: DataStore<Preferences>,
) {
    private val lastHintsUsedKey = stringSetPreferencesKey("last_hints_used")

    private fun Set<String>.trimLatest(limit: Int = 10) =
        this.sortedDescending().take(limit).toSet()

    suspend fun addLastHintUsed(
        timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    ) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[lastHintsUsedKey] =
                    (prefs[lastHintsUsedKey].orEmpty() + timestamp.toString()).trimLatest(10)
            }
        }
    }

    suspend fun getCooldownUntilNextHint(): Long = withContext(Dispatchers.IO) {
        val timestamps = appSettings.data.first()[lastHintsUsedKey]
            .orEmpty()
            .map { it.toLong() }
            .sorted()

        val currentTime = Clock.System.now().toEpochMilliseconds()

        if (timestamps.isEmpty()) {
            return@withContext 0L
        }

        val latestTimestamp = timestamps.last()

        val calculatedCooldownDuration = if (timestamps.size < 2) {
            BASE_COOLDOWN_MS
        } else {
            val earliestTimestamp = timestamps.first()
            val timeWindow = latestTimestamp - earliestTimestamp
            val averageInterval = if (timestamps.size > 1) {
                timeWindow / (timestamps.size - 1)
            } else {
                MAX_AVG_INTERVAL_FOR_BASE_COOLDOWN_MS
            }

            if (averageInterval >= MAX_AVG_INTERVAL_FOR_BASE_COOLDOWN_MS) {
                BASE_COOLDOWN_MS
            } else {
                val intervalRange = MAX_AVG_INTERVAL_FOR_BASE_COOLDOWN_MS.toFloat()
                val positionInIntervalRange = averageInterval.toFloat()
                val ratio = (positionInIntervalRange / intervalRange).coerceIn(0f, 1f)

                (MAX_COOLDOWN_MS - (MAX_COOLDOWN_MS - BASE_COOLDOWN_MS) * ratio).toLong()
            }
        }

        return@withContext max(0L, latestTimestamp + calculatedCooldownDuration - currentTime)
    }

    private companion object {
        const val BASE_COOLDOWN_MS = 5 * 1000L // 5 seconds
        const val MAX_COOLDOWN_MS = 5 * 60 * 1000L // 5 minutes

        const val MAX_AVG_INTERVAL_FOR_BASE_COOLDOWN_MS = 120 * 1000L // 2 minutes
    }
}
