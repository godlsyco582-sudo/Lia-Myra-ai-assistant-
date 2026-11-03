package com.Lia.assistant.ui.screens.forge

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.forge.ForgeStage
import com.Lia.assistant.forge.ForgeUiState
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaButtonType
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.depthSurface

@Composable
fun ForgeHud(
    state: ForgeUiState,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNovaColors.current
    val hudShape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Top prompt chip
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(colors.surface.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Forging: ${state.prompt.take(35)}${if (state.prompt.length > 35) "..." else ""}",
                color = colors.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom status card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .depthSurface(hudShape, elevation = 12.dp)
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PHASE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            letterSpacing = 1.sp
                        )
                        AnimatedContent(targetState = state.stage.label, label = "stageLabel") { lbl ->
                            Text(
                                text = lbl,
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = "${(state.progress * 100).toInt()}%",
                        fontFamily = InstrumentSerifFamily,
                        fontSize = 38.sp,
                        color = colors.accent,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6-segment stage track
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val stages = ForgeStage.entries.filter { it != ForgeStage.READY }
                    for ((idx, s) in stages.withIndex()) {
                        val isComplete = state.progress >= s.progress
                        val isCurrent = state.stage == s
                        val segColor = when {
                            isComplete -> colors.accent
                            isCurrent -> colors.lagoon
                            else -> colors.surfaceRaised
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .background(segColor, RoundedCornerShape(3.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Four stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(label = "SIZE", value = "${state.metrics.chars / 1024} KB")
                    StatItem(label = "SECTIONS", value = "${state.sections.size}")
                    StatItem(label = "RATE", value = "${state.metrics.tokensPerSec.toInt()} t/s")
                    StatItem(label = "ELAPSED", value = "${state.metrics.elapsedSec}s")
                }

                Spacer(modifier = Modifier.height(14.dp))

                NovaButton(
                    text = "Cancel Forge",
                    type = NovaButtonType.SECONDARY,
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val colors = LocalNovaColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = colors.textTertiary, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 13.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
    }
}
