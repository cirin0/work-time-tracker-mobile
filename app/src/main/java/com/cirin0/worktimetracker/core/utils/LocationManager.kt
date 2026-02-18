package com.cirin0.worktimetracker.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class GpsLocationData(
    val latitude: Double,
    val longitude: Double
)

sealed class GpsLocationResult {
    data class Success(val location: GpsLocationData) : GpsLocationResult()
    data class Error(val message: String) : GpsLocationResult()
    object PermissionDenied : GpsLocationResult()
}

@Singleton
class LocationManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): GpsLocationResult {
        if (!hasLocationPermission()) {
            return GpsLocationResult.PermissionDenied
        }

        return try {
            suspendCancellableCoroutine { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            continuation.resume(
                                GpsLocationResult.Success(
                                    GpsLocationData(
                                        latitude = location.latitude,
                                        longitude = location.longitude
                                    )
                                )
                            )
                        } else {
                            // If last location is null, request a fresh location
                            requestFreshLocation { result ->
                                continuation.resume(result)
                            }
                        }
                    }
                    .addOnFailureListener { exception: Exception ->
                        continuation.resume(
                            GpsLocationResult.Error("Failed to get location: ${exception.message}")
                        )
                    }
            }
        } catch (_: SecurityException) {
            GpsLocationResult.Error("Location permission denied")
        } catch (e: Exception) {
            GpsLocationResult.Error("Error getting location: ${e.message}")
        }
    }

    private fun requestFreshLocation(callback: (GpsLocationResult) -> Unit) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds
        ).apply {
            setMinUpdateIntervalMillis(5000L) // 5 seconds
            setMaxUpdates(1)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    callback(
                        GpsLocationResult.Success(
                            GpsLocationData(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    )
                } else {
                    callback(GpsLocationResult.Error("Unable to get location"))
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            callback(GpsLocationResult.Error("Location permission denied"))
        }
    }

    fun observeLocationUpdates(): Flow<GpsLocationResult> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(GpsLocationResult.PermissionDenied)
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).apply {
            setMinUpdateIntervalMillis(5000L)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    trySend(
                        GpsLocationResult.Success(
                            GpsLocationData(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    )
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            trySend(GpsLocationResult.Error("Location permission denied"))
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}


