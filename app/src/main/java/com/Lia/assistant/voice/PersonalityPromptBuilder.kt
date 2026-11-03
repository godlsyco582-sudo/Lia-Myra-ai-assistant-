package com.Lia.assistant.voice

import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality

object PersonalityPromptBuilder {

    fun build(
        personality: Personality,
        languagePreference: LanguagePreference,
        detectedLanguage: DetectedLanguage,
        recap: String?,
        assistantName: String
    ): String {
        val sections = mutableListOf<String>()

        // (a) Base
        sections.add(
            "You are $assistantName, a persistent personal voice assistant living on the user's phone. Keep replies short and conversational, like spoken conversation. Do not use markdown headers, bullets, or long lists in voice mode."
        )

        // (b) Capabilities from flavor
        sections.add(LiaCapabilityPrompts.toolInstruction(assistantName))

        // (c) Personality instruction
        sections.add(personality.systemInstruction)

        // (d) Language policy
        val languagePolicy = when (languagePreference) {
            LanguagePreference.AUTO -> {
                "Language Policy: Follow whatever language the user speaks. If they speak Hindi, respond in Hindi. If they speak Hinglish, respond in natural Hinglish. If they speak English, respond in fluent English. Never translate their statement back to them—simply reply directly in the same language turn by turn."
            }
            LanguagePreference.HINDI -> {
                "Language Policy: Speak strictly in pure, natural, and polite Hindi."
            }
            LanguagePreference.HINGLISH -> {
                "Language Policy: Speak in natural Hinglish (conversational Hindi words written in Latin script mixed naturally with daily English words)."
            }
            LanguagePreference.ENGLISH -> {
                "Language Policy: Speak exclusively in modern, clear, conversational English."
            }
        }
        sections.add(languagePolicy)

        // (e) Safety & Conduct
        sections.add(
            "Safety & Boundaries: You are an AI assistant living on this Android device. Never claim to be human, never claim to have a physical body or human organs. Do not produce sexually explicit, harmful, abusive, or dangerous content. If the user asks to change the topic or tone, respect their boundary immediately. Vary your phrasing naturally."
        )

        // (f) Recap if continuing
        if (!recap.isNullOrBlank()) {
            sections.add("This is a continuing conversation — do not greet again. Recap:\n$recap")
        }

        return sections.joinToString("\n\n")
    }
}
