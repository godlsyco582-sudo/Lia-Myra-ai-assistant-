package com.Lia.assistant.action

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import java.net.URLEncoder

object CommIntents {

    fun hasCallPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun callDirect(context: Context, number: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${Uri.encode(number)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openDialer(context: Context, number: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(number)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openSms(context: Context, number: String, body: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(number)}")
                putExtra("sms_body", body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openWhatsApp(context: Context, rawNumber: String, body: String): Boolean {
        return try {
            val cleanDigits = rawNumber.filter { it.isDigit() }
            val encodedBody = URLEncoder.encode(body, "UTF-8")
            val uriString = if (cleanDigits.isNotBlank()) {
                "https://wa.me/$cleanDigits?text=$encodedBody"
            } else {
                "https://wa.me/?text=$encodedBody"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val pm = context.packageManager
            val packages = listOf("com.whatsapp", "com.whatsapp.w4b")
            for (pkg in packages) {
                try {
                    pm.getPackageInfo(pkg, 0)
                    intent.setPackage(pkg)
                    break
                } catch (_: Exception) {}
            }

            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
