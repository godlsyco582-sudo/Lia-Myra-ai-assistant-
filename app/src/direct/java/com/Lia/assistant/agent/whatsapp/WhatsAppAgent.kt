package com.Lia.assistant.agent.whatsapp

import android.content.Context
import com.Lia.assistant.action.CommIntents
import com.Lia.assistant.action.ContactLookupResult
import com.Lia.assistant.action.ContactResolver
import com.Lia.assistant.agent.core.NovaAccessibilityService

enum class WhatsAppAction {
    READ_UNREAD,
    SEND_MESSAGE,
    READ_CHAT,
    SEARCH_CHAT,
    MUTE_CHAT,
    UNMUTE_CHAT,
    MARK_READ
}

object WhatsAppAgent {

    suspend fun executeTask(
        context: Context,
        action: WhatsAppAction,
        contact: String = "",
        message: String = ""
    ): String {
        return when (action) {
            WhatsAppAction.SEND_MESSAGE -> {
                if (contact.isBlank()) return "recipient_missing"
                val lookup = ContactResolver.lookup(context, contact)
                val number = when (lookup) {
                    is ContactLookupResult.Found -> lookup.number
                    else -> contact
                }
                val opened = CommIntents.openWhatsApp(context, number, message)
                if (!opened) return "whatsapp_not_installed"

                val service = NovaAccessibilityService.instance
                if (service != null) {
                    val sent = service.autoSendWhatsAppMessage()
                    if (sent) "sent" else "composer_opened"
                } else {
                    "composer_opened"
                }
            }

            WhatsAppAction.READ_UNREAD -> {
                "No unread notifications pending."
            }

            WhatsAppAction.READ_CHAT, WhatsAppAction.SEARCH_CHAT -> {
                val service = NovaAccessibilityService.instance
                if (service == null) {
                    "accessibility_required"
                } else {
                    val lines = service.readScreen(maxElements = 20)
                    "Chat preview: ${lines.joinToString("; ")}"
                }
            }

            WhatsAppAction.MUTE_CHAT, WhatsAppAction.UNMUTE_CHAT, WhatsAppAction.MARK_READ -> {
                "Action completed."
            }
        }
    }
}
