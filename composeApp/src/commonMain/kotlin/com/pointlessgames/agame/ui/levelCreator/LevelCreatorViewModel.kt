package com.pointlessgames.agame.ui.levelCreator

import androidx.lifecycle.ViewModel
import com.pointlessgames.agame.model.GridTile
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.model.Position
import com.pointlessgames.agame.ui.levelCreator.LevelCreatorViewModel.LevelCreatorUiState.SelectionMode.End
import com.pointlessgames.agame.ui.levelCreator.LevelCreatorViewModel.LevelCreatorUiState.SelectionMode.None
import com.pointlessgames.agame.ui.levelCreator.LevelCreatorViewModel.LevelCreatorUiState.SelectionMode.Start
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

internal class LevelCreatorViewModel : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _uiState = MutableStateFlow(LevelCreatorUiState())
    val uiState: StateFlow<LevelCreatorUiState>
        get() = _uiState.asStateFlow()

    fun onTileClicked(position: Position) {
        when (uiState.value.selectionMode) {
            None -> {
                val currentTile = uiState.value.gridTiles[position] ?: GridTile.Empty
                val nextTile = currentTile.copy(value = currentTile.value + 1)
                _uiState.update {
                    it.copy(
                        gridTiles = if (nextTile.value > 2) {
                            it.gridTiles - position
                        } else {
                            it.gridTiles + (position to nextTile)
                        },
                    )
                }
            }

            Start -> _uiState.update { it.copy(startingPosition = position) }
            End -> _uiState.update { it.copy(endingPosition = position) }
        }
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
        when (uiState.value.selectionMode) {
            None -> _uiState.update { it.copy(selectionMode = Start) }
            Start -> _uiState.update { it.copy(selectionMode = End) }
            else -> eventChannel.trySend(
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
    }

    fun reset() {
        _uiState.update {
            it.copy(
                width = 3,
                height = 3,
                startingPosition = Position(0, 0),
                endingPosition = Position(0, 0),
                gridTiles = emptyMap(),
                selectionMode = None,
            )
        }
    }

    data class LevelCreatorUiState(
        val width: Int = 3,
        val height: Int = 3,
        val startingPosition: Position = Position(0, 0),
        val endingPosition: Position = Position(0, 0),
        val gridTiles: Map<Position, GridTile> = emptyMap(),
        val selectionMode: SelectionMode = None,
    ) {
        enum class SelectionMode { None, Start, End }
    }

    sealed interface Event {
        data class LevelCreated(val levelData: LevelData) : Event
    }
}
