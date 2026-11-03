package com.Lia.assistant.ui.screens.privacy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.data.AssistantBrand
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val colors = LocalNovaColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
            .verticalScroll(rememberScrollState())
    ) {
        NovaTopBar(title = "Privacy & Security", onBackClick = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Your Privacy is Paramount",
                        fontFamily = InstrumentSerifFamily,
                        fontSize = 24.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "1. Local Key Storage:\n" +
                                "Your Gemini API key is stored strictly on your device inside private encrypted Android SharedPreferences. It is never logged, tracked, or sent to third-party tracking servers.\n\n" +
                                "2. Real-time Audio Stream:\n" +
                                "Audio from your microphone is streamed solely to Google's official Gemini Live WebSocket endpoint during active voice conversations. We do not store or record audio files on any servers.\n\n" +
                                "3. Zero Telemetry & Tracking:\n" +
                                "${AssistantBrand.FULL_NAME} contains no ad trackers, analytics SDKs, or data brokers.\n\n" +
                                "4. On-Device Actions:\n" +
                                "Actions like looking up contacts or opening apps happen directly on your phone using standard Android platform APIs.",
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
