package com.Lia.assistant.ui.screens.forge

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.Lia.assistant.forge.ForgeStage
import com.Lia.assistant.ui.theme.LocalNovaColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ForgeFallbackScene(
    stage: ForgeStage,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colors = LocalNovaColors.current
    val transition = rememberInfiniteTransition(label = "forgeFallback")

    val rot by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.22f

        // Reactor aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors.accent.copy(alpha = 0.35f), Color.Transparent),
                center = center,
                radius = radius * 2.2f
            ),
            radius = radius * 2.2f,
            center = center
        )

        // Progress track arc
        drawArc(
            color = colors.surfaceBorder,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(center.x - radius * 1.3f, center.y - radius * 1.3f),
            size = Size(radius * 2.6f, radius * 2.6f),
            style = Stroke(width = 4.dp.toPx())
        )

        drawArc(
            color = colors.accent,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            topLeft = Offset(center.x - radius * 1.3f, center.y - radius * 1.3f),
            size = Size(radius * 2.6f, radius * 2.6f),
            style = Stroke(width = 4.dp.toPx())
        )

        // Core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, colors.accent, colors.lotus),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // Orbiting particles
        val count = 8
        val radOffset = Math.toRadians(rot.toDouble()).toFloat()
        for (i in 0 until count) {
            val angle = radOffset + (i * 2f * PI.toFloat() / count)
            val pDist = radius * 1.55f
            val pCenter = Offset(
                center.x + pDist * cos(angle),
                center.y + pDist * sin(angle)
            )
            drawCircle(
                color = if (i % 2 == 0) colors.lagoon else colors.accent,
                radius = 4.dp.toPx(),
                center = pCenter
            )
        }
    }
}
