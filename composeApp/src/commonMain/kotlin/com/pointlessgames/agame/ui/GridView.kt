package com.pointlessgames.agame.ui

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.times
import com.pointlessgames.agame.DefaultSpacing
import com.pointlessgames.agame.model.Direction
import com.pointlessgames.agame.model.Direction.BOTTOM
import com.pointlessgames.agame.model.Direction.LEFT
import com.pointlessgames.agame.model.Direction.RIGHT
import com.pointlessgames.agame.model.Direction.TOP
import com.pointlessgames.agame.model.GridTile
import com.pointlessgames.agame.model.Position
import com.pointlessgames.agame.utils.filledRoundedRect
import game.composeapp.generated.resources.Res
import game.composeapp.generated.resources.ic_arrow_bottom
import game.composeapp.generated.resources.ic_arrow_left
import game.composeapp.generated.resources.ic_arrow_right
import game.composeapp.generated.resources.ic_arrow_top
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private const val ANIMATION_DURATION = 300
private const val ANIMATION_OFFSET_DELAY = 50
private val EMPTY_CELLS_COLOR = Color(236, 218, 182)

@Composable
internal fun GameGrid(
    width: Int,
    height: Int,
    maxSize: DpSize,
    currentPosition: Position,
    endingPosition: Position,
    possibleMoves: Set<Direction>,
    tiles: Map<Position, GridTile>,
    onTileClicked: ((Position) -> Unit)? = null,
    onAnimationsFinished: (() -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val tileGap = DefaultSpacing.current.extraSmall
    val tileSizeByWidth = (maxSize.width - (width - 1) * tileGap) / width
    val tileSizeByHeight = (maxSize.height - (height - 1) * tileGap) / height
    val tileSize = minOf(tileSizeByWidth, tileSizeByHeight)

    var isAnimationRunning by remember { mutableStateOf(false) }
    var currentPossibleMoves by remember { mutableStateOf(emptySet<Direction>()) }
    val playerColor = Animatable(EMPTY_CELLS_COLOR)
    val animatedCurrentPosition by animateOffsetAsState(
        Offset(
            x = with(density) { (currentPosition.x * (tileSize + tileGap)).toPx() },
            y = with(density) { (currentPosition.y * (tileSize + tileGap)).toPx() },
        ),
        tween(ANIMATION_DURATION, delayMillis = ANIMATION_DURATION),
    ) {
        coroutineScope.launch {
            playerColor.animateTo(
                EMPTY_CELLS_COLOR.copy(alpha = if (possibleMoves.isNotEmpty()) 1f else 0f),
                tween(ANIMATION_DURATION),
            )
            currentPossibleMoves = possibleMoves
            isAnimationRunning = false
            onAnimationsFinished?.invoke()
        }
    }

    LaunchedEffect(currentPosition) {
        isAnimationRunning = true
    }

    LaunchedEffect(Unit) {
        currentPossibleMoves = possibleMoves
        isAnimationRunning = false
    }

    Box(
        modifier = Modifier.size(
            width = tileSize * width + (width - 1) * tileGap,
            height = tileSize * height + (height - 1) * tileGap,
        ),
    ) {
        for (row in 0 until height) {
            for (col in 0 until width) {
                val position = Position(col, row)
                GridTile(
                    modifier = Modifier.graphicsLayer {
                        translationX = with(density) { (col * (tileSize + tileGap)).toPx() }
                        translationY = with(density) { (row * (tileSize + tileGap)).toPx() }
                    },
                    size = tileSize,
                    gridTile = tiles[position] ?: GridTile.Empty,
                    isEndingPosition = endingPosition == position,
                    onClicked = onTileClicked?.let { { it(position) } },
                )
            }
        }

        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = animatedCurrentPosition.x
                    translationY = animatedCurrentPosition.y
                }
                .size(tileSize)
                .drawWithContent {
                    drawCircle(
                        color = playerColor.value,
                        radius = with(density) { tileSize.toPx() * 0.2f },
                    )
                    drawContent()
                },
        ) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = !isAnimationRunning,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (currentPossibleMoves.contains(LEFT))
                        Arrow(tileSize * 0.4f, Res.drawable.ic_arrow_left, LEFT)
                    if (currentPossibleMoves.contains(RIGHT))
                        Arrow(tileSize * 0.4f, Res.drawable.ic_arrow_right, RIGHT)
                    if (currentPossibleMoves.contains(TOP))
                        Arrow(tileSize * 0.4f, Res.drawable.ic_arrow_top, TOP)
                    if (currentPossibleMoves.contains(BOTTOM))
                        Arrow(tileSize * 0.4f, Res.drawable.ic_arrow_bottom, BOTTOM)
                }
            }
        }
    }
}

@Composable
private fun BoxScope.Arrow(
    size: Dp,
    res: DrawableResource,
    direction: Direction,
) {
    Icon(
        modifier = Modifier
            .size(size)
            .align(
                when (direction) {
                    LEFT -> Alignment.CenterStart
                    RIGHT -> Alignment.CenterEnd
                    TOP -> Alignment.TopCenter
                    BOTTOM -> Alignment.BottomCenter
                },
            ),
        painter = painterResource(res),
        contentDescription = null,
        tint = EMPTY_CELLS_COLOR,
    )
}

@Composable
private fun GridTile(
    size: Dp,
    gridTile: GridTile,
    isEndingPosition: Boolean,
    onClicked: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    var previousShowDirection by remember { mutableStateOf<Direction?>(null) }
    val colorAnimation = remember { Animatable(gridTile.color) }
    val rotationYAnimation = Animatable(0f)
    val rotationXAnimation = Animatable(0f)

    LaunchedEffect(gridTile) {
        if (gridTile.showFromDirection != null) {
            previousShowDirection = gridTile.showFromDirection
        } else {
            colorAnimation.snapTo(gridTile.color)
        }

        previousShowDirection?.let {
            animateShowByDirection(
                direction = it,
                animatableX = rotationXAnimation,
                animatableY = rotationYAnimation,
                animationOffset = gridTile.animationOffset,
                onChangeColor = {
                    coroutineScope.launch {
                        colorAnimation.animateTo(gridTile.color, tween(ANIMATION_DURATION))
                    }
                },
            )
        }
    }

    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotationYAnimation.value
                rotationX = rotationXAnimation.value
                cameraDistance = 10f
            }
            .size(size)
            .clip(shape)
            .clickable(
                enabled = onClicked != null,
                onClick = { onClicked?.invoke() },
                role = Role.Button,
            )
            .drawWithCache {
                val circlePath = Path().apply {
                    addOval(
                        Rect(
                            center = this@drawWithCache.size.center,
                            radius = size.toPx() * 0.2f,
                        ),
                    )
                }
                val roundedRectPath = Path().apply { addRoundRect(filledRoundedRect(shape)) }
                val combinedPath = Path.combine(
                    operation = PathOperation.Difference,
                    path1 = roundedRectPath,
                    path2 = circlePath,
                )

                onDrawBehind {
                    drawPath(
                        path = if (isEndingPosition) combinedPath else roundedRectPath,
                        color = colorAnimation.value,
                    )
                }
            },
    )
}

private suspend fun animateShowByDirection(
    direction: Direction,
    animatableX: Animatable<Float, AnimationVector1D>,
    animatableY: Animatable<Float, AnimationVector1D>,
    animationOffset: Int,
    onChangeColor: () -> Unit,
    duration: Int = ANIMATION_DURATION,
    offsetDelay: Int = ANIMATION_OFFSET_DELAY,
) = when (direction) {
    RIGHT, LEFT -> animatableY.animateShow(
        direction = if (direction == RIGHT) 1 else -1,
        animationOffset = animationOffset,
        onChangeColor = onChangeColor,
        duration = duration,
        offsetDelay = offsetDelay,
    )

    BOTTOM, TOP -> animatableX.animateShow(
        direction = if (direction == TOP) 1 else -1,
        animationOffset = animationOffset,
        onChangeColor = onChangeColor,
        duration = duration,
        offsetDelay = offsetDelay,
    )
}

private suspend fun Animatable<Float, AnimationVector1D>.animateShow(
    direction: Int,
    animationOffset: Int,
    onChangeColor: () -> Unit,
    duration: Int = ANIMATION_DURATION,
    offsetDelay: Int = ANIMATION_OFFSET_DELAY,
) {
    animateTo(
        targetValue = -60f * direction,
        animationSpec = tween(
            durationMillis = duration,
            delayMillis = animationOffset * offsetDelay,
            easing = EaseIn,
        ),
    )
    onChangeColor()
    snapTo(60f * direction)
    animateTo(
        targetValue = 0f,
        animationSpec = tween(
            durationMillis = duration,
            delayMillis = animationOffset * offsetDelay,
            easing = EaseOut,
        ),
    )
}
