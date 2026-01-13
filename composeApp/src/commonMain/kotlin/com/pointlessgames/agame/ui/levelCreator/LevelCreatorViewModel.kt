package com.pointlessgames.agame.ui.levelCreator

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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LevelCreatorViewModel(
    private val levelRepository: LevelRepository = LevelRepository(),
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _uiState = MutableStateFlow(LevelCreatorUiState())
    val uiState: StateFlow<LevelCreatorUiState>
        get() = _uiState.asStateFlow()

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

    fun onWidthIncrement() {
        _uiState.update { it.copy(width = it.width + 1) }
    }

    fun onWidthDecrement() {
        _uiState.update { it.copy(width = it.width - 1) }
    }

    fun onHeightIncrement() {
        _uiState.update { it.copy(height = it.height + 1) }
    }

    fun onHeightDecrement() {
        _uiState.update { it.copy(height = it.height - 1) }
    }

    fun onStartClicked() {
        eventChannel.trySend(
            Event.LevelCreated(
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

    fun onRestartClicked() {
        _uiState.update {
            it.copy(
                startingPosition = Position(0, 0),
                endingPosition = Position(0, 0),
                gridTiles = emptyMap(),
            )
        }
    }

    fun onGenerateLevelClicked() {
        val generatedLevelData = Generator.generate(
            width = uiState.value.width,
            height = uiState.value.height,
        ) ?: return

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

    fun onSaveLevelClicked() {
        viewModelScope.launch {
            levelRepository.addLevel(
                levelData = LevelData(
                    width = uiState.value.width,
                    height = uiState.value.height,
                    currentPosition = uiState.value.startingPosition,
                    endingPosition = uiState.value.endingPosition,
                    tiles = uiState.value.gridTiles.filterValues {
                        it.value != GridTile.Empty.value
                    },
                )
            )
        }
    }

    data class LevelCreatorUiState(
        val width: Int = 3,
        val height: Int = 3,
        val startingPosition: Position = Position(0, 0),
        val endingPosition: Position = Position(0, 0),
        val gridTiles: Map<Position, GridTile> = emptyMap(),
    )

    sealed interface Event {
        data class LevelCreated(val levelData: LevelData) : Event
    }
}
