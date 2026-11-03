package com.Lia.assistant.ui.screens.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaTextField
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.fx.NovaOrb
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalBottomBarInset
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.NovaOrbState

@Composable
fun HistoryScreen(
    appState: NovaAppState,
    onOpenChat: () -> Unit
) {
    val colors = LocalNovaColors.current
    val bottomInset = LocalBottomBarInset.current
    var searchQuery by remember { mutableStateOf("") }

    val savedHistory = appState.conversations

    val filtered = if (searchQuery.isBlank()) {
        savedHistory
    } else {
        savedHistory.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.preview.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
            .padding(bottomInset)
    ) {
        NovaTopBar(title = "History")

        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            NovaTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search conversation history...",
                singleLine = true,
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = colors.textTertiary)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (filtered.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NovaOrb(
                    state = NovaOrbState.IDLE,
                    amplitude = 0f,
                    size = 120.dp,
                    assistantName = appState.assistantName
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "No conversations yet",
                    fontFamily = InstrumentSerifFamily,
                    fontSize = 24.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start speaking or chatting with ${appState.assistantName} to build your history log.",
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                NovaButton(
                    text = "Start Conversation",
                    onClick = onOpenChat
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { item ->
                    NovaGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onOpenChat
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.preview,
                                    color = colors.textSecondary,
                                    fontSize = 13.sp,
                                    maxLines = 2
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = colors.textTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
