package com.Lia.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.ui.theme.LocalNovaColors

@Composable
fun LiaBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNovaColors.current
    val barShape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating glass bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(16.dp, barShape, ambientColor = colors.depthShadow, spotColor = colors.accentGlow)
                .background(colors.surface.copy(alpha = 0.92f), barShape)
                .border(1.dp, colors.surfaceBorder, barShape)
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(68.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentRoute == "home",
                    onClick = { onNavigate("home") }
                )
                NavItem(
                    label = "Chat",
                    icon = Icons.Default.ChatBubbleOutline,
                    isSelected = currentRoute == "chat",
                    onClick = { onNavigate("chat") }
                )

                // Space for the center floating orb button
                Box(modifier = Modifier.size(56.dp))

                NavItem(
                    label = "History",
                    icon = Icons.Default.History,
                    isSelected = currentRoute == "history",
                    onClick = { onNavigate("history") }
                )
                NavItem(
                    label = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = currentRoute == "settings",
                    onClick = { onNavigate("settings") }
                )
            }
        }

        // Centre Talk Orb button, docked & slightly raised
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(62.dp)
                .shadow(12.dp, CircleShape, spotColor = colors.accent)
                .background(colors.accent, CircleShape)
                .clickable(onClick = onVoiceClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Mode",
                tint = colors.onAccent,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalNovaColors.current
    val tint = if (isSelected) colors.accent else colors.textTertiary

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = tint,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
