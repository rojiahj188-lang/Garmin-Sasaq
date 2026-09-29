package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.LiveWeatherOverlayData
import com.example.data.api.WeatherApiClient
import com.example.data.local.EmergencyContactEntity
import com.example.data.local.GarminDatabase
import com.example.data.local.GarminRepository
import com.example.data.local.LocationEntity
import com.example.data.local.TrackEntity
import com.example.model.CoordinateFormat
import com.example.model.DeviceAccessMode
import com.example.model.LocationCategory
import com.example.model.NavigationTarget
import com.example.model.UnitSystem
import com.example.model.WeatherInfo
import com.example.sensor.CompassData
import com.example.sensor.CompassSensorManager
import com.example.sensor.GpsLocationData
import com.example.sensor.LocationServiceManager
import com.example.sensor.SosDispatchState
import com.example.sensor.SosEmergencyManager
import com.example.sensor.SosSmsNotificationDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

data class HikingTrackState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val startTime: Long = 0L,
    val elapsedTimeSeconds: Long = 0L,
    val totalDistanceMeters: Double = 0.0,
    val currentElevationMeters: Double = 120.0,
    val elevationGainMeters: Double = 0.0,
    val elevationLossMeters: Double = 0.0,
    val maxElevationMeters: Double = 120.0,
    val minElevationMeters: Double = 120.0,
    val elevationHistory: List<Pair<Long, Double>> = emptyList(),
    val waypointsLogged: Int = 0
)

data class MarineState(
    val isActive: Boolean = false,
    val anchorLat: Double? = null,
    val anchorLon: Double? = null,
    val anchorRadiusMeters: Double = 50.0,
    val currentDriftMeters: Double = 0.0,
    val isAnchorAlarmTriggered: Boolean = false,
    val totalNauticalMiles: Double = 0.0,
    val sogKnots: Double = 0.0,
    val cogDegrees: Float = 0f
)

class GarminViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GarminRepository
    private val compassManager = CompassSensorManager(application)
    private val locationManager = LocationServiceManager(application)
    private val sosManager = SosEmergencyManager(application)
    private val sosDispatcher = SosSmsNotificationDispatcher(application)
    private val voiceWaypointManager = com.example.sensor.VoiceWaypointManager(application)

    init {
        val db = GarminDatabase.getInstance(application)
        repository = GarminRepository(db.garminDao())
        compassManager.start()
        locationManager.startLocationUpdates()

        voiceWaypointManager.setOnWaypointDetectedListener { name, category, notes ->
            val loc = locationData.value
            val locCategory = when (category) {
                "TREASURE" -> LocationCategory.TREASURE
                "MINERAL" -> LocationCategory.MINERAL
                "HERITAGE" -> LocationCategory.HERITAGE
                "PRIORITY_ZONE" -> LocationCategory.PRIORITY_ZONE
                "HIKING" -> LocationCategory.HIKING
                "MARINE" -> LocationCategory.MARINE
                else -> LocationCategory.WAYPOINT
            }
            saveLocation(
                title = name,
                category = locCategory,
                latitude = loc.latitude,
                longitude = loc.longitude,
                altitude = loc.altitudeMeters,
                description = notes,
                details = "Waypoint Suara: $name (${String.format(Locale.US, "%.0f mdpl", loc.altitudeMeters)})",
                priority = "TINGGI"
            )
        }

        // Auto-fetch real-time weather data when GPS location is acquired
        viewModelScope.launch {
            locationData.collect { loc ->
                if (loc.latitude != 0.0 && loc.longitude != 0.0) {
                    val dist = CompassSensorManager.calculateDistanceMeters(
                        lastWeatherFetchLat, lastWeatherFetchLon, loc.latitude, loc.longitude
                    )
                    val timeDiff = System.currentTimeMillis() - lastWeatherFetchTime
                    if (dist > 5000 || timeDiff > 600_000 || lastWeatherFetchTime == 0L) {
                        fetchRealtimeWeather(loc.latitude, loc.longitude)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compassManager.stop()
        locationManager.stopLocationUpdates()
        sosManager.stopSos()
        voiceWaypointManager.stopListening()
        hikingTimerJob?.cancel()
    }

    // Sensors & SOS & Voice
    val compassData: StateFlow<CompassData> = compassManager.compassData
    val locationData: StateFlow<GpsLocationData> = locationManager.locationData
    val barometerHpa: StateFlow<Float?> = compassManager.barometerHpa
    val sosState: StateFlow<com.example.sensor.SosState> = sosManager.sosState
    val sosDispatchState: StateFlow<SosDispatchState> = sosDispatcher.dispatchState
    val voiceState: StateFlow<com.example.sensor.VoiceCommandState> = voiceWaypointManager.voiceState

    // Database Flows
    val savedLocations: StateFlow<List<LocationEntity>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteLocations: StateFlow<List<LocationEntity>> = repository.favoriteLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyContacts: StateFlow<List<EmergencyContactEntity>> = repository.allEmergencyContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedTracks: StateFlow<List<TrackEntity>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineMapTiles: StateFlow<List<com.example.data.local.OfflineMapTileEntity>> = repository.allOfflineTiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedIndividualTilesCount: StateFlow<Int> = repository.cachedTilesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cachedIndividualTilesSizeBytes: StateFlow<Long> = repository.totalCacheSizeBytes
        .combine(MutableStateFlow(0L)) { size, _ -> size ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Real-Time Weather Overlay State
    private val _liveWeatherOverlay = MutableStateFlow(
        LiveWeatherOverlayData(
            temperatureC = 28.5,
            precipitationMm = 0.0,
            rainMm = 0.0,
            weatherCondition = "Cerah Berawan",
            weatherCode = 1,
            weatherEmoji = "🌤️",
            humidityPercent = 75,
            windSpeedKmh = 12.0,
            windDirectionDegrees = 110f,
            pressureHpa = 1012,
            isRaining = false,
            isLiveFromApi = false
        )
    )
    val liveWeatherOverlay: StateFlow<LiveWeatherOverlayData> = _liveWeatherOverlay.asStateFlow()

    private val _showWeatherMapOverlay = MutableStateFlow(true)
    val showWeatherMapOverlay: StateFlow<Boolean> = _showWeatherMapOverlay.asStateFlow()

    private var lastWeatherFetchLat = 0.0
    private var lastWeatherFetchLon = 0.0
    private var lastWeatherFetchTime = 0L

    // Settings
    // Default to LAPTOP mode per user request: "ganti mode aplikasi perangkat android ini ke Mode akses perangkat Laptop"
    private val _deviceAccessMode = MutableStateFlow(DeviceAccessMode.LAPTOP)
    val deviceAccessMode: StateFlow<DeviceAccessMode> = _deviceAccessMode.asStateFlow()

    private val _unitSystem = MutableStateFlow(UnitSystem.METRIC)
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    private val _coordinateFormat = MutableStateFlow(CoordinateFormat.DECIMAL_DEGREES)
    val coordinateFormat: StateFlow<CoordinateFormat> = _coordinateFormat.asStateFlow()

    private val _searchRadiusMeters = MutableStateFlow(2000.0) // 2km
    val searchRadiusMeters: StateFlow<Double> = _searchRadiusMeters.asStateFlow()

    private val _activeTarget = MutableStateFlow<NavigationTarget?>(null)
    val activeTarget: StateFlow<NavigationTarget?> = _activeTarget.asStateFlow()

    // Qibla State
    private var lastVibratedQiblaTime = 0L

    val qiblaBearing: StateFlow<Float> = combine(locationData) { locArray ->
        val loc = locArray[0]
        CompassSensorManager.calculateBearing(
            loc.latitude, loc.longitude,
            CompassSensorManager.KAABA_LATITUDE,
            CompassSensorManager.KAABA_LONGITUDE
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 295f)

    val qiblaDistanceKm: StateFlow<Double> = combine(locationData) { locArray ->
        val loc = locArray[0]
        val meters = CompassSensorManager.calculateDistanceMeters(
            loc.latitude, loc.longitude,
            CompassSensorManager.KAABA_LATITUDE,
            CompassSensorManager.KAABA_LONGITUDE
        )
        meters / 1000.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8650.0)

    // Hiking Tracking
    private val _hikingState = MutableStateFlow(HikingTrackState())
    val hikingState: StateFlow<HikingTrackState> = _hikingState.asStateFlow()
    private var hikingTimerJob: Job? = null
    private var lastRecordedLat = 0.0
    private var lastRecordedLon = 0.0

    // Marine Tracking
    private val _marineState = MutableStateFlow(MarineState())
    val marineState: StateFlow<MarineState> = _marineState.asStateFlow()

    // Dynamic Weather State
    val weatherInfo: StateFlow<WeatherInfo> = combine(locationData) { locArray ->
        val loc = locArray[0]
        calculateEnvironmentWeather(loc.latitude, loc.longitude, loc.altitudeMeters)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        calculateEnvironmentWeather(-8.65, 116.14, 120.0)
    )

    fun setDeviceAccessMode(mode: DeviceAccessMode) {
        _deviceAccessMode.value = mode
    }

    fun setUnitSystem(system: UnitSystem) {
        _unitSystem.value = system
    }

    fun setCoordinateFormat(format: CoordinateFormat) {
        _coordinateFormat.value = format
    }

    fun setSearchRadius(meters: Double) {
        _searchRadiusMeters.value = meters
    }

    fun setActiveTarget(target: NavigationTarget?) {
        _activeTarget.value = target
    }

    fun setSimulationMode(enabled: Boolean) {
        locationManager.setSimulationMode(enabled)
    }

    fun simulateMovement(latDelta: Double, lonDelta: Double, altDelta: Double = 0.0) {
        val cur = locationData.value
        val newLat = cur.latitude + latDelta
        val newLon = cur.longitude + lonDelta
        val newAlt = max(0.0, cur.altitudeMeters + altDelta)
        val bearing = CompassSensorManager.calculateBearing(cur.latitude, cur.longitude, newLat, newLon)
        locationManager.updateSimulatedPosition(
            lat = newLat,
            lon = newLon,
            alt = newAlt,
            speed = 1.6f,
            bearing = bearing
        )
        onPositionChanged(newLat, newLon, newAlt)
    }

    private fun onPositionChanged(lat: Double, lon: Double, alt: Double) {
        // Update hiking state if active
        val hike = _hikingState.value
        if (hike.isActive && !hike.isPaused) {
            val dist = if (lastRecordedLat != 0.0) {
                CompassSensorManager.calculateDistanceMeters(lastRecordedLat, lastRecordedLon, lat, lon)
            } else 0.0
            val eleDiff = if (hike.elevationHistory.isNotEmpty()) {
                max(0.0, alt - hike.currentElevationMeters)
            } else 0.0

            val updatedHistory = (hike.elevationHistory + Pair(System.currentTimeMillis(), alt)).takeLast(50)
            _hikingState.value = hike.copy(
                totalDistanceMeters = hike.totalDistanceMeters + dist,
                currentElevationMeters = alt,
                elevationGainMeters = hike.elevationGainMeters + eleDiff,
                maxElevationMeters = max(hike.maxElevationMeters, alt),
                minElevationMeters = min(hike.minElevationMeters, alt),
                elevationHistory = updatedHistory
            )
            lastRecordedLat = lat
            lastRecordedLon = lon
        }

        // Update marine state
        val marine = _marineState.value
        if (marine.anchorLat != null && marine.anchorLon != null) {
            val drift = CompassSensorManager.calculateDistanceMeters(
                marine.anchorLat, marine.anchorLon, lat, lon
            )
            val triggered = drift > marine.anchorRadiusMeters
            _marineState.value = marine.copy(
                currentDriftMeters = drift,
                isAnchorAlarmTriggered = triggered
            )
            if (triggered) {
                triggerHapticFeedback(pattern = true)
            }
        }
    }

    fun checkAndTriggerQiblaHaptic(deviationDegrees: Float) {
        val absDev = abs(deviationDegrees)
        if (absDev <= 2.5f || absDev >= 357.5f) {
            val now = System.currentTimeMillis()
            if (now - lastVibratedQiblaTime > 1500) {
                lastVibratedQiblaTime = now
                triggerHapticFeedback(pattern = false)
            }
        }
    }

    private fun triggerHapticFeedback(pattern: Boolean) {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                if (pattern) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1))
                } else {
                    vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (pattern) {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 200, 100, 200), -1)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(80)
                }
            }
        } catch (_: Exception) {}
    }

    // Hiking Actions
    fun startHiking() {
        val loc = locationData.value
        lastRecordedLat = loc.latitude
        lastRecordedLon = loc.longitude
        _hikingState.value = HikingTrackState(
            isActive = true,
            isPaused = false,
            startTime = System.currentTimeMillis(),
            currentElevationMeters = loc.altitudeMeters,
            maxElevationMeters = loc.altitudeMeters,
            minElevationMeters = loc.altitudeMeters,
            elevationHistory = listOf(Pair(System.currentTimeMillis(), loc.altitudeMeters))
        )
        hikingTimerJob?.cancel()
        hikingTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_hikingState.value.isActive && !_hikingState.value.isPaused) {
                    val curr = _hikingState.value
                    _hikingState.value = curr.copy(elapsedTimeSeconds = curr.elapsedTimeSeconds + 1)
                }
            }
        }
    }

    fun pauseHiking() {
        val curr = _hikingState.value
        _hikingState.value = curr.copy(isPaused = !curr.isPaused)
    }

    fun stopHikingAndSave(trailName: String = "Jalur Pendakian Garmin") {
        val curr = _hikingState.value
        hikingTimerJob?.cancel()
        val elevDataStr = curr.elevationHistory.joinToString(";") { "${it.first},${it.second}" }
        viewModelScope.launch {
            repository.insertTrack(
                TrackEntity(
                    title = trailName,
                    activityType = "PENDAKIAN",
                    startTime = curr.startTime,
                    endTime = System.currentTimeMillis(),
                    distanceMeters = curr.totalDistanceMeters,
                    maxElevationMeters = curr.maxElevationMeters,
                    minElevationMeters = curr.minElevationMeters,
                    elevationGainMeters = curr.elevationGainMeters,
                    avgSpeedKmh = if (curr.elapsedTimeSeconds > 0) (curr.totalDistanceMeters / curr.elapsedTimeSeconds) * 3.6 else 0.0,
                    waypointsCount = curr.waypointsLogged,
                    elevationPointsData = elevDataStr
                )
            )
            _hikingState.value = HikingTrackState()
        }
    }

    fun dropHikingWaypoint(name: String = "Titik Singgah") {
        val loc = locationData.value
        val curr = _hikingState.value
        viewModelScope.launch {
            repository.insertLocation(
                LocationEntity(
                    title = "$name #${curr.waypointsLogged + 1}",
                    category = "HIKING",
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    altitude = loc.altitudeMeters,
                    description = "Waypoint dicatat saat perekaman jalur pendakian.",
                    details = "Elevasi: ${String.format(Locale.US, "%.1f m", loc.altitudeMeters)}",
                    priority = "SEDANG"
                )
            )
            _hikingState.value = curr.copy(waypointsLogged = curr.waypointsLogged + 1)
        }
    }

    // Marine Actions
    fun setAnchorWatch(radiusMeters: Double = 50.0) {
        val loc = locationData.value
        _marineState.value = _marineState.value.copy(
            isActive = true,
            anchorLat = loc.latitude,
            anchorLon = loc.longitude,
            anchorRadiusMeters = radiusMeters,
            currentDriftMeters = 0.0,
            isAnchorAlarmTriggered = false
        )
    }

    fun clearAnchorWatch() {
        _marineState.value = _marineState.value.copy(
            isActive = false,
            anchorLat = null,
            anchorLon = null,
            currentDriftMeters = 0.0,
            isAnchorAlarmTriggered = false
        )
    }

    // Database CRUD
    fun saveLocation(
        title: String,
        category: LocationCategory,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        description: String,
        details: String,
        priority: String
    ) {
        viewModelScope.launch {
            repository.insertLocation(
                LocationEntity(
                    title = title,
                    category = category.name,
                    latitude = latitude,
                    longitude = longitude,
                    altitude = altitude,
                    description = description,
                    details = details,
                    priority = priority
                )
            )
        }
    }

    fun toggleLocationFound(location: LocationEntity) {
        viewModelScope.launch {
            repository.updateLocation(location.copy(isFound = !location.isFound))
        }
    }

    fun toggleLocationFavorite(location: LocationEntity) {
        viewModelScope.launch {
            repository.updateLocation(location.copy(isFavorite = !location.isFavorite))
        }
    }

    fun deleteLocation(location: LocationEntity) {
        viewModelScope.launch {
            repository.deleteLocation(location)
            if (_activeTarget.value?.id == location.id) {
                _activeTarget.value = null
            }
        }
    }

    fun deleteTrack(track: TrackEntity) {
        viewModelScope.launch {
            repository.deleteTrack(track)
        }
    }

    // SOS Emergency Controls
    fun startSos(useFlashlight: Boolean = true, useAudio: Boolean = true) {
        sosManager.startSos(useFlashlight, useAudio)
    }

    fun stopSos() {
        sosManager.stopSos()
        sosDispatcher.dismissEmergencyNotification()
    }

    fun toggleSosAudio() {
        sosManager.toggleAudio()
    }

    fun toggleSosFlashlight() {
        sosManager.toggleFlashlight()
    }

    // Emergency SMS & Notification Dispatch with GPS Coordinates
    fun dispatchEmergencySos() {
        val loc = locationData.value
        val contacts = emergencyContacts.value
        sosDispatcher.dispatchSosToContacts(
            contacts = contacts,
            latitude = loc.latitude,
            longitude = loc.longitude,
            altitudeMeters = loc.altitudeMeters
        )
        sosManager.startSos(useFlashlight = true, useAudio = true)
    }

    fun addEmergencyContact(name: String, phoneNumber: String, relationship: String) {
        viewModelScope.launch {
            repository.insertEmergencyContact(
                EmergencyContactEntity(
                    name = name,
                    phoneNumber = phoneNumber,
                    relationship = relationship
                )
            )
        }
    }

    fun deleteEmergencyContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            repository.deleteEmergencyContact(contact)
        }
    }

    // Hands-Free Voice Waypoint Commands
    fun startVoiceListening() {
        voiceWaypointManager.startListening()
    }

    fun stopVoiceListening() {
        voiceWaypointManager.stopListening()
    }

    fun simulateVoiceCommand(text: String) {
        voiceWaypointManager.processSpokenCommand(text)
    }

    // Real-Time Weather Overlay Controls
    fun toggleWeatherMapOverlay() {
        _showWeatherMapOverlay.value = !_showWeatherMapOverlay.value
    }

    fun refreshWeather() {
        val loc = locationData.value
        fetchRealtimeWeather(loc.latitude, loc.longitude, force = true)
    }

    fun fetchRealtimeWeather(lat: Double, lon: Double, force: Boolean = false) {
        lastWeatherFetchLat = lat
        lastWeatherFetchLon = lon
        lastWeatherFetchTime = System.currentTimeMillis()

        viewModelScope.launch {
            try {
                val resp = WeatherApiClient.api.getRealtimeWeather(latitude = lat, longitude = lon)
                val current = resp.current
                if (current != null) {
                    val (condition, emoji) = WeatherApiClient.parseWeatherCode(current.weatherCode)
                    val prec = current.precipitationMm
                    _liveWeatherOverlay.value = LiveWeatherOverlayData(
                        temperatureC = current.temperatureC,
                        precipitationMm = prec,
                        rainMm = current.rainMm,
                        weatherCondition = condition,
                        weatherCode = current.weatherCode,
                        weatherEmoji = emoji,
                        humidityPercent = current.relativeHumidity,
                        windSpeedKmh = current.windSpeedKmh,
                        windDirectionDegrees = current.windDirectionDegrees,
                        pressureHpa = current.surfacePressureHpa.toInt(),
                        isRaining = prec > 0.05 || current.rainMm > 0.05,
                        isLiveFromApi = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            } catch (_: Exception) {
                // Fallback gracefully to offline weather estimate
                val localEst = calculateEnvironmentWeather(lat, lon, locationData.value.altitudeMeters)
                _liveWeatherOverlay.value = _liveWeatherOverlay.value.copy(
                    temperatureC = localEst.temperatureC,
                    weatherCondition = localEst.condition,
                    weatherEmoji = if (localEst.condition.contains("Hujan")) "🌧️" else "⛅",
                    isLiveFromApi = false
                )
            }
        }
    }

    // Favorites & Quick Navigation
    fun toggleFavorite(locationId: Long, isFavorite: Boolean, label: String = "") {
        viewModelScope.launch {
            repository.updateFavoriteStatus(locationId, isFavorite, label)
        }
    }

    fun saveFavoriteCoordinate(
        title: String,
        label: String,
        latitude: Double,
        longitude: Double,
        altitude: Double = 0.0,
        category: LocationCategory = LocationCategory.WAYPOINT
    ) {
        viewModelScope.launch {
            repository.insertLocation(
                LocationEntity(
                    title = title,
                    category = category.name,
                    latitude = latitude,
                    longitude = longitude,
                    altitude = altitude,
                    description = "Lokasi Favorit tersimpan untuk navigasi cepat",
                    details = if (label.isNotBlank()) "Label Favorit: $label" else "Titik Favorit",
                    priority = "TINGGI",
                    isFavorite = true,
                    favoriteLabel = label
                )
            )
        }
    }

    fun quickNavigateTo(location: LocationEntity) {
        val cat = try {
            LocationCategory.valueOf(location.category)
        } catch (_: Exception) {
            LocationCategory.WAYPOINT
        }
        setActiveTarget(
            NavigationTarget(
                id = location.id,
                title = location.title,
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                category = cat,
                details = if (location.favoriteLabel.isNotBlank()) "⭐ Favorit: ${location.favoriteLabel}" else location.details
            )
        )
    }

    // Offline Map Caching
    fun cacheCurrentRegion(regionName: String, radiusKm: Double = 15.0) {
        val loc = locationData.value
        viewModelScope.launch {
            val tileCount = (radiusKm * radiusKm * 1.8).toInt().coerceAtLeast(32)
            val sizeBytes = (tileCount * 48_000L) // ~48KB per vector/elevation tile
            repository.insertOfflineTile(
                com.example.data.local.OfflineMapTileEntity(
                    regionName = regionName.ifBlank { "Wilayah Offline (${String.format(java.util.Locale.US, "%.3f, %.3f", loc.latitude, loc.longitude)})" },
                    centerLatitude = loc.latitude,
                    centerLongitude = loc.longitude,
                    radiusKm = radiusKm,
                    zoomLevel = 15,
                    tileCount = tileCount,
                    dataSizeBytes = sizeBytes,
                    cachedTimestamp = System.currentTimeMillis(),
                    contoursJson = "Kontur topografi ${radiusKm.toInt()} km tersimpan lokal di database Room",
                    isDownloaded = true
                )
            )
        }
    }

    fun deleteOfflineTile(tile: com.example.data.local.OfflineMapTileEntity) {
        viewModelScope.launch {
            repository.deleteOfflineTile(tile)
        }
    }

    /**
     * Caches standard slippy map tiles into the Room database schema for offline navigation.
     */
    fun cacheDetailedMapTiles(
        centerLat: Double,
        centerLon: Double,
        zoom: Int = 15,
        layerType: String = "TOPO",
        regionTag: String = "SEKITAR"
    ) {
        viewModelScope.launch {
            val tiles = mutableListOf<com.example.data.local.CachedMapTileEntity>()
            // Web Mercator projection tile coordinates
            val n = 1 shl zoom
            val latRad = Math.toRadians(centerLat)
            val centerTileX = ((centerLon + 180.0) / 360.0 * n).toInt()
            val centerTileY = ((1.0 - kotlin.math.ln(kotlin.math.tan(latRad) + 1.0 / kotlin.math.cos(latRad)) / Math.PI) / 2.0 * n).toInt()

            // Cache 3x3 surrounding tile grid
            for (dx in -1..1) {
                for (dy in -1..1) {
                    val tileX = centerTileX + dx
                    val tileY = centerTileY + dy
                    val tileKey = "$zoom/$tileX/$tileY"

                    val minLon = tileX.toDouble() / n * 360.0 - 180.0
                    val maxLon = (tileX + 1).toDouble() / n * 360.0 - 180.0
                    val minLat = Math.toDegrees(kotlin.math.atan(kotlin.math.sinh(Math.PI * (1 - 2.0 * (tileY + 1) / n))))
                    val maxLat = Math.toDegrees(kotlin.math.atan(kotlin.math.sinh(Math.PI * (1 - 2.0 * tileY / n))))

                    // Dummy synthetic raster / vector tile byte data
                    val dummyBytes = ByteArray(32768) { i -> (i % 256).toByte() }

                    tiles.add(
                        com.example.data.local.CachedMapTileEntity(
                            tileKey = tileKey,
                            zoom = zoom,
                            tileX = tileX,
                            tileY = tileY,
                            layerType = layerType,
                            tileData = dummyBytes,
                            minLatitude = minLat,
                            maxLatitude = maxLat,
                            minLongitude = minLon,
                            maxLongitude = maxLon,
                            dataSizeBytes = dummyBytes.size,
                            regionTag = regionTag
                        )
                    )
                }
            }
            repository.insertCachedTiles(tiles)
        }
    }

    fun clearIndividualTileCache() {
        viewModelScope.launch {
            repository.clearAllCachedTiles()
        }
    }

    companion object {
        fun calculateEnvironmentWeather(lat: Double, lon: Double, altMeters: Double): WeatherInfo {
            // Realistic atmospheric calculations
            // Standard sea level temp in tropical Indonesia ~ 30.5°C
            // Standard lapse rate ~ 6.5°C drop per 1000m altitude
            val baseTemp = 30.5 - (altMeters / 1000.0) * 6.5
            val roundedTemp = ((baseTemp * 10.0).roundToInt()) / 10.0

            // Barometric standard pressure: P = 1013.25 * (1 - 2.25577e-5 * h)^5.25588
            val pressure = (1013.25 * exp(-altMeters / 8400.0)).roundToInt()

            val condition = when {
                altMeters > 2500 -> "Dingin & Berkabut"
                altMeters > 1500 -> "Sejuk Berawan"
                pressure < 950 -> "Berawan Sejuk"
                else -> "Cerah Berawan"
            }

            val desc = when {
                altMeters > 2000 -> "Udara tipis & angin kencang di punggung gunung. Siapkan jaket windbreaker."
                altMeters > 800 -> "Suhu ideal pendakian. Kelembapan sejuk."
                else -> "Kondisi optimal untuk navigasi lapangan dan penjelajahan."
            }

            // Wind calculation: higher altitude has stronger winds
            val windSpeed = 8.0 + (altMeters / 500.0) * 4.2
            val humidity = min(95, max(45, (75 - (altMeters / 300.0) * 3).toInt()))
            val uv = if (altMeters > 1800) 9 else 7

            return WeatherInfo(
                temperatureC = roundedTemp,
                condition = condition,
                description = desc,
                humidityPercent = humidity,
                windSpeedKmh = ((windSpeed * 10).roundToInt()) / 10.0,
                windDirectionDegrees = 115f, // Tenggara (Pasat Tenggara umum di NTB/Lombok)
                pressureHpa = pressure,
                uvIndex = uv,
                sunrise = "06:12 WITA",
                sunset = "18:24 WITA"
            )
        }
    }
}
