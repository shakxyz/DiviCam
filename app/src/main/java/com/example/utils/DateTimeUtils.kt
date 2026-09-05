package com.example.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun formatTimestamp(date: Date, dateFormat: String, is24h: Boolean): String {
        val datePattern = when (dateFormat) {
            "MM/DD/YYYY" -> "MM/dd/yyyy"
            "YYYY-MM-DD" -> "yyyy-MM-dd"
            else -> "dd/MM/yyyy" // Default DD/MM/YYYY
        }

        val timePattern = if (is24h) "HH:mm:ss" else "hh:mm:ss a"
        val fullPattern = "$datePattern  $timePattern"

        return try {
            val sdf = SimpleDateFormat(fullPattern, Locale.getDefault())
            sdf.format(date)
        } catch (e: Exception) {
            val sdfFallback = SimpleDateFormat("dd/MM/yyyy  HH:mm:ss", Locale.getDefault())
            sdfFallback.format(date)
        }
    }
}
