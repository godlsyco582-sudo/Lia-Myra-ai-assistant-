package com.Lia.assistant.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.data.AssistantBrand
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.NovaOrbState
import kotlinx.coroutines.delay

@Composable
fun LiaSplash3D(onFinish: () -> Unit) {
    val colors = LocalNovaColors.current
    val orbScale = remember { Animatable(0.2f) }
    val titleAlpha = remember { Animatable(0f) }
    val overallAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        orbScale.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
        titleAlpha.animateTo(1f, animationSpec = tween(500))
        delay(600)
        overallAlpha.animateTo(0f, animationSpec = tween(400))
        SplashSession.played = true
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(overallAlpha.value)
            .nightSky(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.scale(orbScale.value)) {
                LiaOrb3D(
                    state = NovaOrbState.CONNECTING,
                    amplitude = 0.4f,
                    size = 180.dp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = AssistantBrand.FULL_NAME,
                fontFamily = InstrumentSerifFamily,
                fontSize = 42.sp,
                color = colors.textPrimary,
                modifier = Modifier.alpha(titleAlpha.value)
            )
            Text(
                text = AssistantBrand.TAGLINE,
                fontSize = 14.sp,
                color = colors.accent,
                modifier = Modifier.alpha(titleAlpha.value)
            )
        }
    }
}
