package com.kkc.clubconnect.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtils {

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val monthChipFormatter = DateTimeFormatter.ofPattern("MMM dd")
    private val cardFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM | h:mm a")
    private val detailFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy | h:mm a")
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    fun toMonthChip(millis: Long): String = monthChipFormatter.format(Instant.ofEpochMilli(millis).atZone(zoneId))

    fun toCardLabel(millis: Long): String = cardFormatter.format(Instant.ofEpochMilli(millis).atZone(zoneId))

    fun toFullLabel(millis: Long): String = detailFormatter.format(Instant.ofEpochMilli(millis).atZone(zoneId))

    fun toRange(startMillis: Long, endMillis: Long): String {
        val start = Instant.ofEpochMilli(startMillis).atZone(zoneId)
        val end = Instant.ofEpochMilli(endMillis).atZone(zoneId)

        return if (start.toLocalDate() == end.toLocalDate()) {
            "${detailFormatter.format(start)} - ${timeFormatter.format(end)}"
        } else {
            "${detailFormatter.format(start)} - ${detailFormatter.format(end)}"
        }
    }
}
