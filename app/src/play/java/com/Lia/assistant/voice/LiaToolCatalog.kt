package com.Lia.assistant.voice

import com.Lia.assistant.action.ToolDeclHelper
import org.json.JSONArray

object LiaToolCatalog {

    fun declarations(): JSONArray {
        val list = JSONArray()

        // 1. Build Website
        list.put(
            ToolDeclHelper.functionDecl(
                name = "build_website",
                description = "Starts the live 3D Website Forge to generate an interactive website, landing page, or web app based on the user's prompt.",
                params = mapOf("prompt" to "Description or request for the website to generate"),
                required = listOf("prompt")
            )
        )

        // 2. Open App
        list.put(
            ToolDeclHelper.functionDecl(
                name = "open_app",
                description = "Opens any installed application on the user's phone by its name (e.g. YouTube, WhatsApp, Camera, Settings, Spotify).",
                params = mapOf("app_name" to "Name of the app to launch"),
                required = listOf("app_name")
            )
        )

        // 3. Call Contact
        list.put(
            ToolDeclHelper.functionDecl(
                name = "call_contact",
                description = "Initiates a phone call to a named contact or phone number.",
                params = mapOf("name_or_number" to "Contact name or phone number to call"),
                required = listOf("name_or_number")
            )
        )

        // 4. Message Contact
        list.put(
            ToolDeclHelper.functionDecl(
                name = "message_contact",
                description = "Opens the message composer in WhatsApp or SMS with the recipient and draft message filled. The user will review and tap Send.",
                params = mapOf(
                    "recipient" to "Contact name or phone number",
                    "message" to "Text content of the message",
                    "platform" to "Messaging platform: 'whatsapp' or 'sms'"
                ),
                required = listOf("recipient", "message")
            )
        )

        return list
    }
}
