package com.Lia.assistant.ui.screens.forge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.forge.ForgeController
import com.Lia.assistant.forge.ForgePhase
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaTextField
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky

@Composable
fun ForgeScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalNovaColors.current
    val uiState by ForgeController.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editPrompt by remember { mutableStateOf("") }
    var isRevealing by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
    ) {
        when (uiState.phase) {
            ForgePhase.BUILDING -> {
                // 3D Scene + HUD
                ForgeWebGlScene(
                    stage = uiState.stage,
                    progress = uiState.progress,
                    isRevealing = isRevealing,
                    onRevealFinished = { isRevealing = false },
                    modifier = Modifier.fillMaxSize()
                )

                ForgeHud(
                    state = uiState,
                    onCancel = { ForgeController.cancel() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            ForgePhase.DONE -> {
                val file = uiState.file
                val html = remember(file) { file?.readText().orEmpty() }

                Box(modifier = Modifier.fillMaxSize()) {
                    GeneratedSiteView(html = html, modifier = Modifier.fillMaxSize())

                    ResultToolbar(
                        file = file,
                        onBack = onBack,
                        onEdit = {
                            editPrompt = ""
                            showEditDialog = true
                        },
                        onDelete = {
                            file?.delete()
                            ForgeController.reset()
                            onBack()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            ForgePhase.FAILED, ForgePhase.CANCELLED -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NovaTopBar(title = "Website Forge", onBackClick = onBack)
                    Spacer(modifier = Modifier.weight(1f))
                    NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (uiState.phase == ForgePhase.CANCELLED) "Forge Cancelled" else "Forge Encountered an Error",
                                fontFamily = InstrumentSerifFamily,
                                fontSize = 24.sp,
                                color = if (uiState.phase == ForgePhase.CANCELLED) colors.textPrimary else colors.error
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = uiState.error ?: "Operation paused by user.",
                                color = colors.textSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            NovaButton(
                                text = "Retry Forge",
                                onClick = { ForgeController.retry(context) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            ForgePhase.IDLE -> {
                var promptInput by remember { mutableStateOf("") }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    NovaTopBar(title = "Website Forge", onBackClick = onBack)
                    Spacer(modifier = Modifier.height(20.dp))
                    NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "Create a 3D Website",
                                fontFamily = InstrumentSerifFamily,
                                fontSize = 28.sp,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enter what kind of website, portfolio, landing page or shop you want Lia to build with 3D effects:",
                                color = colors.textSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            NovaTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                placeholder = "e.g. A luxury specialty coffee cafe in Tokyo with 3D cup and menu",
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            NovaButton(
                                text = "Forge Website",
                                onClick = {
                                    if (promptInput.isNotBlank()) {
                                        ForgeController.start(context, promptInput)
                                    }
                                },
                                enabled = promptInput.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Edit Dialog
        if (showEditDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                NovaGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "Refine Website",
                            fontFamily = InstrumentSerifFamily,
                            fontSize = 22.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        NovaTextField(
                            value = editPrompt,
                            onValueChange = { editPrompt = it },
                            placeholder = "Describe changes (e.g. Change accent to emerald green, add contact section)...",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NovaButton(
                            text = "Apply Changes",
                            onClick = {
                                if (editPrompt.isNotBlank()) {
                                    showEditDialog = false
                                    ForgeController.edit(context, editPrompt)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
