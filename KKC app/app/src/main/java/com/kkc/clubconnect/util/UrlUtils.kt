package com.kkc.clubconnect.util

import androidx.compose.ui.platform.UriHandler

object UrlUtils {

    const val dummyRegistrationUrl = "https://clubconnect.local/register-placeholder"

    fun normalizeWebUrl(raw: String?): String? {
        val trimmed = raw.orEmpty().trim()
        if (trimmed.isBlank()) {
            return null
        }

        return if (
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    fun normalizeRegistrationUrl(raw: String?): String =
        normalizeWebUrl(raw) ?: dummyRegistrationUrl

    fun isDummyRegistrationUrl(raw: String?): Boolean {
        val normalized = normalizeWebUrl(raw)
        return normalized != null && normalized.equals(dummyRegistrationUrl, ignoreCase = true)
    }

    fun toExternalRegistrationUrl(raw: String?): String? {
        val normalized = normalizeWebUrl(raw) ?: return null
        return if (isDummyRegistrationUrl(normalized)) {
            null
        } else {
            normalized
        }
    }

    fun openUrl(uriHandler: UriHandler, url: String) {
        val normalized = normalizeWebUrl(url)
        if (normalized != null) {
            try {
                uriHandler.openUri(normalized)
            } catch (e: Exception) {
                // Ignore failure to open URL
            }
        }
    }
}
