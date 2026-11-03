package com.Lia.assistant.ui.screens.about

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.data.AssistantBrand
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.NovaOrbState

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors = LocalNovaColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
            .verticalScroll(rememberScrollState())
    ) {
        NovaTopBar(title = "About Lia AI", onBackClick = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LiaOrb3D(
                state = NovaOrbState.CONNECTING,
                amplitude = 0.3f,
                size = 120.dp,
                assistantName = AssistantBrand.NAME
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = AssistantBrand.FULL_NAME,
                fontFamily = InstrumentSerifFamily,
                fontSize = 32.sp,
                color = colors.textPrimary
            )
            Text(
                text = "Version 1.0 (Production Architecture)",
                fontSize = 13.sp,
                color = colors.accent
            )
            Text(
                text = AssistantBrand.TAGLINE,
                fontSize = 13.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Open Source Typography & Licenses",
                        fontSize = 16.sp,
                        color = colors.textPrimary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Instrument Serif - SIL Open Font License 1.1\n" +
                                "• Manrope - SIL Open Font License 1.1\n" +
                                "• Material Symbols & Icons - Apache 2.0\n" +
                                "• Jetpack Compose & AndroidX - Apache 2.0",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
