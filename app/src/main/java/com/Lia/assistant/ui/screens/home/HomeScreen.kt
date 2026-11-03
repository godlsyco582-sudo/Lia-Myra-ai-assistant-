package com.Lia.assistant.ui.screens.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.license.AccessKeyBanner
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaButtonType
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.fx.NovaOrbStyle
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalBottomBarInset
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.VoiceSessionManager
import java.util.Calendar

@Composable
fun HomeScreen(
    appState: NovaAppState,
    onOpenVoice: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenQuickActions: () -> Unit,
    onOpenForgePrompt: (String) -> Unit
) {
    val colors = LocalNovaColors.current
    val bottomInset = LocalBottomBarInset.current
    val voiceState by VoiceSessionManager.state.collectAsState()
    val amplitude by VoiceSessionManager.amplitude.collectAsState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }

    val trySayingSuggestions = listOf(
        "Lia, ek cafe ki 3D website banao" to "Build a website",
        "Mummy ko call lagao" to "Call contact",
        "YouTube kholo" to "Open App",
        "Aaj ka mausam kaisa hai?" to "Ask anything",
        "Ek fresh startup landing page bana do" to "Build a website"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
            .verticalScroll(rememberScrollState())
            .padding(bottomInset)
    ) {
        // Floating Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(8.dp, CircleShape, spotColor = colors.accent)
                        .background(colors.surfaceRaised, CircleShape)
                        .border(1.dp, colors.accent.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Logo",
                        tint = colors.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = appState.assistantName,
                        fontFamily = InstrumentSerifFamily,
                        fontSize = 24.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Personality: ${appState.personality.displayName}",
                        fontSize = 11.sp,
                        color = colors.textTertiary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(colors.surfaceGlass, CircleShape)
                    .border(1.dp, colors.surfaceBorder, CircleShape)
                    .clickable(onClick = onOpenProfile),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        AccessKeyBanner(onActivateClick = onOpenProfile)

        Spacer(modifier = Modifier.height(12.dp))

        // Time-based greeting in serif font
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$greeting, ${appState.userName}",
                fontFamily = InstrumentSerifFamily,
                fontSize = 34.sp,
                color = colors.textPrimary,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "How can I assist your world today?",
                fontSize = 14.sp,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Hero 3D Orb Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenVoice),
            contentAlignment = Alignment.Center
        ) {
            LiaOrb3D(
                state = voiceState,
                amplitude = amplitude,
                size = 230.dp,
                style = NovaOrbStyle.entries.firstOrNull { it.name == appState.orbStyle } ?: NovaOrbStyle.NOVA,
                assistantName = appState.assistantName
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tap me to talk",
            fontFamily = InstrumentSerifFamily,
            fontStyle = FontStyle.Italic,
            fontSize = 17.sp,
            color = colors.accent,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Action Buttons: Talk (marigold) and Type (glass)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NovaButton(
                text = "Talk",
                icon = Icons.Default.KeyboardVoice,
                type = NovaButtonType.PRIMARY,
                onClick = onOpenVoice,
                modifier = Modifier.weight(1f)
            )

            NovaButton(
                text = "Type",
                icon = Icons.Default.ChatBubbleOutline,
                type = NovaButtonType.SECONDARY,
                onClick = onOpenChat,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // "Try Saying" Horizontal Carousel
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRY SAYING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Quick Actions",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.clickable(onClick = onOpenQuickActions)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trySayingSuggestions) { (phrase, tag) ->
                    NovaGlassCard(
                        modifier = Modifier.width(260.dp),
                        onClick = {
                            if (phrase.contains("website", ignoreCase = true)) {
                                onOpenForgePrompt(phrase)
                            } else {
                                onOpenVoice()
                            }
                        }
                    ) {
                        Column {
                            Text(
                                text = tag.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.lagoon
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\"$phrase\"",
                                fontSize = 14.sp,
                                color = colors.textPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
