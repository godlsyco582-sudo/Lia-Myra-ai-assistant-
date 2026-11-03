package com.Lia.assistant.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalBottomBarInset = compositionLocalOf { PaddingValues(bottom = 0.dp) }

fun Modifier.nightSky(): Modifier = composed {
    val colors = LocalNovaColors.current
    val brush = Brush.verticalGradient(
        colors = listOf(
            colors.backgroundGradientTop,
            colors.background
        )
    )
    this
        .background(brush)
        .drawBehind {
            // Draw subtle fixed stars
            val starPositions = listOf(
                Offset(size.width * 0.15f, size.height * 0.12f),
                Offset(size.width * 0.82f, size.height * 0.08f),
                Offset(size.width * 0.45f, size.height * 0.22f),
                Offset(size.width * 0.88f, size.height * 0.35f),
                Offset(size.width * 0.12f, size.height * 0.42f),
                Offset(size.width * 0.70f, size.height * 0.65f),
                Offset(size.width * 0.25f, size.height * 0.80f)
            )
            for (pos in starPositions) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = 1.5.dp.toPx(),
                    center = pos
                )
            }
        }
}

fun Modifier.depthSurface(
    shape: Shape,
    elevation: Dp = 8.dp
): Modifier = composed {
    val colors = LocalNovaColors.current
    this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = colors.depthShadow,
            spotColor = colors.accentGlow
        )
        .background(colors.surfaceGlass, shape)
        .border(1.dp, colors.surfaceBorder, shape)
}
