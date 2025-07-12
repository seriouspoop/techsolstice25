package com.example.financetracker.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

object DateUtils {

    fun getStartOfMonthTimestamp(): Long {
        val now = LocalDateTime.now()
        val startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0)
        return startOfMonth.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getEndOfMonthTimestamp(): Long {
        val now = LocalDateTime.now()
        val endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999)
        return endOfMonth.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun formatTimestampToDate(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return dateTime.format(formatter)
    }
}