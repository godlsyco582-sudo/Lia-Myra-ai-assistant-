package com.Lia.assistant.ui.screens.personality

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.data.Personality
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky

@Composable
fun PersonalityScreen(
    appState: NovaAppState,
    onBack: () -> Unit
) {
    val colors = LocalNovaColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
    ) {
        NovaTopBar(title = "Personality Engine", onBackClick = onBack)

        // Notice banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(colors.surfaceGlass, RoundedCornerShape(12.dp))
                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "⚡ Changes take effect immediately in voice and chat sessions.",
                color = colors.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(Personality.entries) { item ->
                val isSelected = appState.personality == item
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.04f else 1f,
                    label = "pScale"
                )

                val cardShape = RoundedCornerShape(18.dp)
                val borderCol = if (isSelected) colors.accent else colors.surfaceBorder
                val bgCol = if (isSelected) colors.surfaceRaised else colors.surfaceGlass

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .shadow(if (isSelected) 12.dp else 4.dp, cardShape, spotColor = if (isSelected) colors.accentGlow else colors.depthShadow)
                        .background(bgCol, cardShape)
                        .border(if (isSelected) 2.dp else 1.dp, borderCol, cardShape)
                        .clickable { appState.applyPersonality(item) }
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (isSelected) colors.accent.copy(alpha = 0.2f) else colors.surfaceRaised, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) colors.accent else colors.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(colors.accent, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = item.displayName,
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.description,
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
