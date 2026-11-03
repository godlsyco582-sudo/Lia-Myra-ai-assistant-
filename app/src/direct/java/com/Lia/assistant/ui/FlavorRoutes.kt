package com.Lia.assistant.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.Lia.assistant.agent.core.NovaAccessibilityService
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaSettingsRow
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky

fun registerFlavorRoutes(navGraphBuilder: NavGraphBuilder, navController: NavController) {
    navGraphBuilder.composable("accessibility_disclosure") {
        AccessibilityDisclosureScreen(onBack = { navController.popBackStack() })
    }
}

@Composable
fun AccessibilitySettingsRow(navController: NavController) {
    val context = LocalContext.current
    val isEnabled = NovaAccessibilityService.isEnabled(context)

    NovaSettingsRow(
        icon = Icons.Default.Accessibility,
        title = "Screen Control & Accessibility",
        subtitle = if (isEnabled) "Active - Screen automation enabled" else "Tap to configure accessibility permissions",
        value = if (isEnabled) "Active" else "Disabled",
        onClick = { navController.navigate("accessibility_disclosure") }
    )
}

@Composable
private fun AccessibilityDisclosureScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val colors = LocalNovaColors.current
    val isEnabled = NovaAccessibilityService.isEnabled(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
    ) {
        NovaTopBar(title = "Accessibility Disclosure", onBackClick = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Autonomous Screen Automation",
                        fontFamily = InstrumentSerifFamily,
                        fontSize = 24.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Lia Direct edition utilizes the Android AccessibilityService API to perform authorized tasks on your behalf when you explicitly ask, such as:\n\n" +
                                "• Inspecting current screen UI elements (read_screen)\n" +
                                "• Tapping buttons, labels, and links (tap_text)\n" +
                                "• Entering text into focused inputs (type_text)\n" +
                                "• Scrolling and navigating back/home\n\n" +
                                "Privacy Guarantee:\n" +
                                "Lia never collects, transmits, or stores passwords, personal credentials, payment data, or sensitive personal information. Screen actions only run upon your spoken or typed command.",
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    NovaButton(
                        text = if (isEnabled) "Accessibility Active (Open Settings)" else "Enable Accessibility Service",
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
