package com.example.sensor

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.example.model.CoordinateFormat
import com.example.model.UnitSystem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.abs

data class GpsLocationData(
    val latitude: Double = -8.6500, // Default to Kuripan, Lombok Barat (Developer base)
    val longitude: Double = 116.1400,
    val altitudeMeters: Double = 120.0,
    val speedMps: Float = 0f,
    val bearingDegrees: Float = 0f,
    val accuracyMeters: Float = 3.5f,
    val isSimulated: Boolean = false,
    val hasGpsFix: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

class LocationServiceManager(private val context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationData = MutableStateFlow(GpsLocationData())
    val locationData: StateFlow<GpsLocationData> = _locationData.asStateFlow()

    private var isTracking = false
    private var isSimulationMode = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (isSimulationMode) return
            val loc = result.lastLocation ?: return
            _locationData.value = GpsLocationData(
                latitude = loc.latitude,
                longitude = loc.longitude,
                altitudeMeters = if (loc.hasAltitude()) loc.altitude else _locationData.value.altitudeMeters,
                speedMps = if (loc.hasSpeed()) loc.speed else 0f,
                bearingDegrees = if (loc.hasBearing()) loc.bearing else _locationData.value.bearingDegrees,
                accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else 5f,
                isSimulated = false,
                hasGpsFix = true,
                timestamp = loc.time
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (isTracking) return
        isTracking = true
        try {
            val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(1.0f)
                .build()

            fusedClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())
            fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null && !isSimulationMode) {
                    _locationData.value = GpsLocationData(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        altitudeMeters = if (loc.hasAltitude()) loc.altitude else 120.0,
                        speedMps = if (loc.hasSpeed()) loc.speed else 0f,
                        bearingDegrees = if (loc.hasBearing()) loc.bearing else 0f,
                        accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else 4f,
                        isSimulated = false,
                        hasGpsFix = true,
                        timestamp = loc.time
                    )
                }
            }
        } catch (_: SecurityException) {
            // Permission not yet granted, fallback to default Kuripan, Lombok coordinates
        }
    }

    fun stopLocationUpdates() {
        if (!isTracking) return
        isTracking = false
        try {
            fusedClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
    }

    fun setSimulationMode(enabled: Boolean) {
        isSimulationMode = enabled
        if (!enabled) {
            startLocationUpdates()
        } else {
            val curr = _locationData.value
            _locationData.value = curr.copy(isSimulated = true)
        }
    }

    fun updateSimulatedPosition(
        lat: Double,
        lon: Double,
        alt: Double = 150.0,
        speed: Float = 1.4f, // ~5 km/h walking speed
        bearing: Float = 45f
    ) {
        _locationData.value = GpsLocationData(
            latitude = lat,
            longitude = lon,
            altitudeMeters = alt,
            speedMps = speed,
            bearingDegrees = bearing,
            accuracyMeters = 2.0f,
            isSimulated = true,
            hasGpsFix = true,
            timestamp = System.currentTimeMillis()
        )
    }

    companion object {
        fun formatCoordinates(
            lat: Double,
            lon: Double,
            format: CoordinateFormat = CoordinateFormat.DECIMAL_DEGREES
        ): String {
            return when (format) {
                CoordinateFormat.DECIMAL_DEGREES -> {
                    val latHem = if (lat >= 0) "N" else "S"
                    val lonHem = if (lon >= 0) "E" else "W"
                    String.format(Locale.US, "%.5f° %s, %.5f° %s", abs(lat), latHem, abs(lon), lonHem)
                }
                CoordinateFormat.DEGREES_MINUTES_SECONDS -> {
                    toDms(lat, isLatitude = true) + " " + toDms(lon, isLatitude = false)
                }
            }
        }

        private fun toDms(coordinate: Double, isLatitude: Boolean): String {
            val absVal = abs(coordinate)
            val degrees = absVal.toInt()
            val minutesDouble = (absVal - degrees) * 60
            val minutes = minutesDouble.toInt()
            val seconds = (minutesDouble - minutes) * 60
            val direction = if (isLatitude) {
                if (coordinate >= 0) "N" else "S"
            } else {
                if (coordinate >= 0) "E" else "W"
            }
            return String.format(Locale.US, "%d°%02d'%04.1f\"%s", degrees, minutes, seconds, direction)
        }

        fun formatDistance(meters: Double, unit: UnitSystem): String {
            return when (unit) {
                UnitSystem.METRIC -> {
                    if (meters >= 1000) {
                        String.format(Locale.US, "%.2f km", meters / 1000.0)
                    } else {
                        String.format(Locale.US, "%.0f m", meters)
                    }
                }
                UnitSystem.NAUTICAL -> {
                    val nm = meters / 1852.0
                    String.format(Locale.US, "%.2f NM", nm)
                }
                UnitSystem.IMPERIAL -> {
                    val miles = meters / 1609.344
                    if (miles >= 0.1) {
                        String.format(Locale.US, "%.2f mi", miles)
                    } else {
                        val feet = meters * 3.28084
                        String.format(Locale.US, "%.0f ft", feet)
                    }
                }
            }
        }

        fun formatSpeed(speedMps: Float, unit: UnitSystem): String {
            return when (unit) {
                UnitSystem.METRIC -> {
                    val kmh = speedMps * 3.6f
                    String.format(Locale.US, "%.1f km/h", kmh)
                }
                UnitSystem.NAUTICAL -> {
                    val knots = speedMps * 1.94384f
                    String.format(Locale.US, "%.1f kts", knots)
                }
                UnitSystem.IMPERIAL -> {
                    val mph = speedMps * 2.23694f
                    String.format(Locale.US, "%.1f mph", mph)
                }
            }
        }

        fun formatElevation(meters: Double, unit: UnitSystem): String {
            return when (unit) {
                UnitSystem.IMPERIAL -> {
                    val ft = meters * 3.28084
                    String.format(Locale.US, "%.0f ft", ft)
                }
                else -> {
                    String.format(Locale.US, "%.1f m", meters)
                }
            }
        }
    }
}
