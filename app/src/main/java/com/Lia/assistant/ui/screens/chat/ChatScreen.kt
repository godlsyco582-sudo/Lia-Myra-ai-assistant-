package com.Lia.assistant.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.forge.ForgeController
import com.Lia.assistant.forge.ForgeIntent
import com.Lia.assistant.ui.components.ChatMessage
import com.Lia.assistant.ui.components.NovaMessageBubble
import com.Lia.assistant.ui.components.NovaTextField
import com.Lia.assistant.ui.components.NovaTypingDots
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.theme.InstrumentSerifFamily
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.ChatPromptBuilder
import com.Lia.assistant.voice.ChatReplyResult
import com.Lia.assistant.voice.ChatTurn
import com.Lia.assistant.voice.GeminiTextClient
import com.Lia.assistant.voice.NovaOrbState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ChatScreen(
    appState: NovaAppState,
    onBack: () -> Unit,
    onOpenForge: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalNovaColors.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Hello ${appState.userName}! I am ${appState.assistantName}. How can I assist you today?",
                isUser = false
            )
        )
    }

    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        val raw = inputText.trim()
        if (raw.isBlank() || isTyping) return

        inputText = ""
        messages.add(ChatMessage(text = raw, isUser = true))

        // Check if user requested to build a website via ForgeIntent
        val forgePrompt = ForgeIntent.extract(raw)
        if (forgePrompt != null) {
            ForgeController.start(context, forgePrompt)
            messages.add(
                ChatMessage(
                    text = "Opening the Website Forge to craft your 3D website for: \"$forgePrompt\"...",
                    isUser = false,
                    forgePrompt = forgePrompt
                )
            )
            return
        }

        val apiKey = ApiKeyStore.getKey(context)
        if (apiKey.isBlank()) {
            messages.add(
                ChatMessage(
                    text = "Please enter your Gemini API key in Settings first to chat.",
                    isUser = false,
                    isError = true
                )
            )
            return
        }

        isTyping = true
        scope.launch {
            val historyTurns = messages.map {
                ChatTurn(isUser = it.isUser, text = it.text)
            }
            val systemInstruction = ChatPromptBuilder.build(
                personality = appState.personality,
                languagePreference = appState.language,
                assistantName = appState.assistantName
            )

            val result = GeminiTextClient.reply(
                apiKey = apiKey,
                systemInstruction = systemInstruction,
                history = historyTurns,
                configuredModel = appState.chatModel
            )

            isTyping = false

            when (result) {
                is ChatReplyResult.Success -> {
                    // Word-by-word reveal (20ms per word)
                    val words = result.text.split(" ")
                    val bubbleIndex = messages.size
                    messages.add(ChatMessage(text = "", isUser = false, isStreaming = true))

                    var accumulated = ""
                    for ((i, word) in words.withIndex()) {
                        accumulated = if (i == 0) word else "$accumulated $word"
                        messages[bubbleIndex] = ChatMessage(text = accumulated, isUser = false, isStreaming = true)
                        delay(20)
                    }
                    messages[bubbleIndex] = ChatMessage(text = result.text, isUser = false, isStreaming = false)
                }
                is ChatReplyResult.Error -> {
                    messages.add(ChatMessage(text = result.message, isUser = false, isError = true))
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nightSky()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.size(40.dp)) {
                    LiaOrb3D(
                        state = if (isTyping) NovaOrbState.THINKING else NovaOrbState.IDLE,
                        amplitude = if (isTyping) 0.6f else 0.1f,
                        size = 40.dp,
                        assistantName = appState.assistantName
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = appState.assistantName,
                        fontFamily = InstrumentSerifFamily,
                        fontSize = 20.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isTyping) "Typing..." else "Here to help",
                        fontSize = 12.sp,
                        color = if (isTyping) colors.accent else colors.textTertiary
                    )
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    NovaMessageBubble(
                        message = msg,
                        onCopy = {
                            val clip = ClipData.newPlainText("LiaMessage", msg.text)
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(clip)
                        },
                        onSpeak = {
                            tts?.speak(msg.text, TextToSpeech.QUEUE_FLUSH, null, "lia_msg")
                        },
                        onRetry = {
                            sendMessage()
                        },
                        onOpenForge = onOpenForge
                    )
                }

                if (isTyping) {
                    item {
                        Box(modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 8.dp)) {
                            NovaTypingDots()
                        }
                    }
                }
            }

            // Floating Glass Input Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NovaTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = "Ask ${appState.assistantName} anything...",
                        singleLine = false,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(8.dp, CircleShape, spotColor = colors.accent)
                            .background(colors.accent, CircleShape)
                            .clickable(enabled = inputText.isNotBlank() && !isTyping) {
                                sendMessage()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = colors.onAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
