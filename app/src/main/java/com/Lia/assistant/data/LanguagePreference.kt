package com.Lia.assistant.data

enum class LanguagePreference(val displayName: String, val description: String) {
    AUTO("Auto Detect", "Follows whatever language you speak"),
    HINDI("Hindi", "Hindi - शुद्ध हिंदी में बातचीत"),
    HINGLISH("Hinglish", "Hinglish - Natural Hindi + English mix"),
    ENGLISH("English", "English - Clear and fluent");

    companion object {
        fun fromName(name: String?): LanguagePreference {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: AUTO
        }
    }
}
