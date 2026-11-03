package com.Lia.assistant.action

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

sealed class ContactLookupResult {
    data class Found(val name: String, val number: String) : ContactLookupResult()
    object NoPermission : ContactLookupResult()
    object NotFound : ContactLookupResult()
}

object ContactResolver {

    fun isPhoneNumber(query: String): Boolean {
        val clean = query.filter { it.isDigit() }
        val allowedChars = query.all { it.isDigit() || it in " +()-." }
        return allowedChars && clean.length >= 5
    }

    fun lookup(context: Context, rawQuery: String): ContactLookupResult {
        val query = rawQuery.trim()
        if (query.isBlank()) return ContactLookupResult.NotFound

        if (isPhoneNumber(query)) {
            val digits = query.filter { it.isDigit() || it == '+' }
            return ContactLookupResult.Found(name = query, number = digits)
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return ContactLookupResult.NoPermission
        }

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC LIMIT 5"

        val matches = mutableListOf<Pair<String, String>>() // name, number

        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val name = if (nameIndex >= 0) it.getString(nameIndex) ?: "" else ""
                    val number = if (numberIndex >= 0) it.getString(numberIndex) ?: "" else ""
                    if (name.isNotBlank() && number.isNotBlank()) {
                        matches.add(name to number)
                    }
                }
            }
        } catch (_: Exception) {
            return ContactLookupResult.NotFound
        }

        if (matches.isEmpty()) return ContactLookupResult.NotFound

        val lowerQuery = query.lowercase()
        val best = matches.minByOrNull { (name, _) ->
            val lower = name.lowercase()
            when {
                lower == lowerQuery -> 0
                lower.startsWith(lowerQuery) -> 1
                else -> 2
            }
        } ?: matches.first()

        return ContactLookupResult.Found(name = best.first, number = best.second)
    }
}
