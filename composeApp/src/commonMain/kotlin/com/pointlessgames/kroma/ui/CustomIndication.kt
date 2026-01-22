package com.pointlessgames.kroma.ui

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomIndicationNodeFactory(
    private val defaultShape: TiltedRoundedCornersShape,
    private val pressedShape: TiltedRoundedCornersShape,
    private val defaultBackgroundColor: Color,
    private val pressedBackgroundColor: Color,
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        CustomIndicationNode(
            defaultShape = defaultShape,
            pressedShape = pressedShape,
            defaultBackgroundColor = defaultBackgroundColor,
            pressedBackgroundColor = pressedBackgroundColor,
            interactionSource = interactionSource,
        )

    override fun hashCode(): Int = -1

    override fun equals(other: Any?) = other === this
}

private class CustomIndicationNode(
    private val defaultShape: TiltedRoundedCornersShape,
    private val pressedShape: TiltedRoundedCornersShape,
    private val defaultBackgroundColor: Color,
    private val pressedBackgroundColor: Color,
    private val interactionSource: InteractionSource,
) : Modifier.Node(), DrawModifierNode {

    private val path = Path()
    private val animatedBackgroundColor = Animatable(defaultBackgroundColor)
    private val animatedShapeProgress = Animatable(0f)

    private suspend fun animateToPressed() {
        coroutineScope {
            launch { animatedBackgroundColor.animateTo(pressedBackgroundColor) }
            launch { animatedShapeProgress.animateTo(1f) }
        }
    }

    private suspend fun animateToResting() {
        coroutineScope {
            launch { animatedBackgroundColor.animateTo(defaultBackgroundColor) }
            launch { animatedShapeProgress.animateTo(0f) }
        }
    }

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> animateToPressed()
                    is PressInteraction.Release -> animateToResting()
                    is PressInteraction.Cancel -> animateToResting()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        path.reset()
        path.addOutline(
            defaultShape.lerp(pressedShape, animatedShapeProgress.value).createOutline(
                size = size,
                layoutDirection = layoutDirection,
                density = this,
            ),
        )
        drawPath(path, color = animatedBackgroundColor.value)

        drawContent()
    }
}

private class DragIndicationNode(
    var isEnabled: Boolean,
    var dragForce: Float,
) : Modifier.Node(),
    PointerInputModifierNode,
    DrawModifierNode {

    private val animatedDragOffset = Animatable(Offset.Zero, Offset.VectorConverter)
    private val animatedScale = Animatable(1f)
    private var initialPointerPosition: Offset? = null

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (pass != PointerEventPass.Initial || !isEnabled) return

        pointerEvent.changes.firstOrNull()?.let {
            when (pointerEvent.type) {
                PointerEventType.Press -> {
                    initialPointerPosition = it.position
                    coroutineScope.launch {
                        launch { animatedDragOffset.stop() }
                        launch { animatedScale.animateTo(1.1f) }
                    }
                }

                PointerEventType.Move -> {
                    initialPointerPosition?.let { initialPos ->
                        val currentPos = it.position
                        val dragDelta = currentPos - initialPos
                        val targetOffset = dragDelta * dragForce
                        if (animatedDragOffset.value != targetOffset) {
                            coroutineScope.launch {
                                animatedDragOffset.snapTo(targetOffset)
                            }
                        }
                    }
                }

                PointerEventType.Release, PointerEventType.Exit -> {
                    initialPointerPosition = null
                    coroutineScope.launch {
                        launch { animatedDragOffset.animateTo(Offset.Zero) }
                        launch { animatedScale.animateTo(1f) }
                    }
                }

                else -> Unit
            }
        }
    }

    override fun onCancelPointerInput() {
        initialPointerPosition = null
        coroutineScope.launch {
            launch { animatedDragOffset.animateTo(Offset.Zero) }
            launch { animatedScale.animateTo(1f) }
        }
    }

    override fun ContentDrawScope.draw() {
        if (!isEnabled) {
            return drawContent()
        }

        scale(scale = animatedScale.value) {
            translate(
                left = animatedDragOffset.value.x,
                top = animatedDragOffset.value.y,
                block = { this@draw.drawContent() },
            )
        }
    }
}

private class DragIndicationElement(
    private val isEnabled: Boolean,
    private val dragForce: Float,
) : ModifierNodeElement<DragIndicationNode>() {
    override fun create() = DragIndicationNode(isEnabled, dragForce)
    override fun update(node: DragIndicationNode) {
        node.isEnabled = isEnabled
        node.dragForce = dragForce
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return true
    }

    override fun hashCode(): Int = this::class.hashCode()
}

internal fun Modifier.dragIndication(isEnabled: Boolean = true, dragForce: Float = 0.1f) =
    this then DragIndicationElement(isEnabled, dragForce)
