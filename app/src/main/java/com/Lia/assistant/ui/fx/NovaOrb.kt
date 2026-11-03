package com.Lia.assistant.ui.fx

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.LocalNovaReducedMotion
import com.Lia.assistant.voice.NovaOrbState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class NovaOrbStyle(val displayName: String, val description: String) {
    NOVA("Nova", "Classic celestial orb with orbital particles and luminous aura"),
    AURORA("Aurora", "Flowing northern lights ribbon gradients"),
    PLASMA("Plasma", "High-energy electric ionization plasma filaments"),
    GLASS("Glass", "Specular refractive crystal marble with rim reflections"),
    ENERGY("Energy", "Surging dual vortex of radiant hyper-particles"),
    MINIMAL("Minimal", "Clean geometric glow ring and subtle center pulse"),
    ARCHER("Archer", "3D golden-spiral spherical particle field")
}

@Composable
fun NovaOrb(
    state: NovaOrbState,
    amplitude: Float,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    style: NovaOrbStyle = NovaOrbStyle.NOVA,
    assistantName: String = "Lia"
) {
    val colors = LocalNovaColors.current
    val reducedMotion = LocalNovaReducedMotion.current

    val transition = rememberInfiniteTransition(label = "orbMotion")

    val breatheAnim by transition.animateFloat(
        initialValue = -0.035f,
        targetValue = 0.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    val rotDuration = when (state) {
        NovaOrbState.CONNECTING -> 1100
        NovaOrbState.THINKING -> 2600
        else -> 9000
    }

    val rotationAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(rotDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot"
    )

    val pulseRingAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRing"
    )

    val (coreColor, glowColor, rimColor) = when (state) {
        NovaOrbState.IDLE -> Triple(colors.surfaceRaised, colors.lotus.copy(alpha = 0.5f), colors.lotus)
        NovaOrbState.LISTENING -> Triple(colors.surfaceRaised, colors.lagoon.copy(alpha = 0.6f), colors.lagoon)
        NovaOrbState.THINKING -> Triple(colors.accentSecondary, colors.accent.copy(alpha = 0.7f), colors.accent)
        NovaOrbState.SPEAKING -> Triple(colors.accent, colors.lotus.copy(alpha = 0.8f), colors.accentSecondary)
        NovaOrbState.CONNECTING -> Triple(colors.warning, colors.warning.copy(alpha = 0.5f), colors.warning)
        NovaOrbState.ERROR -> Triple(colors.error, colors.error.copy(alpha = 0.4f), colors.error)
    }

    val effectiveBreathe = if (reducedMotion) 0f else breatheAnim
    val effectiveRotation = if (reducedMotion) 0f else rotationAnim

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "$assistantName status: ${state.name}" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2.6f) * (1f + effectiveBreathe + (amplitude * 0.25f))

            // 1. Radial outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.8f
                ),
                radius = baseRadius * 1.8f,
                center = center
            )

            // 2. Expanding listening rings
            if (state == NovaOrbState.LISTENING && !reducedMotion) {
                drawCircle(
                    color = rimColor.copy(alpha = (1f - pulseRingAnim) * 0.5f),
                    radius = baseRadius * (1f + pulseRingAnim * 0.6f),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 3. Archer golden spiral sphere
            if (style == NovaOrbStyle.ARCHER) {
                val pointCount = 300
                val phi = (1.0 + Math.sqrt(5.0)) / 2.0 // golden ratio
                for (i in 0 until pointCount) {
                    val theta = 2.0 * PI * i / phi + Math.toRadians(effectiveRotation.toDouble())
                    val yNorm = 1.0 - (i.toDouble() / (pointCount - 1).toDouble()) * 2.0
                    val radiusAtY = Math.sqrt(1.0 - yNorm * yNorm)
                    val xNorm = Math.cos(theta) * radiusAtY
                    val zNorm = Math.sin(theta) * radiusAtY

                    val depthAlpha = ((zNorm + 1.0) / 2.0).toFloat().coerceIn(0.15f, 1f)
                    val pX = center.x + (xNorm * baseRadius).toFloat()
                    val pY = center.y + (yNorm * baseRadius).toFloat()
                    val pRadius = (1.5f + depthAlpha * 2.5f) * (1f + amplitude * 0.5f)

                    drawCircle(
                        color = rimColor.copy(alpha = depthAlpha * 0.85f),
                        radius = pRadius,
                        center = Offset(pX, pY)
                    )
                }
            } else {
                // Radial core gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.9f), coreColor, glowColor),
                        center = center,
                        radius = baseRadius
                    ),
                    radius = baseRadius,
                    center = center
                )

                // 4. Orbiting particles for styles
                val particleCount = when (style) {
                    NovaOrbStyle.ENERGY -> 8
                    NovaOrbStyle.PLASMA -> 6
                    NovaOrbStyle.GLASS -> 3
                    NovaOrbStyle.MINIMAL -> 0
                    else -> 5
                }

                if (particleCount > 0 && state != NovaOrbState.ERROR && !reducedMotion) {
                    val rotRad = Math.toRadians(effectiveRotation.toDouble()).toFloat()
                    for (i in 0 until particleCount) {
                        val angle = rotRad + (i * 2f * PI.toFloat() / particleCount)
                        val orbitDist = baseRadius * (1.15f + (i % 2) * 0.12f)
                        val pCenter = Offset(
                            center.x + orbitDist * cos(angle),
                            center.y + orbitDist * sin(angle)
                        )
                        drawCircle(
                            color = rimColor.copy(alpha = 0.85f),
                            radius = (3.5f + (i % 3)).dp.toPx(),
                            center = pCenter
                        )
                    }
                }

                // 5. Glass highlight arc
                if (style == NovaOrbStyle.GLASS) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.45f),
                        startAngle = 210f,
                        sweepAngle = 75f,
                        useCenter = false,
                        topLeft = Offset(center.x - baseRadius * 0.85f, center.y - baseRadius * 0.85f),
                        size = androidx.compose.ui.geometry.Size(baseRadius * 1.7f, baseRadius * 1.7f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            // Error pulse
            if (state == NovaOrbState.ERROR) {
                drawCircle(
                    color = colors.error.copy(alpha = 0.6f),
                    radius = baseRadius * 1.08f,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}
