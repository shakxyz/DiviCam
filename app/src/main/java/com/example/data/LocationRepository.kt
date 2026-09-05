package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val bearing: Float = 0f,
    val address: String,
    val formattedCoordinates: String
)

class LocationRepository(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    companion object {
        @Volatile
        private var cachedLocation: LocationData? = null

        fun getBearingDirection(bearing: Float): String {
            val normalized = ((bearing % 360) + 360) % 360
            return when {
                normalized >= 337.5 || normalized < 22.5 -> "N"
                normalized >= 22.5 && normalized < 67.5 -> "NE"
                normalized >= 67.5 && normalized < 112.5 -> "E"
                normalized >= 112.5 && normalized < 157.5 -> "SE"
                normalized >= 157.5 && normalized < 202.5 -> "S"
                normalized >= 202.5 && normalized < 247.5 -> "SW"
                normalized >= 247.5 && normalized < 292.5 -> "W"
                else -> "NW"
            }
        }

        fun formatCoordinates(
            lat: Double,
            lng: Double,
            altitude: Double = 0.0,
            bearing: Float = 0f,
            format: String = "Decimal",
            precision: Int = 5,
            prefix: String = "GPS",
            showAltitude: Boolean = false,
            altitudeUnit: String = "Meters (m)",
            showHeading: Boolean = false,
            useCardinal: Boolean = true
        ): String {
            val p = precision.coerceIn(2, 6)
            val absLat = abs(lat)
            val absLng = abs(lng)
            val latHem = if (lat >= 0) "N" else "S"
            val lngHem = if (lng >= 0) "E" else "W"

            val latDeg = absLat.toInt()
            val latMinDec = (absLat - latDeg) * 60.0
            val latMin = latMinDec.toInt()
            val latSec = Math.round((latMinDec - latMin) * 60.0).toInt().coerceIn(0, 59)

            val lngDeg = absLng.toInt()
            val lngMinDec = (absLng - lngDeg) * 60.0
            val lngMin = lngMinDec.toInt()
            val lngSec = Math.round((lngMinDec - lngMin) * 60.0).toInt().coerceIn(0, 59)

            val baseCoords = when (format) {
                "DMS" -> {
                    if (useCardinal) {
                        String.format(
                            Locale.US,
                            "%d°%02d'%02d\"%s %d°%02d'%02d\"%s",
                            latDeg, latMin, latSec, latHem,
                            lngDeg, lngMin, lngSec, lngHem
                        )
                    } else {
                        val signLat = if (lat >= 0) "+" else "-"
                        val signLng = if (lng >= 0) "+" else "-"
                        String.format(
                            Locale.US,
                            "%s%d°%02d'%02d\" %s%d°%02d'%02d\"",
                            signLat, latDeg, latMin, latSec,
                            signLng, lngDeg, lngMin, lngSec
                        )
                    }
                }
                "Compact" -> {
                    String.format(Locale.US, "%.${p}f, %.${p}f", lat, lng)
                }
                "Grid" -> {
                    String.format(Locale.US, "LAT %.${p}f | LON %.${p}f", lat, lng)
                }
                "Short" -> {
                    if (useCardinal) {
                        String.format(Locale.US, "%.2f°%s / %.2f°%s", absLat, latHem, absLng, lngHem)
                    } else {
                        String.format(Locale.US, "%.2f / %.2f", lat, lng)
                    }
                }
                else -> { // "Decimal"
                    if (useCardinal) {
                        String.format(
                            Locale.US,
                            "%.${p}f° %s, %.${p}f° %s",
                            absLat, latHem, absLng, lngHem
                        )
                    } else {
                        val signLat = if (lat >= 0) "+" else ""
                        val signLng = if (lng >= 0) "+" else ""
                        String.format(
                            Locale.US,
                            "%s%.${p}f°, %s%.${p}f°",
                            signLat, lat, signLng, lng
                        )
                    }
                }
            }

            val sb = StringBuilder()
            when (prefix) {
                "GPS" -> sb.append("GPS: ")
                "LAT/LON" -> sb.append("LAT/LON: ")
                "COORD" -> sb.append("COORD: ")
                "None" -> { /* No prefix */ }
                else -> sb.append("$prefix: ")
            }
            sb.append(baseCoords)

            if (showAltitude && altitude != 0.0) {
                if (altitudeUnit.contains("Feet")) {
                    val feet = (altitude * 3.28084).toInt()
                    sb.append(" • Elev: ${feet}ft")
                } else {
                    sb.append(" • Elev: ${altitude.toInt()}m")
                }
            }

            if (showHeading && bearing > 0f) {
                val dir = getBearingDirection(bearing)
                sb.append(" • Hdg: ${bearing.toInt()}° $dir")
            }

            return sb.toString()
        }
    }

    fun getCachedLocation(): LocationData? = cachedLocation

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(settings: SettingsManager? = null): LocationData? = withContext(Dispatchers.IO) {
        try {
            // Check if user set a manual coordinate override in settings
            val manual = settings?.manualCoordsOverride?.trim()
            if (!manual.isNullOrEmpty()) {
                val parts = manual.split(",", ";", " ").filter { it.isNotBlank() }
                if (parts.size >= 2) {
                    val mLat = parts[0].toDoubleOrNull()
                    val mLng = parts[1].toDoubleOrNull()
                    if (mLat != null && mLng != null) {
                        val formatted = formatCoordinates(
                            lat = mLat,
                            lng = mLng,
                            altitude = 0.0,
                            bearing = 0f,
                            format = settings.coordFormat,
                            precision = settings.coordPrecision,
                            prefix = settings.coordPrefix,
                            showAltitude = settings.showAltitude,
                            altitudeUnit = settings.altitudeUnit,
                            showHeading = settings.showCompassHeading,
                            useCardinal = settings.useCardinalDirections
                        )
                        val manualData = LocationData(
                            latitude = mLat,
                            longitude = mLng,
                            altitude = 0.0,
                            bearing = 0f,
                            address = "Custom Location Override",
                            formattedCoordinates = formatted
                        )
                        cachedLocation = manualData
                        return@withContext manualData
                    }
                }
            }

            val hasFine = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarse = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) {
                return@withContext cachedLocation
            }

            // Fast mobile location query: Check fused last location first
            var finalLoc: Location? = try {
                val task = fusedLocationClient.lastLocation
                Tasks.await(task)
            } catch (e: Exception) {
                null
            }

            // Quick device provider fallback if fused is not yet warm
            if (finalLoc == null && locationManager != null) {
                try {
                    val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    finalLoc = when {
                        gpsLoc != null && netLoc != null -> if (gpsLoc.time > netLoc.time) gpsLoc else netLoc
                        gpsLoc != null -> gpsLoc
                        else -> netLoc
                    }
                } catch (e: Exception) {
                    // Ignore fallback failure
                }
            }

            // If still null, request fresh high-speed balanced fix
            if (finalLoc == null) {
                finalLoc = try {
                    val currentTask = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        null
                    )
                    Tasks.await(currentTask)
                } catch (e: Exception) {
                    null
                }
            }

            if (finalLoc != null) {
                val lat = finalLoc.latitude
                val lng = finalLoc.longitude
                val alt = if (finalLoc.hasAltitude()) finalLoc.altitude else 0.0
                val bearing = if (finalLoc.hasBearing()) finalLoc.bearing else 0f

                val coordsStr = if (settings != null) {
                    formatCoordinates(
                        lat = lat,
                        lng = lng,
                        altitude = alt,
                        bearing = bearing,
                        format = settings.coordFormat,
                        precision = settings.coordPrecision,
                        prefix = settings.coordPrefix,
                        showAltitude = settings.showAltitude,
                        altitudeUnit = settings.altitudeUnit,
                        showHeading = settings.showCompassHeading,
                        useCardinal = settings.useCardinalDirections
                    )
                } else {
                    formatCoordinates(lat, lng, alt, bearing)
                }

                var addressStr = cachedLocation?.address ?: "Resolving address..."
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            addressStr = addresses[0].getAddressLine(0) ?: addressStr
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            addressStr = addresses[0].getAddressLine(0) ?: addressStr
                        }
                    }
                } catch (e: Exception) {
                    Log.d("LocationRepository", "Geocoding network unavailable")
                }

                val result = LocationData(
                    latitude = lat,
                    longitude = lng,
                    altitude = alt,
                    bearing = bearing,
                    address = addressStr,
                    formattedCoordinates = coordsStr
                )
                cachedLocation = result
                return@withContext result
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error fetching fast location", e)
        }
        return@withContext cachedLocation
    }
}
