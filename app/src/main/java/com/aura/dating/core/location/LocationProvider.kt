package com.aura.dating.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class UserCoordinates(
    val latitude: Double,
    val longitude: Double
)

interface LocationProvider {
    fun hasLocationPermission(): Boolean
    suspend fun getLastKnownLocation(): UserCoordinates?
    suspend fun getCurrentLocation(): UserCoordinates?
    fun shouldUpdateLocation(lastLocation: UserCoordinates?, newLocation: UserCoordinates, lastUpdatedMillis: Long): Boolean
}

@Singleton
class FusedLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationProvider {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    override suspend fun getLastKnownLocation(): UserCoordinates? {
        if (!hasLocationPermission()) return null
        return try {
            val location: Location? = fusedLocationClient.lastLocation.await()
            location?.let { UserCoordinates(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCurrentLocation(): UserCoordinates? {
        if (!hasLocationPermission()) return null
        return try {
            val cts = CancellationTokenSource()
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cts.token
            ).await()
            location?.let { UserCoordinates(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }

    override fun shouldUpdateLocation(
        lastLocation: UserCoordinates?,
        newLocation: UserCoordinates,
        lastUpdatedMillis: Long
    ): Boolean {
        if (lastLocation == null) return true

        val now = System.currentTimeMillis()
        val timeDiffMillis = now - lastUpdatedMillis
        // Update if more than 30 minutes have passed
        if (timeDiffMillis > 30 * 60 * 1000) return true

        // Or if user moved more than 500 meters
        val results = FloatArray(1)
        Location.distanceBetween(
            lastLocation.latitude,
            lastLocation.longitude,
            newLocation.latitude,
            newLocation.longitude,
            results
        )
        return results[0] > 500f
    }
}
