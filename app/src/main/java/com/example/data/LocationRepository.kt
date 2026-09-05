package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val formattedCoordinates: String
)

class LocationRepository(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): LocationData? = withContext(Dispatchers.IO) {
        try {
            // Check for basic permission check (can be outer handled, but double check)
            val hasFine = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarse = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) {
                return@withContext null
            }

            // Get last location
            val locationTask = fusedLocationClient.lastLocation
            val lastLoc: Location? = try {
                Tasks.await(locationTask)
            } catch (e: Exception) {
                null
            }

            // If last location is null, try to get current location
            val finalLoc: Location? = lastLoc ?: try {
                val currentTask = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    null
                )
                Tasks.await(currentTask)
            } catch (e: Exception) {
                null
            }

            if (finalLoc != null) {
                val lat = finalLoc.latitude
                val lng = finalLoc.longitude
                
                val absLat = Math.abs(lat)
                val latDeg = absLat.toInt()
                val latMinDec = (absLat - latDeg) * 60.0
                val latMin = latMinDec.toInt()
                val latSec = Math.round((latMinDec - latMin) * 60.0).toInt().coerceIn(0, 59)
                val latHemisphere = if (lat >= 0) "N" else "S"
                
                val absLng = Math.abs(lng)
                val lngDeg = absLng.toInt()
                val lngMinDec = (absLng - lngDeg) * 60.0
                val lngMin = lngMinDec.toInt()
                val lngSec = Math.round((lngMinDec - lngMin) * 60.0).toInt().coerceIn(0, 59)
                val lngHemisphere = if (lng >= 0) "E" else "W"
                
                val latStr = "%02d%02d%02d%s".format(Locale.US, latDeg, latMin, latSec, latHemisphere)
                val lngStr = "%02d.%02d%02d%s".format(Locale.US, lngDeg, lngMin, lngSec, lngHemisphere)
                val coordsStr = "$latStr $lngStr"
                
                var addressStr = "Address unavailable"
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // In Tiramisu+ geocoder can use listener, but await is blocking. Let's do a fallback synchronous for simplicity as it runs on Dispatchers.IO
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            addressStr = addresses[0].getAddressLine(0) ?: "Address unavailable"
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            addressStr = addresses[0].getAddressLine(0) ?: "Address unavailable"
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LocationRepository", "Error geocoding location", e)
                }

                return@withContext LocationData(
                    latitude = lat,
                    longitude = lng,
                    address = addressStr,
                    formattedCoordinates = coordsStr
                )
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error fetching location", e)
        }
        return@withContext null
    }
}
