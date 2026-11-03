package com.Lia.assistant.ui.fx

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.Lia.assistant.voice.NovaOrbState
import com.Lia.assistant.voice.VoiceSessionManager

@Composable
fun ListeningEdgeGlow(modifier: Modifier = Modifier) {
    val state by VoiceSessionManager.state.collectAsState()
    val isActive by VoiceSessionManager.isActive.collectAsState()

    val glowColor = when (state) {
        NovaOrbState.LISTENING -> Color(0xFF3FE0D0)
        NovaOrbState.SPEAKING -> Color(0xFFFF6FAE)
        NovaOrbState.THINKING -> Color(0xFFFFB547)
        NovaOrbState.ERROR -> Color(0xFFFF7A85)
        else -> Color(0xFF8B5CF6)
    }

    AnimatedVisibility(
        visible = isActive && state != NovaOrbState.IDLE,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .border(
                    width = 4.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.85f),
                            glowColor.copy(alpha = 0.2f),
                            glowColor.copy(alpha = 0.85f)
                        )
                    ),
                    shape = RectangleShape
                )
        )
    }
}
