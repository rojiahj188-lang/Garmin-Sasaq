package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocationEntity
import com.example.model.LocationCategory
import com.example.model.NavigationTarget
import com.example.model.UnitSystem
import com.example.sensor.CompassSensorManager
import com.example.sensor.LocationServiceManager
import com.example.ui.components.AddLocationDialog
import com.example.ui.components.EmergencySosDialog
import com.example.ui.components.GarminCard
import com.example.ui.components.MapLegendOverlay
import com.example.ui.components.RadarMapCanvas
import com.example.ui.components.SaveFavoriteDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.VoiceCommandHud
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.DarkTacticalSurfaceVariant
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed
import com.example.ui.viewmodel.GarminViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardMapScreen(
    viewModel: GarminViewModel,
    onNavigateToScreen: (String) -> Unit
) {
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val compassData by viewModel.compassData.collectAsStateWithLifecycle()
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val unitSystem by viewModel.unitSystem.collectAsStateWithLifecycle()
    val coordFormat by viewModel.coordinateFormat.collectAsStateWithLifecycle()
    val searchRadius by viewModel.searchRadiusMeters.collectAsStateWithLifecycle()
    val activeTarget by viewModel.activeTarget.collectAsStateWithLifecycle()
    val weather by viewModel.weatherInfo.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val liveWeatherOverlay by viewModel.liveWeatherOverlay.collectAsStateWithLifecycle()
    val showWeatherOverlay by viewModel.showWeatherMapOverlay.collectAsStateWithLifecycle()
    val favoriteLocations by viewModel.favoriteLocations.collectAsStateWithLifecycle()
    val sosState by viewModel.sosState.collectAsStateWithLifecycle()
    val sosDispatchState by viewModel.sosDispatchState.collectAsStateWithLifecycle()
    val emergencyContacts by viewModel.emergencyContacts.collectAsStateWithLifecycle()

    var selectedMapLocation by remember { mutableStateOf<LocationEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showSaveFavoriteDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showSimulationPanel by remember { mutableStateOf(false) }
    var mapCategoryFilter by remember { mutableStateOf<String?>(null) }

    val displayedLocations = remember(savedLocations, mapCategoryFilter) {
        if (mapCategoryFilter != null) {
            savedLocations.filter { it.category == mapCategoryFilter }
        } else {
            savedLocations
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Top Emergency SOS Quick Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showEmergencyDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GarminRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "SOS",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (sosState.isActive) "🚨 SOS AKTIF (PANEL)" else "🚨 SOS DARURAT & SMS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }

                Button(
                    onClick = { onNavigateToScreen("offline_maps") },
                    colors = ButtonDefaults.buttonColors(containerColor = GarminCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🗺️ PETA OFFLINE",
                        color = Color.Black,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }

            // Hands-Free Speech Waypoint Recording Bar
            VoiceCommandHud(
                voiceState = voiceState,
                onStartListening = { viewModel.startVoiceListening() },
                onStopListening = { viewModel.stopVoiceListening() },
                onSimulateCommand = { viewModel.simulateVoiceCommand(it) },
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Tactical GPS Header Panel
            GarminCard(
                title = "GARMIN FINDER • GPS TELEMETRY",
                badgeText = if (locationData.isSimulated) "MODE SIMULASI" else "GPS AKTIF",
                badgeColor = if (locationData.isSimulated) GarminAmber else GarminEmerald
            ) {
                // Top Coordinates readout
                Text(
                    text = LocationServiceManager.formatCoordinates(
                        locationData.latitude,
                        locationData.longitude,
                        coordFormat
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // GPS Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricCell(
                        label = "ELEVASI",
                        value = LocationServiceManager.formatElevation(locationData.altitudeMeters, unitSystem),
                        color = GarminCyan
                    )
                    MetricCell(
                        label = "KECEPATAN",
                        value = LocationServiceManager.formatSpeed(locationData.speedMps, unitSystem),
                        color = GarminOrange
                    )
                    MetricCell(
                        label = "KOMPAS",
                        value = "${compassData.azimuthDegrees.toInt()}°",
                        color = GarminAmber
                    )
                    MetricCell(
                        label = "AKURASI",
                        value = "±${locationData.accuracyMeters.toInt()}m",
                        color = GarminEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Navigation Target Banner (if any)
            if (activeTarget != null) {
                val target = activeTarget!!
                val targetDist = CompassSensorManager.calculateDistanceMeters(
                    locationData.latitude, locationData.longitude, target.latitude, target.longitude
                )
                val targetBearing = CompassSensorManager.calculateBearing(
                    locationData.latitude, locationData.longitude, target.latitude, target.longitude
                )
                val relativeAngle = (targetBearing - compassData.azimuthDegrees + 360f) % 360f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GarminCyan, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GarminCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Arah Target",
                                tint = GarminCyan,
                                modifier = Modifier
                                    .size(28.dp)
                                    .rotate(relativeAngle)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NAVIGASI KE TARGET:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GarminCyan
                            )
                            Text(
                                text = target.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "${LocationServiceManager.formatDistance(targetDist, unitSystem)} • Bearing: ${targetBearing.toInt()}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        IconButton(onClick = { viewModel.setActiveTarget(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Batalkan Target", tint = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Interactive Radar Map
            RadarMapCanvas(
                userLat = locationData.latitude,
                userLon = locationData.longitude,
                headingDegrees = compassData.azimuthDegrees,
                searchRadiusMeters = searchRadius,
                locations = displayedLocations,
                selectedLocation = selectedMapLocation,
                onLocationSelected = { selectedMapLocation = it },
                weatherOverlay = liveWeatherOverlay,
                showWeatherOverlay = showWeatherOverlay,
                onToggleWeather = { viewModel.toggleWeatherMapOverlay() }
            )

            // Radar Controls (Radius selector + Simulation toggle)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Radius chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(500.0, 1000.0, 2000.0, 5000.0).forEach { radius ->
                        val isSelected = searchRadius == radius
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) GarminOrange else DarkTacticalSurfaceVariant,
                            modifier = Modifier.clickable { viewModel.setSearchRadius(radius) }
                        ) {
                            Text(
                                text = LocationServiceManager.formatDistance(radius, UnitSystem.METRIC),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Simulation mode toggle
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (showSimulationPanel) GarminAmber.copy(alpha = 0.2f) else DarkTacticalSurfaceVariant,
                    border = if (showSimulationPanel) androidx.compose.foundation.BorderStroke(1.dp, GarminAmber) else null,
                    modifier = Modifier.clickable {
                        showSimulationPanel = !showSimulationPanel
                        viewModel.setSimulationMode(showSimulationPanel)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = if (showSimulationPanel) GarminAmber else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showSimulationPanel) "Simulator ON" else "Simulator",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (showSimulationPanel) GarminAmber else Color.White
                        )
                    }
                }
            }

            // Real-Time Weather Overlay & Precipitation Map Legend Bar
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (showWeatherOverlay && liveWeatherOverlay.isRaining) Color(0xFF0C243B) else DarkTacticalSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (showWeatherOverlay && liveWeatherOverlay.isRaining) GarminCyan else DarkTacticalBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = liveWeatherOverlay.weatherEmoji,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f°C", liveWeatherOverlay.temperatureC)} • ${liveWeatherOverlay.weatherCondition}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (liveWeatherOverlay.isLiveFromApi) {
                                    Surface(
                                        color = GarminEmerald.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "API LIVE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                            color = GarminEmerald,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Presipitasi: ${String.format(Locale.US, "%.1f mm", liveWeatherOverlay.precipitationMm)} • Kelembapan: ${liveWeatherOverlay.humidityPercent}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (liveWeatherOverlay.isRaining) GarminCyan else Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { viewModel.refreshWeather() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Perbarui Cuaca", tint = GarminCyan, modifier = Modifier.size(18.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (showWeatherOverlay) GarminCyan.copy(alpha = 0.25f) else Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (showWeatherOverlay) GarminCyan else DarkTacticalBorder),
                            modifier = Modifier.clickable { viewModel.toggleWeatherMapOverlay() }
                        ) {
                            Text(
                                text = if (showWeatherOverlay) "Overlay AKTIF" else "Overlay OFF",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = if (showWeatherOverlay) GarminCyan else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Toggleable Map Legend Overlay (Symbol & Icon Interpreter)
            Spacer(modifier = Modifier.height(10.dp))
            MapLegendOverlay(
                locations = savedLocations,
                selectedFilterCategory = mapCategoryFilter,
                onCategoryFilterSelected = { mapCategoryFilter = it }
            )

            // Favorites Quick Navigation Shelf (Recurring Sites)
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = GarminAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⭐ LOKASI FAVORIT & NAVIGASI CEPAT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = GarminAmber
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GarminAmber.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GarminAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { showSaveFavoriteDialog = true }
                ) {
                    Text(
                        text = "+ Tambah Favorit",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = GarminAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (favoriteLocations.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkTacticalSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📌 Belum ada lokasi favorit tersimpan. Tekan '+ Tambah Favorit' atau tandai titik di peta untuk akses navigasi cepat (Quick Nav) ke basecamp, pos, atau titik temu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(favoriteLocations) { fav ->
                        val dist = CompassSensorManager.calculateDistanceMeters(
                            locationData.latitude, locationData.longitude, fav.latitude, fav.longitude
                        )
                        val bearing = CompassSensorManager.calculateBearing(
                            locationData.latitude, locationData.longitude, fav.latitude, fav.longitude
                        )
                        val isCurrentTarget = activeTarget?.id == fav.id

                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .border(
                                    1.dp,
                                    if (isCurrentTarget) GarminCyan else GarminAmber.copy(alpha = 0.6f),
                                    RoundedCornerShape(10.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = GarminAmber.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (fav.favoriteLabel.isNotBlank()) fav.favoriteLabel else "FAVORIT",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = GarminAmber,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(fav.id, false) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = "Hapus Favorit", tint = GarminAmber, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = fav.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${LocationServiceManager.formatDistance(dist, unitSystem)} • ${bearing.toInt()}°",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GarminCyan
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.quickNavigateTo(fav) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCurrentTarget) GarminEmerald else GarminOrange
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isCurrentTarget) "✓ Navigasi Aktif" else "🚀 Navigasi Cepat",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Simulation D-Pad Panel
            AnimatedVisibility(visible = showSimulationPanel) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .border(1.dp, GarminAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "KONTROL SIMULASI PERGERAKAN LAPANGAN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GarminAmber
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // D-Pad
                    IconButton(
                        onClick = { viewModel.simulateMovement(latDelta = 0.0008, lonDelta = 0.0, altDelta = 3.0) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Utara", tint = GarminOrange)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.simulateMovement(latDelta = 0.0, lonDelta = -0.0008, altDelta = -1.0) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Barat", tint = GarminOrange)
                        }
                        IconButton(
                            onClick = {
                                // Reset to Kuripan base
                                viewModel.simulateMovement(
                                    latDelta = -locationData.latitude - 8.6500,
                                    lonDelta = -locationData.longitude + 116.1400
                                )
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Kuripan", tint = GarminCyan)
                        }
                        IconButton(
                            onClick = { viewModel.simulateMovement(latDelta = 0.0, lonDelta = 0.0008, altDelta = 1.0) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Timur", tint = GarminOrange)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.simulateMovement(latDelta = -0.0008, lonDelta = 0.0, altDelta = -3.0) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Selatan", tint = GarminOrange)
                    }
                }
            }

            // Selected Location Details Card (if clicked on radar)
            if (selectedMapLocation != null) {
                val loc = selectedMapLocation!!
                val dist = CompassSensorManager.calculateDistanceMeters(
                    locationData.latitude, locationData.longitude, loc.latitude, loc.longitude
                )
                val bearing = CompassSensorManager.calculateBearing(
                    locationData.latitude, locationData.longitude, loc.latitude, loc.longitude
                )

                Spacer(modifier = Modifier.height(12.dp))
                GarminCard(
                    title = "TITIK TERPILIH DI PETA",
                    badgeText = loc.category
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = loc.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = loc.details.ifBlank { loc.description },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Jarak: ${LocationServiceManager.formatDistance(dist, unitSystem)} • Azimuth: ${bearing.toInt()}°",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GarminCyan
                            )
                        }
                        IconButton(onClick = { selectedMapLocation = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF94A3B8))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.setActiveTarget(
                                    NavigationTarget(
                                        id = loc.id,
                                        title = loc.title,
                                        latitude = loc.latitude,
                                        longitude = loc.longitude,
                                        altitude = loc.altitude,
                                        category = try {
                                            LocationCategory.valueOf(loc.category)
                                        } catch (_: Exception) {
                                            LocationCategory.WAYPOINT
                                        },
                                        details = loc.details
                                    )
                                )
                                selectedMapLocation = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GarminOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Arahkan Navigasi", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val newFav = !loc.isFavorite
                                val label = if (newFav) (if (loc.favoriteLabel.isNotBlank()) loc.favoriteLabel else "FAVORIT") else ""
                                viewModel.toggleFavorite(loc.id, newFav, label)
                                selectedMapLocation = loc.copy(isFavorite = newFav, favoriteLabel = label)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (loc.isFavorite) GarminAmber else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = if (loc.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorit",
                                tint = if (loc.isFavorite) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (loc.isFavorite) "Favorit ✓" else "+ Favorit",
                                color = if (loc.isFavorite) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weather Quick Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToScreen("weather") }
                    .border(1.dp, DarkTacticalBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = GarminOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CUACA & SUHU LAPANGAN",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GarminOrange
                            )
                            Text(
                                text = "${weather.temperatureC}°C • ${weather.condition}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${weather.pressureHpa} hPa",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GarminCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // All Feature Modules Grid
            Text(
                text = "MODUL NAVIGASI & PENCARIAN",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = GarminOrange
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                val modules = listOf(
                    Triple("🧭 Kiblat", "qibla", "Kompas & Arah Ka'bah"),
                    Triple("🥾 Pendakian", "hiking", "Tracking & Jalur"),
                    Triple("⛵ Pelayaran", "marine", "Navigasi Nautika"),
                    Triple("⛰️ Elevasi", "elevation", "Altimeter & Profil"),
                    Triple("🌦️ Cuaca & Suhu", "weather", "Tekanan & Angin"),
                    Triple("💰 Harta Karun", "treasure", "Pencarian Cache"),
                    Triple("💎 Mineral Bumi", "mineral", "Pemetaan Geologi"),
                    Triple("🏺 Benda Pusaka", "heritage", "Peninggalan Budaya"),
                    Triple("📍 Prioritas", "priority_zone", "Pencarian Wilayah"),
                    Triple("🧭 Kompas Digital", "compass", "Orientasi Sensor 60FPS"),
                    Triple("🚨 Sinyal SOS", "sos", "Morse Visual & Audio"),
                    Triple("🗺️ Peta Offline", "offline_maps", "Cache Room Database")
                )

                modules.forEach { (title, route, subtitle) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .clickable { onNavigateToScreen(route) }
                            .border(1.dp, DarkTacticalBorder, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Floating Action Button to Add Location
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_location_fab"),
            containerColor = GarminOrange,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.AddLocation, contentDescription = "Tambah Titik Lokasi")
        }

        if (showAddDialog) {
            AddLocationDialog(
                currentLat = locationData.latitude,
                currentLon = locationData.longitude,
                currentAlt = locationData.altitudeMeters,
                onDismiss = { showAddDialog = false },
                onSave = { title, cat, lat, lon, alt, desc, details, priority ->
                    viewModel.saveLocation(title, cat, lat, lon, alt, desc, details, priority)
                }
            )
        }

        if (showSaveFavoriteDialog) {
            SaveFavoriteDialog(
                currentLat = locationData.latitude,
                currentLon = locationData.longitude,
                currentAlt = locationData.altitudeMeters,
                onDismiss = { showSaveFavoriteDialog = false },
                onSaveFavorite = { title, label, lat, lon, alt, cat ->
                    viewModel.saveFavoriteCoordinate(
                        title = title,
                        label = label,
                        latitude = lat,
                        longitude = lon,
                        altitude = alt,
                        category = cat
                    )
                }
            )
        }

        if (showEmergencyDialog) {
            EmergencySosDialog(
                currentLat = locationData.latitude,
                currentLon = locationData.longitude,
                currentAlt = locationData.altitudeMeters,
                contacts = emergencyContacts,
                dispatchState = sosDispatchState,
                isStrobeActive = sosState.isActive,
                onDismiss = { showEmergencyDialog = false },
                onSendSos = { viewModel.dispatchEmergencySos() },
                onAddContact = { name, phone, relation ->
                    viewModel.addEmergencyContact(name, phone, relation)
                },
                onDeleteContact = { viewModel.deleteEmergencyContact(it) },
                onToggleStrobe = {
                    if (sosState.isActive) viewModel.stopSos() else viewModel.startSos(true, true)
                }
            )
        }
    }
}

@Composable
fun MetricCell(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}
