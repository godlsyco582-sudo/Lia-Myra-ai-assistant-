package com.Lia.assistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.Lia.assistant.ui.theme.LocalNovaColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NovaVoiceVisualizer(
    amplitude: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 48,
    color: Color = LocalNovaColors.current.accent
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2.3f
        val center = Offset(size.width / 2f, size.height / 2f)

        for (i in 0 until barCount) {
            val angle = (i.toFloat() / barCount.toFloat()) * 2f * Math.PI.toFloat()
            val heightVariation = (sin(angle * 4f) * 0.4f + 0.6f) * amplitude * 40.dp.toPx()

            val startX = center.x + radius * cos(angle)
            val startY = center.y + radius * sin(angle)

            val endX = center.x + (radius + heightVariation) * cos(angle)
            val endY = center.y + (radius + heightVariation) * sin(angle)

            drawLine(
                color = color.copy(alpha = (0.3f + amplitude * 0.7f).coerceIn(0f, 1f)),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.5.dp.toPx()
            )
        }
    }
}

@Composable
fun NovaTypingDots(
    modifier: Modifier = Modifier,
    dotSize: Dp = 6.dp
) {
    val colors = LocalNovaColors.current
    val transition = rememberInfiniteTransition(label = "dots")

    val dot1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "d1"
    )
    val dot2 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "d2"
    )
    val dot3 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "d3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(dotSize)) {
            drawCircle(color = colors.accent.copy(alpha = dot1))
        }
        Canvas(modifier = Modifier.size(dotSize)) {
            drawCircle(color = colors.accent.copy(alpha = dot2))
        }
        Canvas(modifier = Modifier.size(dotSize)) {
            drawCircle(color = colors.accent.copy(alpha = dot3))
        }
    }
}
