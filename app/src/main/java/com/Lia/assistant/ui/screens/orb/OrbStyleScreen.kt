package com.Lia.assistant.ui.screens.orb

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.fx.NovaOrbStyle
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.NovaOrbState

@Composable
fun OrbStyleScreen(
    appState: NovaAppState,
    onBack: () -> Unit
) {
    val colors = LocalNovaColors.current
    var previewState by remember { mutableStateOf(NovaOrbState.IDLE) }

    val currentStyle = NovaOrbStyle.entries.firstOrNull { it.name == appState.orbStyle } ?: NovaOrbStyle.NOVA

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
    ) {
        NovaTopBar(title = "Orb Visual Presence", onBackClick = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        // State toggle chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val states = listOf(NovaOrbState.IDLE, NovaOrbState.LISTENING, NovaOrbState.SPEAKING)
            for (st in states) {
                val isSelected = previewState == st
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .background(
                            if (isSelected) colors.accent else colors.surfaceGlass,
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) colors.accent else colors.surfaceBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { previewState = st }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = st.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (isSelected) colors.onAccent else colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Large live orb preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            LiaOrb3D(
                state = previewState,
                amplitude = if (previewState == NovaOrbState.SPEAKING) 0.6f else 0.1f,
                size = 220.dp,
                style = currentStyle,
                assistantName = appState.assistantName
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "SELECT ORB STYLE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Carousel of 7 styles
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(NovaOrbStyle.entries) { style ->
                val isSelected = style == currentStyle
                NovaGlassCard(
                    modifier = Modifier.width(200.dp),
                    onClick = { appState.applyOrbStyle(style.name) }
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = style.displayName,
                                color = if (isSelected) colors.accent else colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSelected) {
                                Text(
                                    text = "Active",
                                    fontSize = 11.sp,
                                    color = colors.accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = style.description,
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
