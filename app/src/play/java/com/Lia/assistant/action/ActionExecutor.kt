package com.Lia.assistant.action

import android.content.Context
import org.json.JSONObject

object ActionExecutor {

    suspend fun execute(context: Context, name: String, args: JSONObject): JSONObject {
        val result = JSONObject()
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

                val success = if (platform == "sms") {
                    CommIntents.openSms(context, resolvedNumber, message)
                } else {
                    CommIntents.openWhatsApp(context, resolvedNumber, message)
                }

                if (success) {
                    result.put("status", "composer_opened")
                    result.put("recipient", recipient)
                    result.put("platform", platform)
                    result.put("message_preview", message.take(40))
                } else {
                    result.put("status", "failed_to_open_composer")
                }
            }

            else -> {
                result.put("error", "Unknown tool: $name")
            }
        }
        return result
    }
}
