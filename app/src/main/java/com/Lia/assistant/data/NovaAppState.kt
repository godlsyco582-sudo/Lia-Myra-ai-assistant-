package com.Lia.assistant.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ConversationItem(
    val id: String,
    val title: String,
    val timestamp: Long,
    val preview: String
)

class NovaAppState(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    var themeMode by mutableStateOf(
        NovaPreferences.getString(context, NovaPreferences.Keys.THEME_MODE, "DARK")
    )
        private set

    var reducedMotion by mutableStateOf(
        NovaPreferences.getBoolean(context, NovaPreferences.Keys.REDUCED_MOTION, false)
    )
        private set

    var orbStyle by mutableStateOf(
        NovaPreferences.getString(context, NovaPreferences.Keys.ORB_STYLE, "NOVA")
    )
        private set

    var personality by mutableStateOf(
        Personality.fromName(
            NovaPreferences.getString(context, NovaPreferences.Keys.PERSONALITY, Personality.NORMAL.name)
        )
    )
        private set

    var assistantName by mutableStateOf(
        NovaPreferences.getString(context, NovaPreferences.Keys.ASSISTANT_NAME, AssistantBrand.NAME)
    )
        private set

    var userName by mutableStateOf(
        NovaPreferences.getString(context, NovaPreferences.Keys.USER_NAME, "Sahij")
    )
        private set

    var language by mutableStateOf(
        LanguagePreference.fromName(
            NovaPreferences.getString(context, NovaPreferences.Keys.LANGUAGE, LanguagePreference.AUTO.name)
        )
    )
        private set

    var voiceName by mutableStateOf(
        NovaPreferences.getString(context, NovaPreferences.Keys.VOICE_NAME, "Aoede")
    )
        private set

    var speechSpeed by mutableFloatStateOf(
        NovaPreferences.getFloat(context, NovaPreferences.Keys.SPEECH_SPEED, 1.0f)
    )
        private set

    var voiceVolume by mutableFloatStateOf(
        NovaPreferences.getFloat(context, NovaPreferences.Keys.VOICE_VOLUME, 1.0f)
    )
        private set

    var hasOnboarded by mutableStateOf(
        NovaPreferences.getBoolean(context, NovaPreferences.Keys.HAS_ONBOARDED, false)
    )
        private set

    var chatModel by mutableStateOf(
        NovaPreferences.getString(context, NovaPreferences.Keys.CHAT_MODEL, "gemini-3.8-flash")
    )
        private set

    var liveModel by mutableStateOf(
        NovaPreferences.getString(context, NovaPreferences.Keys.LIVE_MODEL, "models/gemini-3.1-flash-live-preview")
    )
        private set

    var conversations by mutableStateOf<List<ConversationItem>>(emptyList())
        private set

    fun applyChatModel(model: String) {
        chatModel = model
        NovaPreferences.putString(context, NovaPreferences.Keys.CHAT_MODEL, model)
        com.Lia.assistant.voice.GeminiTextClient.cachedModel = model
    }

    fun applyLiveModel(model: String) {
        liveModel = model
        NovaPreferences.putString(context, NovaPreferences.Keys.LIVE_MODEL, model)
    }

    fun applyThemeMode(mode: String) {
        themeMode = mode
        NovaPreferences.putString(context, NovaPreferences.Keys.THEME_MODE, mode)
    }

    fun applyReducedMotion(enabled: Boolean) {
        reducedMotion = enabled
        NovaPreferences.putBoolean(context, NovaPreferences.Keys.REDUCED_MOTION, enabled)
    }

    fun applyOrbStyle(style: String) {
        orbStyle = style
        NovaPreferences.putString(context, NovaPreferences.Keys.ORB_STYLE, style)
    }

    fun applyPersonality(newPersonality: Personality) {
        personality = newPersonality
        coroutineScope.launch(Dispatchers.IO) {
            PersonalityRepository.setPersonality(context, newPersonality)
        }
    }

    fun applyAssistantName(name: String) {
        val clean = name.trim().ifBlank { AssistantBrand.NAME }
        assistantName = clean
        coroutineScope.launch(Dispatchers.IO) {
            PersonalityRepository.setAssistantName(context, clean)
        }
    }

    fun applyUserName(name: String) {
        val clean = name.trim().ifBlank { "User" }
        userName = clean
        NovaPreferences.putString(context, NovaPreferences.Keys.USER_NAME, clean)
    }

    fun applyLanguage(newLanguage: LanguagePreference) {
        language = newLanguage
        coroutineScope.launch(Dispatchers.IO) {
            PersonalityRepository.setLanguage(context, newLanguage)
        }
    }

    fun applyVoiceName(newVoice: String) {
        voiceName = newVoice
        coroutineScope.launch(Dispatchers.IO) {
            PersonalityRepository.setVoiceName(context, newVoice)
        }
    }

    fun applySpeechSpeed(speed: Float) {
        speechSpeed = speed
        NovaPreferences.putFloat(context, NovaPreferences.Keys.SPEECH_SPEED, speed)
    }

    fun applyVoiceVolume(volume: Float) {
        voiceVolume = volume
        NovaPreferences.putFloat(context, NovaPreferences.Keys.VOICE_VOLUME, volume)
    }

    fun applyHasOnboarded(onboarded: Boolean) {
        hasOnboarded = onboarded
        NovaPreferences.putBoolean(context, NovaPreferences.Keys.HAS_ONBOARDED, onboarded)
    }

    fun applyConversations(items: List<ConversationItem>) {
        conversations = items
    }
}

@Composable
fun rememberNovaAppState(
    context: Context,
    scope: CoroutineScope = rememberCoroutineScope()
): NovaAppState {
    return remember(context) {
        NovaAppState(context, scope)
    }
}
