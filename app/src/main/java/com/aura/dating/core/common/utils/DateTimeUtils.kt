package com.aura.dating.core.common.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    fun calculateAge(birthDateMillis: Long): Int {
        val dob = Calendar.getInstance().apply { timeInMillis = birthDateMillis }
        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age.coerceAtLeast(0)
    }

    fun parseIsoDate(isoString: String): Long {
        if (isoString.isBlank()) return System.currentTimeMillis()
        return try {
            try {
                Instant.parse(isoString).toEpochMilli()
            } catch (_: Exception) {
                val cleanIso = if (!isoString.endsWith("Z") && !isoString.contains("+")) {
                    "${isoString.take(19)}Z"
                } else {
                    isoString
                }
                try {
                    Instant.parse(cleanIso).toEpochMilli()
                } catch (_: Exception) {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    format.parse(isoString.take(19))?.time ?: System.currentTimeMillis()
                }
            }
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    fun formatRelativeTime(timeMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = (now - timeMillis).coerceAtLeast(0)

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
            else -> {
                val format = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
                format.format(Date(timeMillis))
            }
        }
    }

    fun formatMessageTime(timeMillis: Long): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(Date(timeMillis))
    }

    fun formatMatchedTime(timeMillis: Long): String {
        val format = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        return format.format(Date(timeMillis))
    }

    fun formatLastSeen(lastSeenAtMillis: Long?, isOnline: Boolean): String {
        if (isOnline) return "Online"
        if (lastSeenAtMillis == null || lastSeenAtMillis <= 0L) return "Offline"

        val now = System.currentTimeMillis()
        val diff = (now - lastSeenAtMillis).coerceAtLeast(0)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Az önce görüldü"
            minutes < 60 -> "$minutes dk önce görüldü"
            hours < 24 -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                "Bugün ${timeFormat.format(Date(lastSeenAtMillis))}"
            }
            days == 1L -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                "Dün ${timeFormat.format(Date(lastSeenAtMillis))}"
            }
            days < 7 -> "$days gün önce"
            else -> {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                dateFormat.format(Date(lastSeenAtMillis))
            }
        }
    }

    fun formatToIsoUtc(timeMillis: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return format.format(Date(timeMillis))
    }
}
