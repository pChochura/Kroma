package com.pointlessgames.kroma.tutorial.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.pointlessgames.kroma.LocalNavigator
import com.pointlessgames.kroma.game.ui.GameBoard
import com.pointlessgames.kroma.tutorial.TutorialViewModel
import com.pointlessgames.kroma.tutorial.TutorialViewModel.Event.GoBack
import com.pointlessgames.kroma.ui.LocalInnerPadding
import com.pointlessgames.kroma.ui.TiltedRoundedCornersShape
import com.pointlessgames.kroma.ui.components.IconButton
import com.pointlessgames.kroma.ui.components.Position
import com.pointlessgames.kroma.ui.components.ShapeButton
import com.pointlessgames.kroma.ui.components.Tooltip
import com.pointlessgames.kroma.ui.theme.DefaultCornerRadius
import com.pointlessgames.kroma.ui.theme.DefaultIconsSize
import com.pointlessgames.kroma.ui.theme.DefaultSpacing
import kotlinx.coroutines.launch
import kroma.shared.generated.resources.Res
import kroma.shared.generated.resources.go_back
import kroma.shared.generated.resources.icon_arrow_left
import kroma.shared.generated.resources.icon_lightbulb
import kroma.shared.generated.resources.icon_redo
import kroma.shared.generated.resources.icon_restart
import kroma.shared.generated.resources.icon_undo
import kroma.shared.generated.resources.need_a_hint
import kroma.shared.generated.resources.no_hints_available
import kroma.shared.generated.resources.redo_previous_move
import kroma.shared.generated.resources.restart_the_level
import kroma.shared.generated.resources.show_a_hint
import kroma.shared.generated.resources.undo_last_move
import org.jetbrains.compose.resources.stringResource

private data object CurrentInfo : NavigationEventInfo()

@Composable
internal fun TutorialScreen(
    viewModel: TutorialViewModel,
) {
    val navigator = LocalNavigator.current

    val navigationEventState = rememberNavigationEventState(
        currentInfo = CurrentInfo,
        backInfo = listOf(NavigationEventInfo.None),
    )
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        coroutineScope.launch {
            viewModel.events.collect {
                when (it) {
                    is GoBack -> navigator.navigateBackFromTutorial()
                }
            }
        }

        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.loadLevels()
    }

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = uiState.canMovePreviousLevel,
        onBackCancelled = {},
        onBackCompleted = viewModel::onPreviousLevelClicked,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(LocalInnerPadding.current)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        viewModel.onDrag(dragAmount)
                    },
                    onDragEnd = viewModel::onDragEnd,
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = DefaultSpacing.current.extraLarge,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Box(modifier = Modifier.align(Alignment.Start)) {
            ShapeButton(
                size = DefaultIconsSize.current.large,
                iconSize = DefaultIconsSize.current.small,
                icon = Res.drawable.icon_arrow_left,
                contentDescription = Res.string.go_back,
                defaultShape = TiltedRoundedCornersShape(45f, DefaultCornerRadius.current.medium),
                pressedShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.medium),
                defaultBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                pressedBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                tooltipPosition = Position.BELOW,
                onClick = viewModel::onBackClicked,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    val transitionState = navigationEventState.transitionState
                    if (transitionState is NavigationEventTransitionState.InProgress) {
                        // TODO fix the snapping after release
                        val progress = transitionState.latestEvent.progress
                        translationX = progress * 300f
                        alpha = 1f - progress
                        scaleX = 1f - progress * 0.2f
                        scaleY = 1f - progress * 0.2f
                    }
                },
        ) {
            GameBoard(
                animationMovesForward = uiState.animationMovesForward,
                levelData = uiState.levelData,
                possibleMoves = uiState.possibleMoves,
                onAnimationsFinished = viewModel::onAnimationsFinished,
            )
        }

        AnimatedContent(
            modifier = Modifier.height(50.dp),
            targetState = uiState.tutorialDescription,
            transitionSpec = {
                fadeIn() + slideInVertically { it / 2 } togetherWith
                        fadeOut() + slideOutVertically { -it / 2 } using SizeTransform(false)
            },
            contentAlignment = Alignment.BottomCenter,
        ) { description ->
            if (description != null) {
                Text(
                    modifier = Modifier.padding(
                        horizontal = DefaultSpacing.current.extraLarge,
                    ),
                    text = stringResource(description),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
            }
        }

        BottomBar(
            canUndo = uiState.canUndo,
            canRedo = uiState.canRedo,
            canRestart = uiState.canRestart,
            canHint = uiState.canHint,
            isLoadingHint = uiState.isLoadingHint,
            showNoHintsPopup = uiState.showNoHintsPopup,
            showNeedHintPopup = uiState.showNeedHintPopup,
            onUndoClicked = viewModel::onUndoClicked,
            onRedoClicked = viewModel::onRedoClicked,
            onRestartClicked = viewModel::onRestartClicked,
            onHintClicked = viewModel::onHintClicked,
            onPopupDismissed = viewModel::onPopupDismissed,
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomBar(
    canUndo: Boolean,
    canRedo: Boolean,
    canRestart: Boolean,
    canHint: Boolean,
    isLoadingHint: Boolean,
    showNoHintsPopup: Boolean,
    showNeedHintPopup: Boolean,
    onUndoClicked: () -> Unit,
    onRedoClicked: () -> Unit,
    onRestartClicked: () -> Unit,
    onHintClicked: () -> Unit,
    onPopupDismissed: () -> Unit,
) {
    val spacing = DefaultSpacing.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = spacing.medium,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        IconButton(
            isPulsating = showNoHintsPopup,
            isEnabled = canUndo,
            iconRes = Res.drawable.icon_undo,
            contentDescription = Res.string.undo_last_move,
            onClick = onUndoClicked,
        )
        IconButton(
            isEnabled = canRedo,
            iconRes = Res.drawable.icon_redo,
            contentDescription = Res.string.redo_previous_move,
            onClick = onRedoClicked,
        )
        IconButton(
            isEnabled = canRestart,
            iconRes = Res.drawable.icon_restart,
            contentDescription = Res.string.restart_the_level,
            onClick = onRestartClicked,
        )

        val tooltipState = rememberBasicTooltipState(isPersistent = true)
        LaunchedEffect(showNoHintsPopup, showNeedHintPopup) {
            if (showNoHintsPopup || showNeedHintPopup) {
                tooltipState.show(MutatePriority.UserInput)
            } else {
                tooltipState.dismiss()
            }
        }
        LaunchedEffect(tooltipState.isVisible) {
            if (!tooltipState.isVisible) {
                onPopupDismissed()
            }
        }

        Tooltip(
            position = Position.ABOVE,
            contentDescription = when {
                showNoHintsPopup -> Res.string.no_hints_available
                showNeedHintPopup -> Res.string.need_a_hint
                else -> Res.string.show_a_hint
            },
            allowUserInput = false,
            state = tooltipState,
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                IconButton(
                    isLoading = isLoadingHint,
                    isEnabled = canHint,
                    iconRes = Res.drawable.icon_lightbulb,
                    contentDescription = Res.string.show_a_hint,
                    onClick = onHintClicked,
                )
            }
        }
    }
}
