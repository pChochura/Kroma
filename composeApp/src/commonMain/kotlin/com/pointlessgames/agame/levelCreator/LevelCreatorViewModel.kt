package com.pointlessgames.agame.levelCreator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.agame.Generator
import com.pointlessgames.agame.data.LevelRepository
import com.pointlessgames.agame.model.GridTile
import com.pointlessgames.agame.model.GridTile.Companion.MAX_VALUE
import com.pointlessgames.agame.model.GridTile.Companion.MIN_VALUE
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.model.Position
import com.pointlessgames.agame.utils.next
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LevelCreatorViewModel(
    private val levelRepository: LevelRepository,
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()

    private var currentAsyncJob: Job? = null

    fun onTileClicked(position: Position) {
        val currentTile = uiState.value.gridTiles[position] ?: GridTile.Empty
        val nextTile = currentTile.copy(
            value = currentTile.value.next(MIN_VALUE, MAX_VALUE),
        )
        _uiState.update { it.copy(gridTiles = it.gridTiles + (position to nextTile)) }
    }

    fun onStartChanged(position: Position?) {
        if (position == null) return

        _uiState.update { it.copy(startingPosition = position) }
    }

    fun onEndChanged(position: Position?) {
        if (position == null) return

        _uiState.update { it.copy(endingPosition = position) }
    }

    fun onTestLevelClicked() {
        eventChannel.trySend(
            Event.TestLevel(
                LevelData(
                    width = uiState.value.width,
                    height = uiState.value.height,
                    currentPosition = uiState.value.startingPosition,
                    endingPosition = uiState.value.endingPosition,
                    tiles = uiState.value.gridTiles,
                )
            ),
        )
    }

    fun onResetClicked() {
        _uiState.update {
            it.copy(
                startingPosition = Position(0, 0),
                endingPosition = Position(0, 0),
                gridTiles = emptyMap(),
            )
        }
    }

    fun onGenerateLevelClicked() {
        currentAsyncJob?.cancel()
        currentAsyncJob = viewModelScope.launch {
            val generatedLevelData = Generator.generate(
                width = uiState.value.width,
                height = uiState.value.height,
            ) ?: return@launch

            _uiState.update {
                it.copy(
                    width = generatedLevelData.width,
                    height = generatedLevelData.height,
                    startingPosition = generatedLevelData.currentPosition,
                    endingPosition = generatedLevelData.endingPosition,
                    gridTiles = generatedLevelData.tiles,
                )
            }
        }
    }

    fun onSaveLevelClicked() {
        viewModelScope.launch {
            levelRepository.addLevel(
                levelData = LevelData(
                    width = uiState.value.width,
                    height = uiState.value.height,
                    currentPosition = uiState.value.startingPosition.copy(
                        x = uiState.value.startingPosition.x.coerceIn(0 until uiState.value.width),
                        y = uiState.value.startingPosition.y.coerceIn(0 until uiState.value.height),
                    ),
                    endingPosition = uiState.value.endingPosition.copy(
                        x = uiState.value.endingPosition.x.coerceIn(0 until uiState.value.width),
                        y = uiState.value.endingPosition.y.coerceIn(0 until uiState.value.height),
                    ),
                    tiles = uiState.value.gridTiles.filter { (key, value) ->
                        value.value != GridTile.Empty.value &&
                                key.x in 0 until uiState.value.width &&
                                key.y in 0 until uiState.value.height
                    },
                )
            )
        }
    }

    fun onSizeChanged(width: Int, height: Int) {
        _uiState.update {
            it.copy(
                width = width,
                height = height,
            )
        }
    }

    fun onBackClicked() {
        eventChannel.trySend(Event.GoBack)
    }

    data class UiState(
        val width: Int = 3,
        val height: Int = 3,
        val startingPosition: Position = Position(0, 0),
        val endingPosition: Position = Position(0, 0),
        val gridTiles: Map<Position, GridTile> = emptyMap(),
    )

    sealed interface Event {
        data class TestLevel(val levelData: LevelData) : Event
        data object GoBack : Event
    }
}
