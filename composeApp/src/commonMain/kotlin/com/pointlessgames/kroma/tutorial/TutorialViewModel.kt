package com.pointlessgames.kroma.tutorial

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.kroma.Game
import com.pointlessgames.kroma.Solver
import com.pointlessgames.kroma.data.SettingsRepository
import com.pointlessgames.kroma.model.Direction
import com.pointlessgames.kroma.model.LevelData
import com.pointlessgames.kroma.model.UndoState
import com.pointlessgames.kroma.tutorial.utils.levels
import com.pointlessgames.kroma.utils.UndoManager
import com.pointlessgames.kroma.utils.toDegrees
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kroma.composeapp.generated.resources.Res
import kroma.composeapp.generated.resources.tutorial_1
import kroma.composeapp.generated.resources.tutorial_2
import kroma.composeapp.generated.resources.tutorial_3
import kroma.composeapp.generated.resources.tutorial_4
import org.jetbrains.compose.resources.StringResource
import kotlin.math.atan2
import kotlin.time.Duration.Companion.seconds

internal class TutorialViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val undoManager = UndoManager<UndoState>()

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(
        UiState(
            levels = levels,
            level = 0,
            levelData = levels.first(),
        ),
    )
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()

    private var swipeAngle = 0.0

    private var currentAsyncJob: Job? = null
    private var needHintJob: Job? = null

    fun loadLevels() {
        _uiState.update {
            it.copy(
                levels = levels,
                level = 0,
                levelData = levels.first(),
            )
        }
        updateState(uiState.value.levelData, updateTutorial = true)
    }

    private fun updateState(levelData: LevelData, updateTutorial: Boolean) {
        _uiState.update {
            it.copy(
                levelData = levelData,
                possibleMoves = Game.getPossibleMoves(levelData),
                canUndo = undoManager.canUndo(),
                canRedo = undoManager.canRedo(),
                canRestart = undoManager.canUndo(),

                canMovePreviousLevel = it.level > 0,

                tutorialDescription = when (it.level) {
                    0 -> Res.string.tutorial_1
                    1 -> Res.string.tutorial_2
                    2 -> Res.string.tutorial_3
                    3 -> Res.string.tutorial_4
                    else -> null
                }.takeIf { updateTutorial },
            )
        }

        currentAsyncJob?.cancel()
        currentAsyncJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    hasHints = !Solver.getBestMoveSequence(levelData).isNullOrEmpty(),
                    isLoadingHint = false,
                )
            }
        }

        needHintJob?.cancel()
        needHintJob = viewModelScope.launch {
            delay(5.seconds)
            _uiState.update { it.copy(showNeedHintPopup = it.hasHints) }
        }
    }

    private fun loadNextLevel() {
        val levelData = uiState.value.levels[uiState.value.level + 1]

        Solver.clearCache()
        undoManager.clear()
        _uiState.update {
            it.copy(
                level = it.level + 1,
                animationMovesForward = true,
            )
        }

        updateState(levelData, updateTutorial = true)
    }

    private fun loadPreviousLevel() {
        val levelData = uiState.value.levels[uiState.value.level - 1]

        Solver.clearCache()
        undoManager.clear()
        _uiState.update {
            it.copy(
                level = it.level - 1,
                animationMovesForward = false,
            )
        }

        updateState(levelData, updateTutorial = true)
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

        if (!uiState.value.canMove(moveDirection)) return

        undoManager.insertState(
            UndoState(
                currentPosition = uiState.value.levelData.currentPosition,
                gridTiles = uiState.value.levelData.tiles.toMap(),
            ),
        )

        updateState(
            levelData = Game.performMove(
                levelData = uiState.value.levelData,
                moveDirection = moveDirection,
            ),
            updateTutorial = false,
        )
    }

    fun onAnimationsFinished() {
        if (Game.isFinished(uiState.value.levelData)) {
            if (uiState.value.level == uiState.value.levels.lastIndex) {
                viewModelScope.launch {
                    settingsRepository.setTutorialFinished()
                    eventChannel.send(Event.GoBack)
                }

                return
            }

            loadNextLevel()
        }
    }

    fun onUndoClicked() {
        if (!undoManager.canRedo()) {
            undoManager.insertState(
                UndoState(
                    currentPosition = uiState.value.levelData.currentPosition,
                    gridTiles = uiState.value.levelData.tiles.toMap(),
                ),
            )
        }

        val state = undoManager.undo()
        updateState(
            levelData = uiState.value.levelData.copy(
                currentPosition = state.currentPosition,
                tiles = state.gridTiles,
            ),
            updateTutorial = !undoManager.canUndo(),
        )
    }

    fun onRedoClicked() {
        val state = undoManager.redo()
        updateState(
            levelData = uiState.value.levelData.copy(
                currentPosition = state.currentPosition,
                tiles = state.gridTiles,
            ),
            updateTutorial = false,
        )
    }

    fun onRestartClicked() {
        val state = undoManager.clear() ?: return
        updateState(
            levelData = uiState.value.levelData.copy(
                currentPosition = state.currentPosition,
                tiles = state.gridTiles,
            ),
            updateTutorial = true,
        )
    }

    fun onHintClicked() {
        if (!uiState.value.hasHints) {
            _uiState.update { it.copy(showNoHintsPopup = true) }

            return
        }

        _uiState.update { it.copy(isLoadingHint = true) }
        viewModelScope.launch {
            val nextMove = requireNotNull(Solver.getBestNextMove(uiState.value.levelData)) {
                "Could not find a solution for this level."
            }

            undoManager.insertState(
                UndoState(
                    currentPosition = uiState.value.levelData.currentPosition,
                    gridTiles = uiState.value.levelData.tiles.toMap(),
                ),
            )

            updateState(
                levelData = Game.performMove(
                    levelData = uiState.value.levelData,
                    moveDirection = nextMove,
                ),
                updateTutorial = false,
            )
        }
    }

    fun onPopupDismissed() {
        _uiState.update {
            it.copy(
                showNoHintsPopup = false,
                showNeedHintPopup = false,
            )
        }
    }

    fun onBackClicked() {
        eventChannel.trySend(Event.GoBack)
    }

    fun onPreviousLevelClicked() {
        loadPreviousLevel()
    }

    override fun onCleared() {
        currentAsyncJob?.cancel()
        needHintJob?.cancel()
        super.onCleared()
    }

    data class UiState(
        val levels: List<LevelData>,
        val level: Int,
        val levelData: LevelData,

        val tutorialDescription: StringResource? = null,

        val animationMovesForward: Boolean = true,

        val canUndo: Boolean = false,
        val canRedo: Boolean = false,
        val canRestart: Boolean = false,

        val canHint: Boolean = true,
        val hasHints: Boolean = false,
        val isLoadingHint: Boolean = false,
        val showNoHintsPopup: Boolean = false,
        val showNeedHintPopup: Boolean = false,

        val canMovePreviousLevel: Boolean = false,

        val possibleMoves: Set<Direction> = emptySet(),
    ) {
        fun canMove(direction: Direction): Boolean = possibleMoves.contains(direction)
    }

    sealed interface Event {
        data object GoBack : Event
    }
}
