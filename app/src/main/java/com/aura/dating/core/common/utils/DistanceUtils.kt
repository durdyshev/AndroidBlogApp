package com.aura.dating.core.common.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceUtils {

    private const val EARTH_RADIUS_KM = 6371.0

    fun calculateHaversineDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(rLat1) * cos(rLat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    fun formatDistance(distanceKm: Double?, showDistance: Boolean = true): String {
        if (!showDistance || distanceKm == null) return "Nearby"
        return when {
            distanceKm < 1.0 -> "Less than 1 km away"
            distanceKm < 10.0 -> String.format(java.util.Locale.US, "%.1f km away", distanceKm)
            else -> "${distanceKm.toInt()} km away"
        }
    }
}
