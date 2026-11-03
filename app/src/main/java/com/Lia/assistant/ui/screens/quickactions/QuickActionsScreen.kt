package com.Lia.assistant.ui.screens.quickactions

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.action.ActionExecutor
import com.Lia.assistant.action.AppLauncher
import com.Lia.assistant.action.CommIntents
import com.Lia.assistant.forge.ForgeController
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

data class QuickActionTile(
    val title: String,
    val phrase: String,
    val icon: ImageVector,
    val action: (Context) -> Unit
)

@Composable
fun QuickActionsScreen(
    onBack: () -> Unit,
    onOpenForge: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalNovaColors.current

    val tiles = listOf(
        QuickActionTile(
            title = "3D Website Forge",
            phrase = "Lia, ek cafe ki website banao",
            icon = Icons.Default.AutoAwesome,
            action = { onOpenForge() }
        ),
        QuickActionTile(
            title = "Open YouTube",
            phrase = "YouTube kholo",
            icon = Icons.Default.PlayArrow,
            action = {
                CoroutineScope(Dispatchers.IO).launch {
                    val pkg = AppLauncher.findApp(it, "youtube") ?: "com.google.android.youtube"
                    AppLauncher.launch(it, pkg)
                }
            }
        ),
        QuickActionTile(
            title = "Open WhatsApp",
            phrase = "WhatsApp kholo",
            icon = Icons.Default.Chat,
            action = {
                CommIntents.openWhatsApp(it, "", "")
            }
        ),
        QuickActionTile(
            title = "Phone Dialer",
            phrase = "Call Mummy",
            icon = Icons.Default.Call,
            action = {
                CommIntents.openDialer(it, "")
            }
        ),
        QuickActionTile(
            title = "System Browser",
            phrase = "Chrome open karo",
            icon = Icons.Default.Language,
            action = {
                CoroutineScope(Dispatchers.IO).launch {
                    val pkg = AppLauncher.findApp(it, "chrome") ?: "com.android.chrome"
                    AppLauncher.launch(it, pkg)
                }
            }
        ),
        QuickActionTile(
            title = "System Settings",
            phrase = "Settings kholo",
            icon = Icons.Default.Settings,
            action = {
                CoroutineScope(Dispatchers.IO).launch {
                    val pkg = AppLauncher.findApp(it, "settings") ?: "com.android.settings"
                    AppLauncher.launch(it, pkg)
                }
            }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
    ) {
        NovaTopBar(title = "Quick Actions", onBackClick = onBack)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tiles) { tile ->
                NovaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { tile.action(context) }
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(colors.surfaceRaised, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tile.icon,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = tile.title,
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "\"${tile.phrase}\"",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
