package com.Lia.assistant.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.LocalNovaReducedMotion
import com.Lia.assistant.ui.theme.depthSurface

enum class NovaButtonType {
    PRIMARY,
    SECONDARY,
    TEXT
}

@Composable
fun NovaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: NovaButtonType = NovaButtonType.PRIMARY,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val colors = LocalNovaColors.current
    val reducedMotion = LocalNovaReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed) 0.96f else 1f,
        animationSpec = tween(150),
        label = "btnScale"
    )

    val shape = RoundedCornerShape(24.dp)
    val buttonModifier = when (type) {
        NovaButtonType.PRIMARY -> {
            modifier
                .scale(scale)
                .defaultMinSize(minHeight = 48.dp, minWidth = 88.dp)
                .shadow(
                    elevation = if (enabled) 6.dp else 0.dp,
                    shape = shape,
                    ambientColor = colors.depthShadow,
                    spotColor = colors.accentGlow
                )
                .background(
                    brush = if (enabled) {
                        Brush.horizontalGradient(listOf(colors.accent, colors.accentSecondary))
                    } else {
                        Brush.horizontalGradient(listOf(colors.surfaceRaised, colors.surfaceRaised))
                    },
                    shape = shape
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        }
        NovaButtonType.SECONDARY -> {
            modifier
                .scale(scale)
                .defaultMinSize(minHeight = 48.dp, minWidth = 88.dp)
                .depthSurface(shape, elevation = 2.dp)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        }
        NovaButtonType.TEXT -> {
            modifier
                .scale(scale)
                .defaultMinSize(minHeight = 48.dp)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        }
    }

    val textColor = when (type) {
        NovaButtonType.PRIMARY -> if (enabled) colors.onAccent else colors.textTertiary
        NovaButtonType.SECONDARY -> if (enabled) colors.textPrimary else colors.textTertiary
        NovaButtonType.TEXT -> if (enabled) colors.accent else colors.textTertiary
    }

    Box(
        modifier = buttonModifier.padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
