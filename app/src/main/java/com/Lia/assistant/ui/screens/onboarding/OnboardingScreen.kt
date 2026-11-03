package com.Lia.assistant.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.data.AssistantBrand
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaButtonType
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.fx.NovaOrbStyle
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.NovaOrbState
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val orbState: NovaOrbState,
    val orbStyle: NovaOrbStyle
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    appState: NovaAppState,
    onComplete: () -> Unit
) {
    val colors = LocalNovaColors.current
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPageData(
            title = "A Pearl of Light in the Night",
            subtitle = "Meet ${appState.assistantName}, your intelligent real-time voice companion designed for deep, fluid conversation.",
            orbState = NovaOrbState.CONNECTING,
            orbStyle = NovaOrbStyle.NOVA
        ),
        OnboardingPageData(
            title = "Speaks Your Language",
            subtitle = "Talk effortlessly in Hindi, Hinglish, or English. Lia adapts to your dialect and tone in real time.",
            orbState = NovaOrbState.LISTENING,
            orbStyle = NovaOrbStyle.AURORA
        ),
        OnboardingPageData(
            title = "Takes Real Phone Actions",
            subtitle = "Open apps, call friends, prepare messages, and control your device with voice speed.",
            orbState = NovaOrbState.THINKING,
            orbStyle = NovaOrbStyle.PLASMA
        ),
        OnboardingPageData(
            title = "Website Forge 3D",
            subtitle = "Just say 'Lia, make a cafe website' and watch an animated 3D website materialize before your eyes.",
            orbState = NovaOrbState.SPEAKING,
            orbStyle = NovaOrbStyle.ENERGY
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                NovaButton(
                    text = "Skip",
                    type = NovaButtonType.TEXT,
                    onClick = {
                        appState.applyHasOnboarded(true)
                        onComplete()
                    }
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
            ) { pageIdx ->
                val page = pages[pageIdx]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LiaOrb3D(
                        state = page.orbState,
                        amplitude = 0.5f,
                        size = 200.dp,
                        style = page.orbStyle,
                        assistantName = appState.assistantName
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = page.title,
                        fontFamily = InstrumentSerifFamily,
                        fontSize = 32.sp,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 38.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = page.subtitle,
                        fontSize = 15.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Pager Indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                for (i in pages.indices) {
                    val isSelected = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) colors.accent else colors.surfaceRaised
                            )
                    )
                }
            }

            // Bottom action button
            NovaButton(
                text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                type = NovaButtonType.PRIMARY,
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        appState.applyHasOnboarded(true)
                        onComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
