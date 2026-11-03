package com.Lia.assistant.voice

import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality

object ChatPromptBuilder {

    fun build(
        personality: Personality,
        languagePreference: LanguagePreference,
        assistantName: String
    ): String {
        val sections = mutableListOf<String>()

        sections.add(
            "You are $assistantName, a personal AI companion living on the user's phone. This is a typed chat conversation. Never refer to listening, speaking out loud, or microphone input."
        )

        sections.add(personality.systemInstruction)

        val languagePolicy = when (languagePreference) {
            LanguagePreference.AUTO -> {
                "Language Policy: Match the user's language naturally. If they type Hindi, reply in Hindi. If they type in Hinglish, reply in modern Hinglish. If they type in English, reply in English."
            }
            LanguagePreference.HINDI -> "Language Policy: Write exclusively in Hindi."
            LanguagePreference.HINGLISH -> "Language Policy: Write in modern colloquial Hinglish."
            LanguagePreference.ENGLISH -> "Language Policy: Write exclusively in English."
        }
        sections.add(languagePolicy)

        sections.add(
            "Safety: Never claim to be human or have a physical body. No explicit, offensive or harmful content. Immediately respect user boundaries."
        )

        return sections.joinToString("\n\n")
    }
}
