package com.Lia.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.ui.theme.LocalNovaColors

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val forgePrompt: String? = null
)

@Composable
fun NovaMessageBubble(
    message: ChatMessage,
    onCopy: (() -> Unit)? = null,
    onSpeak: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onOpenForge: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalNovaColors.current

    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    val backgroundModifier = if (message.isUser) {
        Modifier
            .background(colors.userBubble, bubbleShape)
            .border(1.dp, colors.accent.copy(alpha = 0.3f), bubbleShape)
    } else {
        Modifier
            .shadow(4.dp, bubbleShape, ambientColor = colors.depthShadow)
            .background(colors.assistantBubble, bubbleShape)
            .border(1.dp, colors.surfaceBorder, bubbleShape)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .then(backgroundModifier)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    color = if (message.isError) colors.error else colors.textPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                // If website forge triggered, render mini preview chip
                if (!message.forgePrompt.isNullOrBlank() && onOpenForge != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceRaised, RoundedCornerShape(12.dp))
                            .border(1.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { onOpenForge(message.forgePrompt) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Launch Website Forge",
                            color = colors.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Action icons for assistant responses
        if (!message.isUser && !message.isStreaming && !message.isError) {
            Row(
                modifier = Modifier.padding(top = 2.dp, start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onCopy != null) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = colors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (onSpeak != null) {
                    IconButton(onClick = onSpeak, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = colors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (onRetry != null) {
                    IconButton(onClick = onRetry, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = colors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
