package com.kkc.clubconnect.util

import java.util.Locale

object ClubBrandingUtils {

    const val defaultBannerColorHex = "#0EA5E9"

    val commonBannerColors: List<String> = listOf(
        "#0EA5E9",
        "#10B981",
        "#F97316",
        "#EF4444",
        "#8B5CF6",
        "#14B8A6",
        "#EAB308",
        "#1F2937",
    )

    fun normalizeBannerColorHex(raw: String?): String {
        val cleaned = raw.orEmpty().trim().removePrefix("#")
        if (cleaned.length != 6 || cleaned.any { !it.isDigit() && it.lowercaseChar() !in 'a'..'f' }) {
            return defaultBannerColorHex
        }
        return "#${cleaned.uppercase(Locale.US)}"
    }

    fun bannerColorHexFromRgb(
        red: Int,
        green: Int,
        blue: Int,
    ): String = String.format(
        Locale.US,
        "#%02X%02X%02X",
        red.coerceIn(0, 255),
        green.coerceIn(0, 255),
        blue.coerceIn(0, 255),
    )

    fun rgbFromBannerColorHex(raw: String?): Triple<Int, Int, Int> {
        val normalized = normalizeBannerColorHex(raw).removePrefix("#")
        val red = normalized.substring(0, 2).toInt(16)
        val green = normalized.substring(2, 4).toInt(16)
        val blue = normalized.substring(4, 6).toInt(16)
        return Triple(red, green, blue)
    }
}
