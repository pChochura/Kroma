package com.pointlessgames.agame.ui

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
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
            launch { animatedBackgroundColor.animateTo(pressedBackgroundColor, spring()) }
            launch { animatedShapeProgress.animateTo(1f, spring()) }
        }
    }

    private suspend fun animateToResting() {
        coroutineScope {
            launch { animatedBackgroundColor.animateTo(defaultBackgroundColor, spring()) }
            launch { animatedShapeProgress.animateTo(0f, spring()) }
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
