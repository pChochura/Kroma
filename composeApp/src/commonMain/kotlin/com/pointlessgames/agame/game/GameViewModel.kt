package com.pointlessgames.agame.game

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.agame.Game
import com.pointlessgames.agame.Solver
import com.pointlessgames.agame.data.LevelRepository
import com.pointlessgames.agame.model.Direction
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.model.UndoState
import com.pointlessgames.agame.utils.UndoManager
import com.pointlessgames.agame.utils.toDegrees
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.atan2

internal class GameViewModel(
    private val levelRepository: LevelRepository,
) : ViewModel() {

    private val undoManager = UndoManager<UndoState>()

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()

    private val loadedState: UiState.Loaded
        get() = uiState.value as UiState.Loaded

    private var firstUnfinishedLevelId: Long? = null
    private var swipeAngle = 0.0

    private var currentAsyncJob: Job? = null

    fun loadStoredLevels() {
        currentAsyncJob?.cancel()
        currentAsyncJob = viewModelScope.launch {
            val levels = levelRepository.getLevels()
            loadLevels(levels)
        }
    }

    fun loadLevels(levels: List<LevelData>) {
        currentAsyncJob?.cancel()
        currentAsyncJob = viewModelScope.launch {
            firstUnfinishedLevelId = levelRepository.getFirstUnfinishedLevelId()
            val firstUnfinishedLevelIndex = levels
                .indexOfFirst { it.id == firstUnfinishedLevelId }
                .takeIf { it != -1 } ?: 0

            val nextLevelData = levels[firstUnfinishedLevelIndex]
            Solver.clearCache()
            undoManager.clear()
            _uiState.update {
                UiState.Loaded(
                    levels = levels,
                    level = firstUnfinishedLevelIndex,
                    levelData = nextLevelData,

                    canMovePreviousLevel = firstUnfinishedLevelIndex > 0,
                    canMoveNextLevel = false,

                    canHint = !Solver.getBestMoveSequence(nextLevelData).isNullOrEmpty(),
                    possibleMoves = Game.getPossibleMoves(nextLevelData),
                )
            }
        }
    }

    private fun loadNextLevel() {
        val levelData = loadedState.levels[loadedState.level + 1]
        val firstUnfinishedLevelIndex = loadedState.levels.indexOfFirst {
            it.id == firstUnfinishedLevelId
        }

        Solver.clearCache()
        undoManager.clear()
        _uiState.update {
            loadedState.copy(
                level = loadedState.level + 1,
                levelData = levelData,

                canMovePreviousLevel = loadedState.level + 1 > 0,
                canMoveNextLevel = loadedState.level + 1 < firstUnfinishedLevelIndex,

                canHint = !Solver.getBestMoveSequence(levelData).isNullOrEmpty(),
                possibleMoves = Game.getPossibleMoves(levelData),
            )
        }

        updateState(levelData)
    }

    private fun loadPreviousLevel() {
        val levelData = loadedState.levels[loadedState.level - 1]
        val firstUnfinishedLevelIndex = loadedState.levels.indexOfFirst {
            it.id == firstUnfinishedLevelId
        }

        Solver.clearCache()
        undoManager.clear()
        _uiState.update {
            loadedState.copy(
                level = loadedState.level - 1,
                levelData = levelData,

                canMovePreviousLevel = loadedState.level - 1 > 0,
                canMoveNextLevel = loadedState.level - 1 < firstUnfinishedLevelIndex,

                possibleMoves = Game.getPossibleMoves(levelData),
            )
        }

        updateState(levelData)
    }

    private fun updateState(levelData: LevelData) {
        _uiState.update {
            loadedState.copy(
                levelData = levelData,
                possibleMoves = Game.getPossibleMoves(levelData),
                canUndo = undoManager.canUndo(),
                canRedo = undoManager.canRedo(),
                canRestart = undoManager.canUndo(),
            )
        }

        currentAsyncJob?.cancel()
        currentAsyncJob = viewModelScope.launch {
            _uiState.update {
                loadedState.copy(
                    canHint = !Solver.getBestMoveSequence(levelData).isNullOrEmpty(),
                )
            }
        }
    }

    fun onDrag(dragAmount: Offset) {
        swipeAngle = atan2(-dragAmount.y, dragAmount.x).toDegrees()
    }

    fun onDragEnd() {
        val moveDirection = when (swipeAngle) {
            in 45.0..<135.0 -> Direction.TOP
            in 135.0..<225.0 -> Direction.LEFT
            in 225.0..<315.0 -> Direction.BOTTOM
            else -> Direction.RIGHT
        }

        if (!loadedState.canMove(moveDirection)) return

        undoManager.insertState(
            UndoState(
                currentPosition = loadedState.levelData.currentPosition,
                gridTiles = loadedState.levelData.tiles.toMap(),
            ),
        )

        updateState(
            levelData = Game.performMove(
                levelData = loadedState.levelData,
                moveDirection = moveDirection,
            ),
        )
    }

    fun onAnimationsFinished() {
        if (Game.isFinished(loadedState.levelData)) {
            firstUnfinishedLevelId = loadedState.levels.getOrNull(loadedState.level + 1)?.id
            viewModelScope.launch {
                levelRepository.markLevelAsFinished(loadedState.levelData.id)
            }

            if (firstUnfinishedLevelId == null) {
                eventChannel.trySend(Event.GameFinished)

                return
            }

            onNextLevelClicked()
        }
    }

    fun onUndoClicked() {
        if (!undoManager.canRedo()) {
            undoManager.insertState(
                UndoState(
                    currentPosition = loadedState.levelData.currentPosition,
                    gridTiles = loadedState.levelData.tiles.toMap(),
                ),
            )
        }

        val state = undoManager.undo()
        updateState(
            levelData = loadedState.levelData.copy(
                currentPosition = state.currentPosition,
                tiles = state.gridTiles,
            ),
        )
    }

    fun onRedoClicked() {
        val state = undoManager.redo()
        updateState(
            levelData = loadedState.levelData.copy(
                currentPosition = state.currentPosition,
                tiles = state.gridTiles,
            ),
        )
    }

    fun onRestartClicked() {
        val state = undoManager.clear() ?: return
        updateState(
            levelData = loadedState.levelData.copy(
                currentPosition = state.currentPosition,
                tiles = state.gridTiles,
            ),
        )
    }

    fun onHintClicked() {
        val nextMove = requireNotNull(Solver.getBestNextMove(loadedState.levelData)) {
            "Could not find a solution for this level."
        }

        undoManager.insertState(
            UndoState(
                currentPosition = loadedState.levelData.currentPosition,
                gridTiles = loadedState.levelData.tiles.toMap(),
            ),
        )

        updateState(
            levelData = Game.performMove(
                levelData = loadedState.levelData,
                moveDirection = nextMove,
            ),
        )
    }

    fun onPreviousLevelClicked() {
        loadPreviousLevel()
        _uiState.update { loadedState.copy(animationMovesForward = false) }
    }

    fun onNextLevelClicked() {
        loadNextLevel()
        _uiState.update { loadedState.copy(animationMovesForward = true) }
    }

    fun onBackClicked() {
        eventChannel.trySend(Event.GoBack)
    }

    sealed class UiState {
        data class Loaded(
            val levels: List<LevelData>,
            val level: Int,
            val levelData: LevelData,

            val animationMovesForward: Boolean = true,

            val canMovePreviousLevel: Boolean = false,
            val canMoveNextLevel: Boolean = false,

            val canUndo: Boolean = false,
            val canRedo: Boolean = false,
            val canRestart: Boolean = false,
            val canHint: Boolean = false,

            val possibleMoves: Set<Direction> = emptySet(),
        ) : UiState() {
            fun canMove(direction: Direction): Boolean = possibleMoves.contains(direction)
        }

        data object Loading : UiState()
    }

    sealed interface Event {
        data object GameFinished : Event
        data object GoBack : Event
    }
}
