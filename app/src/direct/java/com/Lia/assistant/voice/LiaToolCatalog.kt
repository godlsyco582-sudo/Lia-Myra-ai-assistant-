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
                description = "Opens any installed application on the user's phone by its name.",
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
                description = "Prepares and sends a message via WhatsApp or SMS. On direct flavor, automatically navigates and confirms sending.",
                params = mapOf(
                    "recipient" to "Contact name or phone number",
                    "message" to "Text content of the message",
                    "platform" to "Messaging platform: 'whatsapp' or 'sms'"
                ),
                required = listOf("recipient", "message")
            )
        )

        // 5. Read Screen
        list.put(
            ToolDeclHelper.functionDecl(
                name = "read_screen",
                description = "Reads visible elements, text, and inputs on the current phone screen using accessibility.",
                params = emptyMap()
            )
        )

        // 6. Tap Text
        list.put(
            ToolDeclHelper.functionDecl(
                name = "tap_text",
                description = "Taps on a UI element, button, or link that displays or matches the given text.",
                params = mapOf("text" to "Text label on the UI element to tap"),
                required = listOf("text")
            )
        )

        // 7. Type Text
        list.put(
            ToolDeclHelper.functionDecl(
                name = "type_text",
                description = "Types text into the currently active or focused input field.",
                params = mapOf("text" to "Text string to input"),
                required = listOf("text")
            )
        )

        // 8. Scroll Screen
        list.put(
            ToolDeclHelper.functionDecl(
                name = "scroll_screen",
                description = "Scrolls the current screen forward or backward.",
                params = mapOf("direction" to "Direction to scroll: 'down', 'up', 'forward', or 'backward'"),
                required = listOf("direction")
            )
        )

        // 9. Go Back
        list.put(
            ToolDeclHelper.functionDecl(
                name = "go_back",
                description = "Triggers the Android system Back navigation action.",
                params = emptyMap()
            )
        )

        // 10. Go Home
        list.put(
            ToolDeclHelper.functionDecl(
                name = "go_home",
                description = "Triggers the Android system Home navigation action.",
                params = emptyMap()
            )
        )

        // 11. Execute Social Media Task
        list.put(
            ToolDeclHelper.functionDecl(
                name = "execute_social_media_task",
                description = "Automates an Instagram or Facebook task (post, reel, story). Pauses before publishing to ask confirmation. Only call confirm after user explicitly says yes.",
                params = mapOf(
                    "platform" to "Target platform: 'instagram' or 'facebook'",
                    "action" to "Action: 'create_post', 'create_reel', 'create_story', or 'text_post'",
                    "media_uri" to "Optional content URI of photo/video",
                    "caption" to "Caption or post text",
                    "mode" to "Mode: 'draft' or 'publish'",
                    "target_account" to "Optional target account handle"
                ),
                required = listOf("platform", "action")
            )
        )

        // 12. Social Media Task Control
        list.put(
            ToolDeclHelper.functionDecl(
                name = "social_media_task_control",
                description = "Controls a pending social media task: 'confirm', 'reject', 'resume', 'cancel', or 'status'. Only call confirm after the user explicitly said yes.",
                params = mapOf(
                    "command" to "Command to execute: confirm, reject, resume, cancel, or status",
                    "task_id" to "ID of the social task"
                ),
                required = listOf("command", "task_id")
            )
        )

        // 13. Execute WhatsApp Task
        list.put(
            ToolDeclHelper.functionDecl(
                name = "execute_whatsapp_task",
                description = "Controls WhatsApp actions: 'read_unread', 'send_message', 'read_chat', 'search_chat', 'mute_chat', 'unmute_chat', or 'mark_read'.",
                params = mapOf(
                    "action" to "Action to perform",
                    "contact" to "Contact name or phone number",
                    "message" to "Message body if sending"
                ),
                required = listOf("action")
            )
        )

        // 14. WhatsApp Task Control
        list.put(
            ToolDeclHelper.functionDecl(
                name = "whatsapp_task_control",
                description = "Controls or confirms a WhatsApp automated operation.",
                params = mapOf(
                    "command" to "Command: confirm, reject, cancel, or status",
                    "task_id" to "Task ID"
                ),
                required = listOf("command", "task_id")
            )
        )

        return list
    }
}
