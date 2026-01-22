package com.pointlessgames.kroma.levelCreator.ui

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.PopupPositionProvider
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.kroma.LocalNavigator
import com.pointlessgames.kroma.levelCreator.LevelCreatorViewModel
import com.pointlessgames.kroma.levelCreator.LevelCreatorViewModel.Event.GoBack
import com.pointlessgames.kroma.levelCreator.LevelCreatorViewModel.Event.TestLevel
import com.pointlessgames.kroma.ui.LocalInnerPadding
import com.pointlessgames.kroma.ui.TiltedRoundedCornersShape
import com.pointlessgames.kroma.ui.components.Button
import com.pointlessgames.kroma.ui.components.Counter
import com.pointlessgames.kroma.ui.components.Dialog
import com.pointlessgames.kroma.ui.components.GameGrid
import com.pointlessgames.kroma.ui.components.IconButton
import com.pointlessgames.kroma.ui.components.Position
import com.pointlessgames.kroma.ui.components.ShapeButton
import com.pointlessgames.kroma.ui.theme.DefaultCornerRadius
import com.pointlessgames.kroma.ui.theme.DefaultIconsSize
import com.pointlessgames.kroma.ui.theme.DefaultSpacing
import kotlinx.coroutines.launch
import kroma.composeapp.generated.resources.Res
import kroma.composeapp.generated.resources.cancel
import kroma.composeapp.generated.resources.end_of_the_level
import kroma.composeapp.generated.resources.generate
import kroma.composeapp.generated.resources.go_back
import kroma.composeapp.generated.resources.height
import kroma.composeapp.generated.resources.icon_arrow_left
import kroma.composeapp.generated.resources.icon_dot
import kroma.composeapp.generated.resources.icon_magic
import kroma.composeapp.generated.resources.icon_play
import kroma.composeapp.generated.resources.icon_resize
import kroma.composeapp.generated.resources.icon_restart
import kroma.composeapp.generated.resources.icon_save
import kroma.composeapp.generated.resources.reset
import kroma.composeapp.generated.resources.resize
import kroma.composeapp.generated.resources.save
import kroma.composeapp.generated.resources.save_the_level
import kroma.composeapp.generated.resources.start_game
import kroma.composeapp.generated.resources.start_of_the_level
import kroma.composeapp.generated.resources.width
import org.jetbrains.compose.resources.stringResource
import com.pointlessgames.kroma.model.Position as GridTilePosition

@Composable
internal fun LevelCreatorScreen(viewModel: LevelCreatorViewModel) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        coroutineScope.launch {
            viewModel.events.collect {
                when (it) {
                    is TestLevel -> navigator.navigateToTestLevel(it.levelData)
                    is GoBack -> navigator.navigateBack()
                }
            }
        }

        onPauseOrDispose { }
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(LocalInnerPadding.current),
    ) {
        val (topBar, board, bottomBar) = createRefs()

        createVerticalChain(
            topBar,
            board.withVerticalChainParams(weight = 1f),
            bottomBar,
            chainStyle = ChainStyle.SpreadInside,
        )

        TopBar(
            modifier = Modifier
                .constrainAs(topBar) {
                    centerHorizontallyTo(parent)
                },
            onBackClicked = viewModel::onBackClicked,
            onSaveLevelClicked = viewModel::onSaveLevelClicked,
        )

        Board(
            modifier = Modifier
                .constrainAs(board) {
                    centerHorizontallyTo(parent)
                    this.height = Dimension.fillToConstraints
                    this.width = Dimension.matchParent
                },
            uiState = uiState,
            onGenerateLevelClicked = viewModel::onGenerateLevelClicked,
            onStartChanged = viewModel::onStartChanged,
            onEndChanged = viewModel::onEndChanged,
            onTileClicked = viewModel::onTileClicked,
        )

        BottomBar(
            modifier = Modifier
                .constrainAs(bottomBar) {
                    centerHorizontallyTo(parent)
                },
            width = uiState.width,
            height = uiState.height,
            onTestLevelClicked = viewModel::onTestLevelClicked,
            onResetClicked = viewModel::onResetClicked,
            onSizeChanged = viewModel::onSizeChanged,
        )
    }
}

@Composable
private fun rememberBoardPopupPositionProvider(
    width: Int,
    height: Int,
    maxSize: DpSize,
    tooltipPosition: GridTilePosition?,
): PopupPositionProvider {
    val density = LocalDensity.current
    val spacing = DefaultSpacing.current

    val width by rememberUpdatedState(width)
    val height by rememberUpdatedState(height)
    val maxSize by rememberUpdatedState(maxSize)
    val tooltipPosition by rememberUpdatedState(tooltipPosition)

    return remember {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val position = tooltipPosition ?: return IntOffset.Zero

                val tileGap = spacing.extraSmall
                val tileSizeByWidth = (maxSize.width - (width - 1) * tileGap) / width
                val tileSizeByHeight = (maxSize.height - (height - 1) * tileGap) / height
                val tileSize = minOf(tileSizeByWidth, tileSizeByHeight)

                val x = with(density) { (position.x * (tileSize + tileGap)).toPx() }
                val y = with(density) { (position.y * (tileSize + tileGap)).toPx() }

                val offsetX = (popupContentSize.width - with(density) { tileSize.toPx() }) / 2

                return IntOffset(
                    x = (anchorBounds.left + x - offsetX).toInt(),
                    y = (anchorBounds.top + y - popupContentSize.height).toInt(),
                )
            }
        }
    }
}

@Composable
private fun BoardTooltip(
    onStartChanged: () -> Unit,
    onEndChanged: () -> Unit,
) {
    val spacing = DefaultSpacing.current

    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(spacing.extraSmall),
        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            isEnabled = true,
            iconRes = Res.drawable.icon_dot,
            contentDescription = Res.string.start_of_the_level,
            tint = Color(236, 218, 182),
            onClick = onStartChanged,
        )
        IconButton(
            isEnabled = true,
            iconRes = Res.drawable.icon_dot,
            contentDescription = Res.string.end_of_the_level,
            tint = MaterialTheme.colorScheme.background,
            onClick = onEndChanged,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Board(
    uiState: LevelCreatorViewModel.UiState,
    onGenerateLevelClicked: () -> Unit,
    onStartChanged: (GridTilePosition?) -> Unit,
    onEndChanged: (GridTilePosition?) -> Unit,
    onTileClicked: (GridTilePosition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = DefaultSpacing.current

    val coroutineScope = rememberCoroutineScope()
    var currentTooltipPosition by remember { mutableStateOf<GridTilePosition?>(null) }
    val tooltipState = rememberBasicTooltipState(isPersistent = true)

    Column(
        modifier = modifier
            .padding(spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            icon = Res.drawable.icon_magic,
            text = stringResource(Res.string.generate),
            defaultShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.small),
            pressedShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.medium),
            defaultBackgroundColor = MaterialTheme.colorScheme.tertiary,
            pressedBackgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            onClick = onGenerateLevelClicked,
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val maxSize = DpSize(width = maxWidth, height = maxHeight)

            BasicTooltipBox(
                positionProvider = rememberBoardPopupPositionProvider(
                    width = uiState.width,
                    height = uiState.height,
                    maxSize = maxSize,
                    tooltipPosition = currentTooltipPosition,
                ),
                tooltip = @Composable {
                    BoardTooltip(
                        onStartChanged = {
                            onStartChanged(currentTooltipPosition)
                            tooltipState.dismiss()
                        },
                        onEndChanged = {
                            onEndChanged(currentTooltipPosition)
                            tooltipState.dismiss()
                        },
                    )
                },
                state = tooltipState,
                focusable = true,
                enableUserInput = false,
            ) {
                GameGrid(
                    width = uiState.width,
                    height = uiState.height,
                    maxSize = maxSize,
                    currentPosition = uiState.startingPosition,
                    endingPosition = uiState.endingPosition,
                    possibleMoves = emptySet(),
                    tiles = uiState.gridTiles,
                    onTileClicked = onTileClicked,
                    onTileLongClicked = {
                        currentTooltipPosition = it
                        coroutineScope.launch { tooltipState.show() }
                    },
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    onBackClicked: () -> Unit,
    onSaveLevelClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
            onClick = onBackClicked,
        )
        ShapeButton(
            size = DefaultIconsSize.current.large,
            iconSize = DefaultIconsSize.current.small,
            icon = Res.drawable.icon_save,
            contentDescription = Res.string.save_the_level,
            defaultShape = TiltedRoundedCornersShape(-45f, DefaultCornerRadius.current.medium),
            pressedShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.medium),
            defaultBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            pressedBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            tooltipPosition = Position.BELOW,
            onClick = onSaveLevelClicked,
        )
    }
}

@Composable
private fun BottomBar(
    width: Int,
    height: Int,
    onTestLevelClicked: () -> Unit,
    onResetClicked: () -> Unit,
    onSizeChanged: (width: Int, height: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = DefaultSpacing.current
    var showSizeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        ShapeButton(
            size = DefaultIconsSize.current.extraLarge,
            iconSize = DefaultIconsSize.current.medium,
            icon = Res.drawable.icon_play,
            contentDescription = Res.string.start_game,
            defaultShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.medium),
            pressedShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.full),
            defaultBackgroundColor = MaterialTheme.colorScheme.primary,
            pressedBackgroundColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onTestLevelClicked,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Button(
                icon = Res.drawable.icon_restart,
                text = stringResource(Res.string.reset),
                defaultShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.small),
                pressedShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.medium),
                defaultBackgroundColor = MaterialTheme.colorScheme.secondary,
                pressedBackgroundColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                onClick = onResetClicked,
            )
            Button(
                icon = Res.drawable.icon_resize,
                text = "${width}x${height}",
                defaultShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.small),
                pressedShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.medium),
                defaultBackgroundColor = MaterialTheme.colorScheme.secondary,
                pressedBackgroundColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                onClick = { showSizeDialog = true },
            )
        }
    }

    if (showSizeDialog) {
        var currentWidth by remember { mutableIntStateOf(width) }
        var currentHeight by remember { mutableIntStateOf(height) }

        Dialog(
            icon = Res.drawable.icon_resize,
            title = Res.string.resize,
            primaryButtonText = Res.string.save,
            secondaryButtonText = Res.string.cancel,
            onPrimaryButtonClick = {
                onSizeChanged(currentWidth, currentHeight)
                showSizeDialog = false
            },
            onSecondaryButtonClick = { showSizeDialog = false },
            onDismissRequest = { showSizeDialog = false },
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.width),
                    style = MaterialTheme.typography.labelMedium,
                )

                Counter(
                    count = currentWidth,
                    minValue = 3,
                    maxValue = 12,
                    onIncrement = { currentWidth++ },
                    onDecrement = { currentWidth-- },
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.height),
                    style = MaterialTheme.typography.labelMedium,
                )

                Counter(
                    count = currentHeight,
                    minValue = 3,
                    maxValue = 12,
                    onIncrement = { currentHeight++ },
                    onDecrement = { currentHeight-- },
                )
            }
        }
    }
}
