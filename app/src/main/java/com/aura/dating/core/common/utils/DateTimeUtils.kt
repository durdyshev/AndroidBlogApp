package com.aura.dating.core.common.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.parse(isoString.take(19))?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun formatRelativeTime(timeMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeMillis

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
            else -> {
                val format = SimpleDateFormat("MMM d", Locale.getDefault())
                format.format(Date(timeMillis))
            }
        }
    }

    fun formatMessageTime(timeMillis: Long): String {
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        return format.format(Date(timeMillis))
    }

    fun formatMatchedTime(timeMillis: Long): String {
        val format = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return format.format(Date(timeMillis))
    }
}
