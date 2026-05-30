package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.models.CityInfo
import com.example.models.PakistanCities
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.*

object LocationHelper {

    private suspend fun <T> Task<T>.awaitTask(): T? = suspendCancellableCoroutine { continuation ->
        this.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resume(null)
            }
        }
        this.addOnFailureListener {
            continuation.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun detectClosestCity(context: Context): CityInfo? {
        return try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            
            // Query for high accuracy current location
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location? = fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).awaitTask()

            if (location != null) {
                findNearestCity(location.latitude, location.longitude)
            } else {
                // Fallback to last known location
                val lastLocation = fusedClient.lastLocation.awaitTask()
                if (lastLocation != null) {
                    findNearestCity(lastLocation.latitude, lastLocation.longitude)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun findNearestCity(latitude: Double, longitude: Double): CityInfo {
        var closestCity = PakistanCities.list[0]
        var minDistance = Double.MAX_VALUE

        for (city in PakistanCities.list) {
            val dist = calculateDistance(latitude, longitude, city.latitude, city.longitude)
            if (dist < minDistance) {
                minDistance = dist
                closestCity = city
            }
        }
        return closestCity
    }

    // Haversine distance formula to accurately map coordinates to Pakistani cities
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
