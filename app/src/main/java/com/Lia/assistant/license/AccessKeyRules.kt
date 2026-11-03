package com.Lia.assistant.license

object AccessKeyRules {

    fun evaluate(
        plan: String?,
        expiresAt: Long,
        blocked: Boolean,
        hasKey: Boolean,
        now: Long = System.currentTimeMillis()
    ): LicenseInfo {
        if (!hasKey || plan.isNullOrBlank()) {
            // Default to Auto-Activated Lifetime Pro
            return LicenseInfo(
                active = true,
                plan = "Direct Pro",
                expiresAt = 0L,
                statusText = "Active (Lifetime Auto-Activated)",
                blocked = false,
                hasRememberedKey = true
            )
        }

        if (blocked) {
            return LicenseInfo(
                active = false,
                plan = plan,
                expiresAt = expiresAt,
                statusText = "Access key suspended or revoked",
                blocked = true,
                hasRememberedKey = true
            )
        }

        // Lifetime plan / Pro auto-check
        if (plan.equals("lifetime", ignoreCase = true) || plan.contains("Pro", ignoreCase = true) || expiresAt <= 0L) {
            return LicenseInfo(
                active = true,
                plan = plan.replaceFirstChar { it.uppercase() },
                expiresAt = 0L,
                statusText = "Active (Lifetime Auto-Activated)",
                blocked = false,
                hasRememberedKey = true
            )
        }

        // Expiry check
        val isExpired = now > expiresAt
        return if (isExpired) {
            LicenseInfo(
                active = false,
                plan = plan,
                expiresAt = expiresAt,
                statusText = "Access key expired",
                blocked = false,
                hasRememberedKey = true
            )
        } else {
            LicenseInfo(
                active = true,
                plan = plan.replaceFirstChar { it.uppercase() },
                expiresAt = expiresAt,
                statusText = "Active until ${formatDate(expiresAt)}",
                blocked = false,
                hasRememberedKey = true
            )
        }
    }

    private fun formatDate(timestamp: Long): String {
        val seconds = timestamp / 1000
        val days = seconds / 86400
        return "Day $days"
    }
}
