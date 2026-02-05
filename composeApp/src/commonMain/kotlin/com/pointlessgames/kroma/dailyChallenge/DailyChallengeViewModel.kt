package com.pointlessgames.kroma.dailyChallenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.kroma.Generator
import com.pointlessgames.kroma.Solver
import com.pointlessgames.kroma.data.SettingsRepository
import com.pointlessgames.kroma.model.FinishableLevelData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal class DailyChallengeViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            .date.let { today ->
                UiState(
                    dates = listOf(
                        today.minus(2, DateTimeUnit.DAY),
                        today.minus(1, DateTimeUnit.DAY),
                        today,
                    ),
                    currentDate = today,
                )
            },
    )
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()

    init {
        loadDate(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }

    fun loadDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                isLoading = true,
                currentDate = date,
            )
        }

        viewModelScope.launch {
            Generator.setSeed(date.toEpochDays())
            val levels = MutableList(12) { Generator.generate() }.filterNotNull()

            val sections = levels.groupBy {
                val complexity = Solver.getBestMoveSequence(it)?.size ?: 0
                when {
                    complexity <= 4 -> 0
                    complexity <= 10 -> 1
                    else -> 2
                }
            }

            val finishedLevelIds = settingsRepository.areDailyChallengeLevelsFinished(
                ids = levels.map { it.id },
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    sections = sections
                        .mapValues { (_, value) ->
                            value.map { level ->
                                FinishableLevelData(
                                    levelData = level,
                                    isFinished = finishedLevelIds[level.id] ?: false,
                                )
                            }
                        }
                        .entries
                        .sortedBy { entry -> entry.key }
                        .map(Map.Entry<Int, List<FinishableLevelData>>::toPair),
                )
            }
        }
    }

    fun markLevelAsFinished(level: FinishableLevelData) {
        val id = level.levelData.id
        viewModelScope.launch {
            settingsRepository.addDailyChallengeLevelFinished(id)
        }

        _uiState.update {
            it.copy(
                sections = it.sections.map { (key, value) ->
                    key to value.map { level ->
                        level.copy(isFinished = level.isFinished || level.levelData.id == id)
                    }
                },
            )
        }
    }

    data class UiState(
        val dates: List<LocalDate>,
        val currentDate: LocalDate,
        val isLoading: Boolean = true,
        val sections: List<Pair<Int, List<FinishableLevelData>>> = emptyList(),
    )
}
