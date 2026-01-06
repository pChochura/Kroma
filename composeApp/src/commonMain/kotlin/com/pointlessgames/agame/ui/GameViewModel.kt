package com.pointlessgames.agame.ui

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.pointlessgames.agame.model.Direction
import com.pointlessgames.agame.model.Direction.BOTTOM
import com.pointlessgames.agame.model.Direction.LEFT
import com.pointlessgames.agame.model.Direction.RIGHT
import com.pointlessgames.agame.model.Direction.TOP
import com.pointlessgames.agame.model.GridTile
import com.pointlessgames.agame.model.LevelData
import com.pointlessgames.agame.model.Position
import com.pointlessgames.agame.ui.model.UndoState
import com.pointlessgames.agame.utils.UndoManager
import com.pointlessgames.agame.utils.toDegrees
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
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

    fun loadLevel(levelData: LevelData) {
        _uiState.update {
            GameUiState.Loaded(
                width = levelData.width,
                height = levelData.height,
                currentPosition = levelData.startingPosition,
                endingPosition = levelData.endingPosition,
                gridTiles = levelData.tiles,
                isFinished = false,
            )
        }
        calculatePossibleMoves()
    }

    private fun performMove(
        edgeCondition: (Position) -> Boolean,
        transformedPosition: (Position) -> Position,
        showDirection: Direction,
    ) {
        val currentTile = loadedState.currentTile
        var currentPosition = loadedState.currentPosition
        val currentGridTiles = loadedState.gridTiles.toMutableMap()

        val nextPosition = transformedPosition(currentPosition)
        currentGridTiles[nextPosition]?.let {
            if (it.value != currentTile.value) {
                _uiState.update {
                    loadedState.copy(
                        currentPosition = nextPosition,
                        gridTiles = loadedState.gridTiles + (nextPosition to GridTile(
                            value = currentTile.value,
                            showFromDirection = showDirection,
                        )),
                    )
                }
            }

            return
        }

        var offset = 0
        while (
            edgeCondition(currentPosition) &&
            !currentGridTiles.contains(transformedPosition(currentPosition))
        ) {
            currentPosition = transformedPosition(currentPosition)
            currentGridTiles[currentPosition] = GridTile(
                value = currentTile.value,
                showFromDirection = showDirection,
                animationOffset = offset++,
            )
        }

        if (
            edgeCondition(currentPosition) &&
            currentGridTiles[transformedPosition(currentPosition)]?.value != currentTile.value
        ) {
            currentPosition = transformedPosition(currentPosition)
        }

        _uiState.update {
            loadedState.copy(
                currentPosition = currentPosition,
                gridTiles = currentGridTiles,
            )
        }
    }

    private fun calculatePossibleMoves() {
        val possibleMoves = buildSet {
            if (
                calculateCanMove(
                    edgeCondition = { it.x - 1 >= 0 },
                    transformedPosition = { it.copy(x = it.x - 1) },
                )
            ) {
                add(LEFT)
            }

            if (
                calculateCanMove(
                    edgeCondition = { it.x + 1 < loadedState.width },
                    transformedPosition = { it.copy(x = it.x + 1) },
                )
            ) {
                add(RIGHT)
            }

            if (
                calculateCanMove(
                    edgeCondition = { it.y - 1 >= 0 },
                    transformedPosition = { it.copy(y = it.y - 1) },
                )
            ) {
                add(TOP)
            }

            if (
                calculateCanMove(
                    edgeCondition = { it.y + 1 < loadedState.height },
                    transformedPosition = { it.copy(y = it.y + 1) },
                )
            ) {
                add(BOTTOM)
            }
        }

        _uiState.update { loadedState.copy(possibleMoves = possibleMoves) }
    }

    private fun calculateCanMove(
        edgeCondition: (Position) -> Boolean,
        transformedPosition: (Position) -> Position,
    ): Boolean {
        val currentTile = loadedState.currentTile
        var currentPosition = loadedState.currentPosition
        val currentGridTiles = loadedState.gridTiles

        currentGridTiles[transformedPosition(currentPosition)]?.let {
            return it.value != currentTile.value
        }

        while (
            edgeCondition(currentPosition) &&
            !currentGridTiles.contains(transformedPosition(currentPosition))
        ) {
            currentPosition = transformedPosition(currentPosition)
        }

        return edgeCondition(currentPosition) &&
                currentGridTiles[transformedPosition(currentPosition)]?.value != currentTile.value
    }

    private fun isFinished(): Boolean =
        loadedState.currentPosition == loadedState.endingPosition

    private fun updateState() {
        _uiState.update {
            loadedState.copy(
                isFinished = isFinished(),
                canUndo = undoManager.canUndo(),
                canRedo = undoManager.canRedo(),
                canRestart = undoManager.canUndo(),
                canHint = false,
            )
        }
    }

    fun onDrag(dragAmount: Offset) {
        swipeAngle = atan2(-dragAmount.y, dragAmount.x).toDegrees()
    }

    fun onDragEnd() {
        val moveDirection = when (swipeAngle) {
            in 45.0..<135.0 -> TOP
            in 135.0..<225.0 -> LEFT
            in 225.0..<315.0 -> BOTTOM
            else -> RIGHT
        }

        val state = uiState.value
        if (!state.canMove(moveDirection)) return

        undoManager.insertState(
            UndoState(
                currentPosition = state.currentPosition,
                gridTiles = state.gridTiles.toMap(),
            ),
        )

        when (moveDirection) {
            LEFT -> performMove(
                edgeCondition = { it.x - 1 >= 0 },
                transformedPosition = { it.copy(x = it.x - 1) },
                showDirection = moveDirection.opposite,
            )

            RIGHT -> performMove(
                edgeCondition = { it.x + 1 < state.width },
                transformedPosition = { it.copy(x = it.x + 1) },
                showDirection = moveDirection.opposite,
            )

            TOP -> performMove(
                edgeCondition = { it.y - 1 >= 0 },
                transformedPosition = { it.copy(y = it.y - 1) },
                showDirection = moveDirection.opposite,
            )

            BOTTOM -> performMove(
                edgeCondition = { it.y + 1 < state.height },
                transformedPosition = { it.copy(y = it.y + 1) },
                showDirection = moveDirection.opposite,
            )
        }
        calculatePossibleMoves()
        updateState()
    }

    fun onAnimationsFinished() {
        if (loadedState.isFinished) {
            eventChannel.trySend(Event.LevelFinished)
        }
    }

    fun onUndoClicked() {
        if (!undoManager.canRedo()) {
            undoManager.insertState(
                UndoState(
                    currentPosition = loadedState.currentPosition,
                    gridTiles = loadedState.gridTiles.toMap(),
                ),
            )
        }

        val state = undoManager.undo()
        _uiState.update {
            loadedState.copy(
                currentPosition = state.currentPosition,
                gridTiles = state.gridTiles,
            )
        }
        calculatePossibleMoves()
        updateState()
    }

    fun onRedoClicked() {
        val state = undoManager.redo()
        _uiState.update {
            loadedState.copy(
                currentPosition = state.currentPosition,
                gridTiles = state.gridTiles,
            )
        }
        calculatePossibleMoves()
        updateState()
    }

    fun onRestartClicked() {
        val state = undoManager.clear()
        _uiState.update {
            loadedState.copy(
                currentPosition = state.currentPosition,
                gridTiles = state.gridTiles,
            )
        }
        calculatePossibleMoves()
        updateState()
    }

    fun onHintClicked() {}

    sealed class GameUiState {
        data class Loaded(
            val isFinished: Boolean = false,

            val width: Int = 1,
            val height: Int = 1,
            val canUndo: Boolean = false,
            val canRedo: Boolean = false,
            val canRestart: Boolean = false,
            val canHint: Boolean = false,
            val currentPosition: Position = Position(0, 0),
            val endingPosition: Position = Position(0, 0),
            val gridTiles: Map<Position, GridTile> = emptyMap(),
            val possibleMoves: Set<Direction> = emptySet(),
        ) : GameUiState() {
            val currentTile: GridTile
                get() = gridTiles.getValue(currentPosition)
        }

        data object Loading : GameUiState()

        @OptIn(ExperimentalContracts::class)
        fun canMove(direction: Direction): Boolean {
            contract {
                returns(true) implies (this@GameUiState is Loaded)
            }

            return this is Loaded && possibleMoves.contains(direction)
        }
    }

    sealed interface Event {
        data object LevelFinished : Event
    }
}
