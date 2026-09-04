package com.aura.dating.core.utils

import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.common.utils.DistanceUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DateTimeAndDistanceUtilsTest {

    @Test
    fun `calculateAge returns correct age based on birthdate`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -24)
        val birthMillis = calendar.timeInMillis

        val age = DateTimeUtils.calculateAge(birthMillis)
        assertEquals(24, age)
    }

    @Test
    fun `calculateHaversineDistanceKm computes accurate distance between two GPS coordinates`() {
        // Paris: 48.8566, 2.3522
        // London: 51.5074, -0.1278
        val distance = DistanceUtils.calculateHaversineDistanceKm(48.8566, 2.3522, 51.5074, -0.1278)
        assertTrue(distance in 340.0..345.0)
    }

    @Test
    fun `formatDistance returns user-friendly approximate strings`() {
        assertEquals("Less than 1 km away", DistanceUtils.formatDistance(0.4))
        assertEquals("2.4 km away", DistanceUtils.formatDistance(2.4))
        assertEquals("15 km away", DistanceUtils.formatDistance(15.2))
        assertEquals("Nearby", DistanceUtils.formatDistance(null))
        assertEquals("Nearby", DistanceUtils.formatDistance(2.4, showDistance = false))
    }
}
