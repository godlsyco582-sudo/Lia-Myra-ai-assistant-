package com.Lia.assistant.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.AccessibilitySettingsRow
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaSectionHeader
import com.Lia.assistant.ui.components.NovaSettingsRow
import com.Lia.assistant.ui.components.NovaTextField
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.LocalBottomBarInset
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky

@Composable
fun SettingsScreen(
    appState: NovaAppState,
    navController: NavController
) {
    val context = LocalContext.current
    val colors = LocalNovaColors.current
    val bottomInset = LocalBottomBarInset.current

    var currentApiKey by remember { mutableStateOf(ApiKeyStore.getKey(context)) }
    var keyVisible by remember { mutableStateOf(false) }
    var savedNotice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
            .verticalScroll(rememberScrollState())
            .padding(bottomInset)
    ) {
        NovaTopBar(title = "Settings")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section 1: Gemini API Key Card
            NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = colors.accent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Gemini API Configuration",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Enter your Google Gemini API key to activate voice mode and 3D website generation. Stored safely on this phone only.",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    NovaTextField(
                        value = if (keyVisible) currentApiKey else "•".repeat(currentApiKey.length.coerceAtMost(32)),
                        onValueChange = {
                            currentApiKey = it
                            savedNotice = false
                        },
                        placeholder = "AIzaSy...",
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = colors.textTertiary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (savedNotice) {
                            Text("✓ Key Saved", color = colors.lagoon, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        NovaButton(
                            text = "Save Key",
                            onClick = {
                                ApiKeyStore.saveKey(context, currentApiKey)
                                savedNotice = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: AI Identity & Personality
            NovaSectionHeader(title = "Assistant Identity & Brain")
            NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    NovaSettingsRow(
                        icon = Icons.Default.Person,
                        title = "Assistant Name",
                        subtitle = "Renamable assistant persona",
                        value = appState.assistantName,
                        onClick = {
                            // Quick prompt dialog or prompt
                            val names = listOf("Lia", "Nova", "Aria", "Maya", "Jarvis")
                            val nextIdx = (names.indexOf(appState.assistantName) + 1) % names.size
                            appState.applyAssistantName(names[nextIdx])
                        }
                    )

                    NovaSettingsRow(
                        icon = Icons.Default.Psychology,
                        title = "Personality Engine",
                        subtitle = appState.personality.description,
                        value = appState.personality.displayName,
                        onClick = { navController.navigate("personality") }
                    )

                    NovaSettingsRow(
                        icon = Icons.Default.Language,
                        title = "Language Mode",
                        subtitle = appState.language.description,
                        value = appState.language.displayName,
                        onClick = {
                            val all = LanguagePreference.entries
                            val next = all[(all.indexOf(appState.language) + 1) % all.size]
                            appState.applyLanguage(next)
                        }
                    )

                    NovaSettingsRow(
                        icon = Icons.Default.RecordVoiceOver,
                        title = "Live Voice Timbre",
                        subtitle = "Prebuilt voice profile",
                        value = appState.voiceName,
                        onClick = {
                            val voices = listOf("Aoede", "Puck", "Charon", "Kore", "Fenrir")
                            val next = voices[(voices.indexOf(appState.voiceName) + 1) % voices.size]
                            appState.applyVoiceName(next)
                        }
                    )

                    NovaSettingsRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Chat Model",
                        subtitle = "Fast text generation model",
                        value = appState.chatModel,
                        onClick = {
                            val models = listOf(
                                "gemini-3.8-flash",
                                "gemini-3.6-flash",
                                "gemini-3.5-flash",
                                "gemini-3.1-flash-lite-preview",
                                "gemini-flash-latest"
                            )
                            val next = models[(models.indexOf(appState.chatModel) + 1).coerceAtLeast(0) % models.size]
                            appState.applyChatModel(next)
                        }
                    )

                    NovaSettingsRow(
                        icon = Icons.Default.RecordVoiceOver,
                        title = "Live Voice Model",
                        subtitle = "Real-time bidirectional duplex audio",
                        value = appState.liveModel.removePrefix("models/"),
                        onClick = {
                            val liveModels = listOf(
                                "models/gemini-3.1-flash-live-preview",
                                "models/gemini-2.5-flash-native-audio-preview-12-2025",
                                "models/gemini-3.8-flash",
                                "models/gemini-2.0-flash-realtime-exp"
                            )
                            val next = liveModels[(liveModels.indexOf(appState.liveModel) + 1).coerceAtLeast(0) % liveModels.size]
                            appState.applyLiveModel(next)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Visuals & Motion
            NovaSectionHeader(title = "Visuals & 3D Stage")
            NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    NovaSettingsRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Orb Visual Style",
                        subtitle = "Customize the glowing 3D shader presence",
                        value = appState.orbStyle,
                        onClick = { navController.navigate("orb_style") }
                    )

                    NovaSettingsRow(
                        icon = Icons.Default.DarkMode,
                        title = "Theme Appearance",
                        subtitle = "Deep indigo night or crisp daylight",
                        value = appState.themeMode,
                        onClick = {
                            val next = if (appState.themeMode == "DARK") "LIGHT" else "DARK"
                            appState.applyThemeMode(next)
                        }
                    )

                    NovaSettingsRow(
                        icon = Icons.Default.MotionPhotosOff,
                        title = "Reduced Motion",
                        subtitle = "Disables gyro parallax and 3D tilts",
                        switchChecked = appState.reducedMotion,
                        onCheckedChange = { appState.applyReducedMotion(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: System & Flavor Actions
            NovaSectionHeader(title = "Automation & Capabilities")
            NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    AccessibilitySettingsRow(navController = navController)

                    NovaSettingsRow(
                        icon = Icons.Default.Shield,
                        title = "Permissions Hub",
                        subtitle = "Microphone, Contacts, Dialer status",
                        onClick = { navController.navigate("permissions") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 5: Diagnostics & About
            NovaSectionHeader(title = "App & Diagnostics")
            NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    NovaSettingsRow(
                        icon = Icons.Default.BugReport,
                        title = "Voice Pipeline Debugger",
                        subtitle = "Live telemetry, VAD, and latency deltas",
                        onClick = { navController.navigate("debug") }
                    )

                    NovaSettingsRow(
                        icon = Icons.Default.Security,
                        title = "Privacy & Data Storage",
                        subtitle = "100% on-device privacy policy",
                        onClick = { navController.navigate("privacy") }
                    )

                    NovaSettingsRow(
                        icon = Icons.Default.Info,
                        title = "About Lia AI",
                        subtitle = "Version, licenses and credits",
                        onClick = { navController.navigate("about") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
