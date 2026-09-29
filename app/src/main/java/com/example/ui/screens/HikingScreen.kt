package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.sensor.LocationServiceManager
import com.example.ui.components.GarminCard
import com.example.ui.components.RealtimeElevationProfileGraph
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Tracking Dashboard Card
            item {
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

                    // Real-time Advanced Elevation profile graph
                    RealtimeElevationProfileGraph(
                        elevationPoints = hikingState.elevationHistory,
                        currentElevation = locationData.altitudeMeters,
                        minElevation = hikingState.minElevationMeters,
                        maxElevation = hikingState.maxElevationMeters,
                        totalDistanceMeters = hikingState.totalDistanceMeters,
                        elevationGainMeters = hikingState.elevationGainMeters,
                        elevationLossMeters = hikingState.elevationLossMeters
                    )

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

            // Saved Hiking Tracks List
            item {
                Text(
                    text = "RIWAYAT JALUR PENDAKIAN TERSIMPAN (${hikingTracks.size})",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = GarminOrange
                )
            }

            if (hikingTracks.isEmpty()) {
                item {
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
            } else {
                items(hikingTracks) { track ->
                    HikingTrackItem(track = track, unitSystem = unitSystem, onDelete = { viewModel.deleteTrack(track) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
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
        }
    }
}
