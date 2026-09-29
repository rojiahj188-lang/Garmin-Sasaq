package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TrackEntity
import com.example.sensor.GpsLocationData
import com.example.sensor.LocationServiceManager
import com.example.sensor.VoiceCommandState
import com.example.ui.components.GarminCard
import com.example.ui.components.RealtimeElevationProfileGraph
import com.example.ui.components.RechartsElevationProfileView
import com.example.ui.components.VoiceCommandHud
import com.example.ui.theme.DarkTacticalBackground
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed
import com.example.ui.viewmodel.GarminViewModel
import com.example.ui.viewmodel.HikingTrackState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikingScreen(
    viewModel: GarminViewModel,
    onBack: () -> Unit
) {
    val hikingState by viewModel.hikingState.collectAsStateWithLifecycle()
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val unitSystem by viewModel.unitSystem.collectAsStateWithLifecycle()
    val savedTracks by viewModel.savedTracks.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()

    val hikingTracks = savedTracks.filter { it.activityType == "PENDAKIAN" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🥾 PENDAKIAN & TRACKING",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminOrange
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkTacticalBackground)
            )
        },
        containerColor = DarkTacticalBackground
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isDesktop = maxWidth >= 860.dp

            if (isDesktop) {
                // PC / Laptop Dual-Pane Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .widthIn(max = 1600.dp)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Live Hiking Monitor & Controls
                    Column(
                        modifier = Modifier
                            .weight(1.05f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        LiveHikingSection(
                            hikingState = hikingState,
                            locationData = locationData,
                            unitSystem = unitSystem,
                            voiceState = voiceState,
                            viewModel = viewModel
                        )
                        Spacer(modifier = Modifier.height(60.dp))
                    }

                    // Right Column: Saved Tracks & Elevation Records
                    Column(
                        modifier = Modifier
                            .weight(0.95f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SavedHikingTracksHeader(count = hikingTracks.size)
                        if (hikingTracks.isEmpty()) {
                            EmptyHikingTracksCard()
                        } else {
                            hikingTracks.forEach { track ->
                                HikingTrackItem(
                                    track = track,
                                    unitSystem = unitSystem,
                                    onDelete = { viewModel.deleteTrack(track) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            } else {
                // Mobile Handheld Layout
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        LiveHikingSection(
                            hikingState = hikingState,
                            locationData = locationData,
                            unitSystem = unitSystem,
                            voiceState = voiceState,
                            viewModel = viewModel
                        )
                    }

                    item {
                        SavedHikingTracksHeader(count = hikingTracks.size)
                    }

                    if (hikingTracks.isEmpty()) {
                        item {
                            EmptyHikingTracksCard()
                        }
                    } else {
                        items(hikingTracks) { track ->
                            HikingTrackItem(
                                track = track,
                                unitSystem = unitSystem,
                                onDelete = { viewModel.deleteTrack(track) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LiveHikingSection(
    hikingState: HikingTrackState,
    locationData: GpsLocationData,
    unitSystem: com.example.model.UnitSystem,
    voiceState: VoiceCommandState,
    viewModel: GarminViewModel
) {
    GarminCard(
        title = "MONITOR PENDAKIAN LIVE",
        badgeText = when {
            !hikingState.isActive -> "SIAP REKAM"
            hikingState.isPaused -> "DIJEDA"
            else -> "PEREKAMAN AKTIF"
        },
        badgeColor = when {
            !hikingState.isActive -> GarminCyan
            hikingState.isPaused -> GarminAmber
            else -> GarminEmerald
        }
    ) {
        // Big Stats Row: Distance & Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "TOTAL JARAK",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = LocationServiceManager.formatDistance(hikingState.totalDistanceMeters, unitSystem),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "DURASI JALUR",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
                val hrs = hikingState.elapsedTimeSeconds / 3600
                val mins = (hikingState.elapsedTimeSeconds % 3600) / 60
                val secs = hikingState.elapsedTimeSeconds % 60
                Text(
                    text = String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = GarminOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Secondary Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricCell(
                label = "ELEVASI SAAT INI",
                value = LocationServiceManager.formatElevation(locationData.altitudeMeters, unitSystem),
                color = GarminCyan
            )
            MetricCell(
                label = "GAIN ELEVASI",
                value = "+${LocationServiceManager.formatElevation(hikingState.elevationGainMeters, unitSystem)}",
                color = GarminEmerald
            )
            MetricCell(
                label = "KECEPATAN",
                value = LocationServiceManager.formatSpeed(locationData.speedMps, unitSystem),
                color = GarminAmber
            )
            MetricCell(
                label = "WAYPOINT",
                value = "${hikingState.waypointsLogged} titik",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        var activeGraphMode by remember { mutableStateOf("RECHARTS") }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (activeGraphMode == "RECHARTS") GarminEmerald else Color(0xFF1E293B),
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeGraphMode = "RECHARTS" }
            ) {
                Text(
                    text = "📊 Recharts Area Graph",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (activeGraphMode == "RECHARTS") Color.Black else Color.White,
                    modifier = Modifier.padding(vertical = 6.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (activeGraphMode == "TOPOGRAPHIC") GarminCyan else Color(0xFF1E293B),
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeGraphMode = "TOPOGRAPHIC" }
            ) {
                Text(
                    text = "📈 Topografi GPS Live",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (activeGraphMode == "TOPOGRAPHIC") Color.Black else Color.White,
                    modifier = Modifier.padding(vertical = 6.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (activeGraphMode == "RECHARTS") {
            val distanceElevPoints = remember(hikingState.elevationHistory, hikingState.totalDistanceMeters) {
                val history = hikingState.elevationHistory
                if (history.size >= 2) {
                    history.mapIndexed { idx, pt ->
                        val dist = (idx.toDouble() / (history.size - 1)) * hikingState.totalDistanceMeters
                        Pair(dist, pt.second)
                    }
                } else {
                    emptyList()
                }
            }

            RechartsElevationProfileView(
                elevationData = distanceElevPoints,
                title = "PROFIL ELEVASI JALUR AKTIF",
                totalDistanceMeters = hikingState.totalDistanceMeters,
                elevationGainMeters = hikingState.elevationGainMeters,
                elevationLossMeters = hikingState.elevationLossMeters,
                minElevationMeters = hikingState.minElevationMeters,
                maxElevationMeters = hikingState.maxElevationMeters
            )
        } else {
            RealtimeElevationProfileGraph(
                elevationPoints = hikingState.elevationHistory,
                currentElevation = locationData.altitudeMeters,
                minElevation = hikingState.minElevationMeters,
                maxElevation = hikingState.maxElevationMeters,
                totalDistanceMeters = hikingState.totalDistanceMeters,
                elevationGainMeters = hikingState.elevationGainMeters,
                elevationLossMeters = hikingState.elevationLossMeters
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hands-Free Voice Waypoint Logging during hiking
        VoiceCommandHud(
            voiceState = voiceState,
            onStartListening = { viewModel.startVoiceListening() },
            onStopListening = { viewModel.stopVoiceListening() },
            onSimulateCommand = { viewModel.simulateVoiceCommand(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!hikingState.isActive) {
                Button(
                    onClick = { viewModel.startHiking() },
                    colors = ButtonDefaults.buttonColors(containerColor = GarminOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("start_hiking_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mulai Rekam Jalur", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.pauseHiking() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (hikingState.isPaused) GarminEmerald else GarminAmber
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        if (hikingState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (hikingState.isPaused) "Lanjutkan" else "Jeda")
                }

                Button(
                    onClick = { viewModel.dropHikingWaypoint() },
                    colors = ButtonDefaults.buttonColors(containerColor = GarminCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PinDrop, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Titik Acuan", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.stopHikingAndSave() },
                    colors = ButtonDefaults.buttonColors(containerColor = GarminRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Selesai")
                }
            }
        }
    }
}

@Composable
fun SavedHikingTracksHeader(count: Int) {
    Text(
        text = "RIWAYAT JALUR PENDAKIAN TERSIMPAN ($count)",
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = GarminOrange
    )
}

@Composable
fun EmptyHikingTracksCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkTacticalBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface)
    ) {
        Text(
            text = "Belum ada jalur pendakian terekam. Tekan 'Mulai Rekam Jalur' saat melakukan pendakian untuk mencatat rute, elevasi, dan jarak secara otomatis.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun HikingTrackItem(
    track: TrackEntity,
    unitSystem: com.example.model.UnitSystem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkTacticalBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Hiking, contentDescription = null, tint = GarminEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFEF4444))
                }
            }

            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            Text(
                text = dateFormat.format(Date(track.startTime)),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Jarak", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(
                        LocationServiceManager.formatDistance(track.distanceMeters, unitSystem),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Column {
                    Text("Elevasi Gain", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(
                        "+${LocationServiceManager.formatElevation(track.elevationGainMeters, unitSystem)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminEmerald
                    )
                }
                Column {
                    Text("Puncak", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(
                        LocationServiceManager.formatElevation(track.maxElevationMeters, unitSystem),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminCyan
                    )
                }
                Column {
                    Text("Waypoints", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(
                        "${track.waypointsCount}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            var showElevationGraph by remember { mutableStateOf(false) }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (showElevationGraph) GarminEmerald.copy(alpha = 0.2f) else Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (showElevationGraph) GarminEmerald else DarkTacticalBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showElevationGraph = !showElevationGraph }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showElevationGraph) "Sembunyikan Grafik Elevasi ▲" else "Lihat Grafik Elevasi Recharts ▼",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (showElevationGraph) GarminEmerald else GarminCyan
                    )
                    Text(
                        text = "Area Chart",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            if (showElevationGraph) {
                Spacer(modifier = Modifier.height(8.dp))

                val parsedElevationPoints = remember(track.elevationPointsData) {
                    if (track.elevationPointsData.isNotBlank()) {
                        try {
                            track.elevationPointsData.split(";").mapNotNull { entry ->
                                val parts = entry.split(":")
                                if (parts.size == 2) {
                                    val d = parts[0].toDoubleOrNull() ?: 0.0
                                    val e = parts[1].toDoubleOrNull() ?: 0.0
                                    Pair(d, e)
                                } else null
                            }
                        } catch (_: Exception) {
                            emptyList()
                        }
                    } else {
                        // Generate realistic points if legacy record
                        val minE = track.minElevationMeters
                        val maxE = track.maxElevationMeters
                        val dist = track.distanceMeters
                        listOf(
                            Pair(0.0, minE),
                            Pair(dist * 0.25, minE + (maxE - minE) * 0.3),
                            Pair(dist * 0.55, minE + (maxE - minE) * 0.65),
                            Pair(dist * 0.85, minE + (maxE - minE) * 0.9),
                            Pair(dist, maxE)
                        )
                    }
                }

                RechartsElevationProfileView(
                    elevationData = parsedElevationPoints,
                    title = "GRAFIK ELEVASI: ${track.title}",
                    totalDistanceMeters = track.distanceMeters,
                    elevationGainMeters = track.elevationGainMeters,
                    minElevationMeters = track.minElevationMeters,
                    maxElevationMeters = track.maxElevationMeters
                )
            }
        }
    }
}
