package com.pointlessgames.agame.ui.level

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.pointlessgames.agame.Game
import com.pointlessgames.agame.Solver
import com.pointlessgames.agame.model.Direction
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.model.UndoState
import com.pointlessgames.agame.utils.UndoManager
import com.pointlessgames.agame.utils.toDegrees
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlin.math.atan2

internal class GameViewModel : ViewModel() {

    private val undoManager = UndoManager<UndoState>()

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _uiState: MutableStateFlow<GameUiState> = MutableStateFlow(GameUiState.Loading)
    val uiState: StateFlow<GameUiState>
        get() = _uiState.asStateFlow()

    private val loadedState: GameUiState.Loaded
        get() = uiState.value as GameUiState.Loaded

    private var swipeAngle = 0.0

    fun loadLevels(levels: List<LevelData>) {
        Solver.clearCache()
        undoManager.clear()
        _uiState.update {
            val levelData = levels.first()
            GameUiState.Loaded(
                levels = levels,
                level = 0,
                levelData = levelData,

                canMovePreviousLevel = false,
                canMoveNextLevel = levels.size > 1,

                canHint = !Solver.getBestMoveSequence(levelData).isNullOrEmpty(),
                possibleMoves = Game.getPossibleMoves(levelData),
            )
        }
    }

    private fun loadNextLevel() {
        Solver.clearCache()
        undoManager.clear()
        _uiState.update {
            val levelData = loadedState.levels[loadedState.level + 1]
            loadedState.copy(
                level = loadedState.level + 1,
                levelData = levelData,

                canMovePreviousLevel = loadedState.level + 1 > 0,
                canMoveNextLevel = loadedState.level + 1 < loadedState.levels.lastIndex,

                canHint = !Solver.getBestMoveSequence(levelData).isNullOrEmpty(),
                possibleMoves = Game.getPossibleMoves(levelData),
            )
        }
    }

    private fun loadPreviousLevel() {
        Solver.clearCache()
        undoManager.clear()
        _uiState.update {
            val levelData = loadedState.levels[loadedState.level - 1]
            loadedState.copy(
                level = loadedState.level - 1,
                levelData = levelData,

                canMovePreviousLevel = loadedState.level - 1 > 0,
                canMoveNextLevel = loadedState.level - 1 < loadedState.levels.lastIndex,

                canHint = !Solver.getBestMoveSequence(levelData).isNullOrEmpty(),
                possibleMoves = Game.getPossibleMoves(levelData),
            )
        }
    }

    private fun updateState(levelData: LevelData) {
        _uiState.update {
            loadedState.copy(
                levelData = levelData,
                possibleMoves = Game.getPossibleMoves(levelData),
                canUndo = undoManager.canUndo(),
                canRedo = undoManager.canRedo(),
                canRestart = undoManager.canUndo(),
                canHint = !Solver.getBestMoveSequence(levelData).isNullOrEmpty(),
            )
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
            if (!loadedState.canMoveNextLevel) {
                eventChannel.trySend(Event.Finished)

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

    fun onLevelCreatorClicked() {
        eventChannel.trySend(Event.Finished)
    }

    sealed class GameUiState {
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
        ) : GameUiState() {
            fun canMove(direction: Direction): Boolean = possibleMoves.contains(direction)
        }

        data object Loading : GameUiState()
    }

    sealed interface Event {
        data object Finished : Event
    }
}
