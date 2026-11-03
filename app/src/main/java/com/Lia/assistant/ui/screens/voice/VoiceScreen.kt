package com.Lia.assistant.ui.screens.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaVoiceVisualizer
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.fx.ListeningEdgeGlow
import com.Lia.assistant.ui.fx.NovaOrbStyle
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.depthSurface
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.NovaOrbState
import com.Lia.assistant.voice.VoiceForegroundService
import com.Lia.assistant.voice.VoiceSessionManager

@Composable
fun VoiceScreen(
    appState: NovaAppState,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalNovaColors.current

    val voiceState by VoiceSessionManager.state.collectAsState()
    val amplitude by VoiceSessionManager.amplitude.collectAsState()
    val isMuted by VoiceSessionManager.muted.collectAsState()
    val statusText by VoiceSessionManager.statusText.collectAsState()
    val isActive by VoiceSessionManager.isActive.collectAsState()
    val lastError by VoiceSessionManager.lastError.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            VoiceForegroundService.start(context)
        }
    }

    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission && !isActive && ApiKeyStore.hasKey(context)) {
            VoiceForegroundService.start(context)
        }
    }

    val hasKey = ApiKeyStore.hasKey(context)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
    ) {
        ListeningEdgeGlow()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${appState.assistantName} Voice",
                    fontFamily = InstrumentSerifFamily,
                    fontSize = 24.sp,
                    color = colors.textPrimary
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(colors.surfaceGlass, CircleShape)
                        .border(1.dp, colors.surfaceBorder, CircleShape)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!hasKey) {
                NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Gemini API Key Required",
                            fontFamily = InstrumentSerifFamily,
                            fontSize = 22.sp,
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Please enter your Gemini API key in Settings to connect to real-time voice mode.",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NovaButton(
                            text = "Open Settings",
                            onClick = onOpenSettings,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else if (!hasAudioPermission) {
                NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Microphone Access Required",
                            fontFamily = InstrumentSerifFamily,
                            fontSize = 22.sp,
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Lia needs microphone permission to hear you in full duplex conversation.",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NovaButton(
                            text = "Grant Permission",
                            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Hero 3D Orb Stage with 48-bar visualizer ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(310.dp)
                ) {
                    if (voiceState == NovaOrbState.SPEAKING) {
                        NovaVoiceVisualizer(
                            amplitude = amplitude,
                            modifier = Modifier.size(310.dp)
                        )
                    }

                    LiaOrb3D(
                        state = voiceState,
                        amplitude = amplitude,
                        size = 240.dp,
                        style = NovaOrbStyle.entries.firstOrNull { it.name == appState.orbStyle } ?: NovaOrbStyle.NOVA,
                        assistantName = appState.assistantName
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Serif italic status text
                Text(
                    text = statusText,
                    fontFamily = InstrumentSerifFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 22.sp,
                    color = when (voiceState) {
                        NovaOrbState.ERROR -> colors.error
                        NovaOrbState.SPEAKING -> colors.accent
                        NovaOrbState.LISTENING -> colors.lagoon
                        else -> colors.textSecondary
                    }
                )

                if (lastError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lastError.orEmpty(),
                        color = colors.error,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Frosted Bottom Control Row: Mute, Stop (large red), Close
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .depthSurface(RoundedCornerShape(32.dp), elevation = 12.dp)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (isMuted) colors.error.copy(alpha = 0.2f) else colors.surfaceRaised, CircleShape)
                            .border(1.dp, if (isMuted) colors.error else colors.surfaceBorder, CircleShape)
                            .clickable { VoiceSessionManager.toggleMute() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = if (isMuted) colors.error else colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Stop button (large, red-tinted)
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(12.dp, CircleShape, spotColor = colors.error)
                            .background(Color(0xFFE53935), CircleShape)
                            .clickable {
                                VoiceForegroundService.stop(context)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Voice Session",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Close button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(colors.surfaceRaised, CircleShape)
                            .border(1.dp, colors.surfaceBorder, CircleShape)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close View",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
