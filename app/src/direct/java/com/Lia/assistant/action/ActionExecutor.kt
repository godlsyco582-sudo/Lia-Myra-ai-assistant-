package com.Lia.assistant.action

import android.content.Context
import com.Lia.assistant.agent.core.NovaAccessibilityService
import com.Lia.assistant.license.AccessKeyManager
import org.json.JSONArray
import org.json.JSONObject

object ActionExecutor {

    suspend fun execute(context: Context, name: String, args: JSONObject): JSONObject {
        val result = JSONObject()

        if (!AccessKeyManager.isLicensed(context)) {
            result.put("status", "license_inactive")
            result.put("error", "Direct actions require an active access key. Please activate in your Profile.")
            return result
        }

        when (name) {
            "open_app" -> {
                val appName = args.optString("app_name", "").trim()
                if (appName.isBlank()) {
                    result.put("status", "failed")
                    result.put("error", "No app name provided")
                    return result
                }

                val pkg = AppLauncher.findApp(context, appName)
                if (pkg != null && AppLauncher.launch(context, pkg)) {
                    result.put("status", "app_opened")
                    result.put("app", appName)
                    result.put("package", pkg)
                } else {
                    result.put("status", "app_not_found")
                    result.put("app", appName)
                }
            }

            "call_contact" -> {
                val target = args.optString("name_or_number", "").trim()
                if (target.isBlank()) {
                    result.put("status", "failed")
                    result.put("error", "No contact name or phone number provided")
                    return result
                }

                when (val lookup = ContactResolver.lookup(context, target)) {
                    is ContactLookupResult.Found -> {
                        val hasPerm = CommIntents.hasCallPermission(context)
                        if (hasPerm) {
                            CommIntents.callDirect(context, lookup.number)
                            result.put("status", "calling")
                        } else {
                            CommIntents.openDialer(context, lookup.number)
                            result.put("status", "dialer_opened_no_permission")
                        }
                        result.put("name", lookup.name)
                        result.put("number", lookup.number)
                    }
                    is ContactLookupResult.NoPermission -> {
                        CommIntents.openDialer(context, target)
                        result.put("status", "dialer_opened_no_permission")
                        result.put("target", target)
                    }
                    is ContactLookupResult.NotFound -> {
                        result.put("status", "contact_not_found")
                        result.put("target", target)
                    }
                }
            }

            "message_contact" -> {
                val recipient = args.optString("recipient", "").trim()
                val message = args.optString("message", "").trim()
                val platform = args.optString("platform", "whatsapp").lowercase()

                val resolvedNumber = when (val lookup = ContactResolver.lookup(context, recipient)) {
                    is ContactLookupResult.Found -> lookup.number
                    else -> recipient
                }

                val opened = if (platform == "sms") {
                    CommIntents.openSms(context, resolvedNumber, message)
                } else {
                    CommIntents.openWhatsApp(context, resolvedNumber, message)
                }

                if (!opened) {
                    result.put("status", "failed_to_open_composer")
                    return result
                }

                // If accessibility service is active on direct build, attempt automatic send
                val service = NovaAccessibilityService.instance
                if (service != null && platform == "whatsapp") {
                    val sent = service.autoSendWhatsAppMessage()
                    if (sent) {
                        result.put("status", "sent")
                    } else {
                        result.put("status", "composer_opened_send_not_found")
                    }
                } else {
                    result.put("status", "composer_opened")
                }
                result.put("recipient", recipient)
                result.put("platform", platform)
            }

            "read_screen" -> {
                val service = NovaAccessibilityService.instance
                if (service == null) {
                    result.put("status", "accessibility_not_enabled")
                    result.put("error", "Accessibility service is not turned on. Enable it in Settings.")
                    return result
                }
                val elements = service.readScreen()
                val array = JSONArray()
                elements.forEach { array.put(it) }
                result.put("status", "success")
                result.put("elements", array)
            }

            "tap_text" -> {
                val text = args.optString("text", "").trim()
                val service = NovaAccessibilityService.instance
                if (service == null) {
                    result.put("status", "accessibility_not_enabled")
                    return result
                }
                val tapped = service.tapText(text)
                if (tapped) {
                    result.put("status", "tapped")
                    result.put("target", text)
                } else {
                    result.put("status", "element_not_found")
                    result.put("target", text)
                }
            }

            "type_text" -> {
                val text = args.optString("text", "")
                val service = NovaAccessibilityService.instance
                if (service == null) {
                    result.put("status", "accessibility_not_enabled")
                    return result
                }
                val typed = service.typeText(text)
                if (typed) {
                    result.put("status", "typed")
                } else {
                    result.put("status", "no_focused_field")
                }
            }

            "scroll_screen" -> {
                val dir = args.optString("direction", "down")
                val service = NovaAccessibilityService.instance
                if (service == null) {
                    result.put("status", "accessibility_not_enabled")
                    return result
                }
                val scrolled = service.scroll(dir)
                if (scrolled) {
                    result.put("status", "scrolled")
                    result.put("direction", dir)
                } else {
                    result.put("status", "nothing_scrollable")
                }
            }

            "go_back" -> {
                val service = NovaAccessibilityService.instance
                if (service == null) {
                    result.put("status", "accessibility_not_enabled")
                    return result
                }
                val ok = service.goBack()
                result.put("status", if (ok) "done" else "failed")
            }

            "go_home" -> {
                val service = NovaAccessibilityService.instance
                if (service == null) {
                    result.put("status", "accessibility_not_enabled")
                    return result
                }
                val ok = service.goHome()
                result.put("status", if (ok) "done" else "failed")
            }

            "execute_social_media_task", "social_media_task_control",
            "execute_whatsapp_task", "whatsapp_task_control" -> {
                result.put("status", "waiting_for_user_confirmation")
                result.put("message", "Task queued. Please confirm before publishing or sending.")
            }

            else -> {
                result.put("error", "Unknown tool: $name")
            }
        }
        return result
    }
}
