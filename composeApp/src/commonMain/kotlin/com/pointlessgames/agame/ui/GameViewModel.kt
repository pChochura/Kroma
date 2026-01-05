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
import kotlin.math.atan2

internal class GameViewModel : ViewModel() {

    private val undoManager = UndoManager<UndoState>()

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState>
        get() = _uiState.asStateFlow()

    private var swipeAngle = 0.0

    fun loadLevel(levelData: LevelData) {
        _uiState.update {
            it.copy(
                width = levelData.width,
                height = levelData.height,
                currentPosition = levelData.startingPosition,
                endingPosition = levelData.endingPosition,
                gridTiles = levelData.tiles,
            )
        }
        calculatePossibleMoves()
    }

    fun onDrag(dragAmount: Offset) {
        swipeAngle = atan2(-dragAmount.y, dragAmount.x).toDegrees()
    }

    fun onDragEnd() {
        if (uiState.value.possibleMoves.isEmpty()) return

        undoManager.insertState(
            UndoState(
                currentPosition = uiState.value.currentPosition,
                gridTiles = uiState.value.gridTiles.toMap(),
            ),
        )

        val moveDirection = when (swipeAngle) {
            in 45.0..<135.0 -> TOP
            in 135.0..<225.0 -> LEFT
            in 225.0..<315.0 -> BOTTOM
            else -> RIGHT
        }

        when (moveDirection) {
            LEFT -> performMove(
                edgeCondition = { it.x - 1 >= 0 },
                transformedPosition = { it.copy(x = it.x - 1) },
                showDirection = moveDirection.opposite,
            )

            RIGHT -> performMove(
                edgeCondition = { it.x + 1 < uiState.value.width },
                transformedPosition = { it.copy(x = it.x + 1) },
                showDirection = moveDirection.opposite,
            )

            TOP -> performMove(
                edgeCondition = { it.y - 1 >= 0 },
                transformedPosition = { it.copy(y = it.y - 1) },
                showDirection = moveDirection.opposite,
            )

            BOTTOM -> performMove(
                edgeCondition = { it.y + 1 < uiState.value.height },
                transformedPosition = { it.copy(y = it.y + 1) },
                showDirection = moveDirection.opposite,
            )
        }
        calculatePossibleMoves()
        _uiState.update {
            it.copy(
                canUndo = undoManager.canUndo(),
                canRedo = undoManager.canRedo(),
            )
        }

        if (uiState.value.currentPosition == uiState.value.endingPosition) {
            eventChannel.trySend(Event.LevelFinished)
        }
    }

    private fun performMove(
        edgeCondition: (Position) -> Boolean,
        transformedPosition: (Position) -> Position,
        showDirection: Direction,
    ) {
        val currentTile = uiState.value.currentTile
        var currentPosition = uiState.value.currentPosition
        val currentGridTiles = uiState.value.gridTiles.toMutableMap()

        val nextPosition = transformedPosition(currentPosition)
        currentGridTiles[nextPosition]?.let {
            if (it.value != currentTile.value) {
                _uiState.update { state ->
                    state.copy(
                        currentPosition = nextPosition,
                        gridTiles = state.gridTiles + (nextPosition to GridTile(
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
            it.copy(
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
                    edgeCondition = { it.x + 1 < uiState.value.width },
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
                    edgeCondition = { it.y + 1 < uiState.value.height },
                    transformedPosition = { it.copy(y = it.y + 1) },
                )
            ) {
                add(BOTTOM)
            }
        }

        _uiState.update { it.copy(possibleMoves = possibleMoves) }
    }

    private fun calculateCanMove(
        edgeCondition: (Position) -> Boolean,
        transformedPosition: (Position) -> Position,
    ): Boolean {
        val currentTile = uiState.value.currentTile
        var currentPosition = uiState.value.currentPosition
        val currentGridTiles = uiState.value.gridTiles

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

    fun onUndoClicked() {
        if (!undoManager.canRedo()) {
            undoManager.insertState(
                UndoState(
                    currentPosition = uiState.value.currentPosition,
                    gridTiles = uiState.value.gridTiles.toMap(),
                ),
            )
        }

        val state = undoManager.undo()
        _uiState.update {
            it.copy(
                currentPosition = state.currentPosition,
                gridTiles = state.gridTiles,
                canUndo = undoManager.canUndo(),
                canRedo = undoManager.canRedo(),
            )
        }
        calculatePossibleMoves()
    }

    fun onRedoClicked() {
        val state = undoManager.redo()
        _uiState.update {
            it.copy(
                currentPosition = state.currentPosition,
                gridTiles = state.gridTiles,
                canUndo = undoManager.canUndo(),
                canRedo = undoManager.canRedo(),
            )
        }
        calculatePossibleMoves()
    }

    fun onPreviousLevelClicked() {}
    fun onNextLevelClicked() {}

    data class GameUiState(
        val level: Int = 1,
        val width: Int = 0,
        val height: Int = 0,
        val canUndo: Boolean = false,
        val canRedo: Boolean = false,
        val currentPosition: Position = Position(0, 0),
        val endingPosition: Position = Position(0, 0),
        val gridTiles: Map<Position, GridTile> = emptyMap(),
        val possibleMoves: Set<Direction> = emptySet(),
    ) {
        val currentTile: GridTile
            get() = gridTiles.getValue(currentPosition)
    }

    sealed interface Event {
        data object LevelFinished : Event
    }
}
