package com.Lia.assistant.ui.screens.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.EchoGuard
import com.Lia.assistant.voice.VoiceSessionManager

@Composable
fun DebugScreen(
    appState: NovaAppState,
    onBack: () -> Unit
) {
    val colors = LocalNovaColors.current
    val voiceState by VoiceSessionManager.state.collectAsState()
    val isMuted by VoiceSessionManager.muted.collectAsState()
    val vadActive by VoiceSessionManager.vadActive.collectAsState()
    val detectedLang by VoiceSessionManager.detectedLanguage.collectAsState()
    val reconnects by VoiceSessionManager.latencyTracker.reconnectCount.collectAsState()

    val tracker = VoiceSessionManager.latencyTracker

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
            .verticalScroll(rememberScrollState())
    ) {
        NovaTopBar(title = "Voice Pipeline Debugger", onBackClick = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("LIVE PIPELINE TELEMETRY", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    DebugRow("Session State", voiceState.name)
                    DebugRow("Configured Personality", appState.personality.displayName)
                    DebugRow("Configured Language", appState.language.displayName)
                    DebugRow("Detected Language", detectedLang.name)
                    DebugRow("VAD Speech Active", if (vadActive) "YES (Audio Peak)" else "NO")
                    DebugRow("Mic Muted", if (isMuted) "YES" else "NO")
                    DebugRow("EchoGuard Playback Mute", if (EchoGuard.isMicMuted()) "MUTED (Echo suppression)" else "UNMUTED")
                    DebugRow("Auto Reconnect Count", "$reconnects")
                }
            }

            NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("LATENCY & TIMINGS", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    DebugRow("Last Turn Duration", "${tracker.lastTurnDurationMs} ms")
                    DebugRow("Server First Audio Latency", "${tracker.lastServerLatencyMs} ms")
                    DebugRow("Last Tool Execution Time", "${tracker.lastToolExecutionMs} ms")
                    DebugRow("Conversation Memory Turns", "${VoiceSessionManager.memory.size()} / 12")
                }
            }
        }
    }
}

@Composable
private fun DebugRow(label: String, value: String) {
    val colors = LocalNovaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = colors.textSecondary, fontSize = 13.sp)
        Text(text = value, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
