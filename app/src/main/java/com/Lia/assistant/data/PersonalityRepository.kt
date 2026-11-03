package com.Lia.assistant.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.Lia.assistant.voice.VoiceSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "assistant_settings")

object PersonalityRepository {
    private val KEY_PERSONALITY = stringPreferencesKey("personality")
    private val KEY_LANGUAGE = stringPreferencesKey("language")
    private val KEY_ASSISTANT_NAME = stringPreferencesKey("assistant_name")
    private val KEY_VOICE_NAME = stringPreferencesKey("voice_name")

    fun getPersonalityFlow(context: Context): Flow<Personality> {
        return context.dataStore.data.map { prefs ->
            val stored = prefs[KEY_PERSONALITY] ?: NovaPreferences.getString(
                context,
                NovaPreferences.Keys.PERSONALITY,
                Personality.NORMAL.name
            )
            Personality.fromName(stored)
        }
    }

    suspend fun getPersonality(context: Context): Personality {
        val prefs = context.dataStore.data.first()
        val stored = prefs[KEY_PERSONALITY] ?: NovaPreferences.getString(
            context,
            NovaPreferences.Keys.PERSONALITY,
            Personality.NORMAL.name
        )
        return Personality.fromName(stored)
    }

    suspend fun setPersonality(context: Context, personality: Personality) {
        context.dataStore.edit { it[KEY_PERSONALITY] = personality.name }
        NovaPreferences.putString(context, NovaPreferences.Keys.PERSONALITY, personality.name)
        VoiceSessionManager.refreshInstructions(context)
    }

    fun getLanguageFlow(context: Context): Flow<LanguagePreference> {
        return context.dataStore.data.map { prefs ->
            val stored = prefs[KEY_LANGUAGE] ?: NovaPreferences.getString(
                context,
                NovaPreferences.Keys.LANGUAGE,
                LanguagePreference.AUTO.name
            )
            LanguagePreference.fromName(stored)
        }
    }

    suspend fun getLanguage(context: Context): LanguagePreference {
        val prefs = context.dataStore.data.first()
        val stored = prefs[KEY_LANGUAGE] ?: NovaPreferences.getString(
            context,
            NovaPreferences.Keys.LANGUAGE,
            LanguagePreference.AUTO.name
        )
        return LanguagePreference.fromName(stored)
    }

    suspend fun setLanguage(context: Context, language: LanguagePreference) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language.name }
        NovaPreferences.putString(context, NovaPreferences.Keys.LANGUAGE, language.name)
        VoiceSessionManager.refreshInstructions(context)
    }

    fun getAssistantNameFlow(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_ASSISTANT_NAME] ?: NovaPreferences.getString(
                context,
                NovaPreferences.Keys.ASSISTANT_NAME,
                AssistantBrand.NAME
            )
        }
    }

    suspend fun getAssistantName(context: Context): String {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_ASSISTANT_NAME] ?: NovaPreferences.getString(
            context,
            NovaPreferences.Keys.ASSISTANT_NAME,
            AssistantBrand.NAME
        )
    }

    suspend fun setAssistantName(context: Context, name: String) {
        val cleanName = name.trim().ifBlank { AssistantBrand.NAME }
        context.dataStore.edit { it[KEY_ASSISTANT_NAME] = cleanName }
        NovaPreferences.putString(context, NovaPreferences.Keys.ASSISTANT_NAME, cleanName)
        VoiceSessionManager.refreshInstructions(context)
    }

    fun getVoiceNameFlow(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_VOICE_NAME] ?: NovaPreferences.getString(
                context,
                NovaPreferences.Keys.VOICE_NAME,
                "Aoede"
            )
        }
    }

    suspend fun getVoiceName(context: Context): String {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_VOICE_NAME] ?: NovaPreferences.getString(
            context,
            NovaPreferences.Keys.VOICE_NAME,
            "Aoede"
        )
    }

    suspend fun setVoiceName(context: Context, voiceName: String) {
        val cleanVoice = voiceName.trim().ifBlank { "Aoede" }
        context.dataStore.edit { it[KEY_VOICE_NAME] = cleanVoice }
        NovaPreferences.putString(context, NovaPreferences.Keys.VOICE_NAME, cleanVoice)
        VoiceSessionManager.refreshInstructions(context)
    }
}
