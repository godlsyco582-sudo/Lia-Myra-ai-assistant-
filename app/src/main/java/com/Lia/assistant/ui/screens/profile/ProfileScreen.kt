package com.Lia.assistant.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.license.AccessKeyManager
import com.Lia.assistant.license.AccessKeySection
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaTextField
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    appState: NovaAppState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalNovaColors.current
    var editingName by remember { mutableStateOf(appState.userName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
            .verticalScroll(rememberScrollState())
    ) {
        NovaTopBar(title = "Profile & Account", onBackClick = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with soft glow
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(colors.surfaceRaised, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = appState.userName,
                fontFamily = InstrumentSerifFamily,
                fontSize = 28.sp,
                color = colors.textPrimary
            )
            Text(
                text = "Companion: ${appState.assistantName} (${appState.personality.displayName})",
                fontSize = 13.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Edit Your Name",
                        color = colors.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NovaTextField(
                        value = editingName,
                        onValueChange = { editingName = it },
                        placeholder = "Your name",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NovaButton(
                        text = "Save Name",
                        onClick = { appState.applyUserName(editingName) },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AccessKeySection(
                onActivate = { key ->
                    CoroutineScope(Dispatchers.IO).launch {
                        AccessKeyManager.activate(context, key)
                    }
                }
            )
        }
    }
}
