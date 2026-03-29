package com.cirin0.worktimetracker.core.utils

import androidx.core.net.toUri

object UrlNormalizer {
    fun normalizeRemoteHttpToHttps(url: String?, activeDomain: String): String? {
        if (url.isNullOrBlank()) return url

        val normalizedUrl = url.trim()
        val parsedUrl = normalizedUrl.toUri()
        if (!parsedUrl.isAbsolute) return normalizedUrl

        val scheme = parsedUrl.scheme?.lowercase()
        if (scheme != "http") return normalizedUrl

        val activeHost = activeDomain.toUri().host?.lowercase() ?: return normalizedUrl
        val urlHost = parsedUrl.host?.lowercase() ?: return normalizedUrl

        if (urlHost != activeHost) return normalizedUrl

        return parsedUrl.buildUpon()
            .scheme("https")
            .build()
            .toString()
    }
}

