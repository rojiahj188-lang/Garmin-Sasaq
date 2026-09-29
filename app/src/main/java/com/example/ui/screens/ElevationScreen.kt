package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.UnitSystem
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
import com.example.ui.viewmodel.GarminViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElevationScreen(
    viewModel: GarminViewModel,
    onBack: () -> Unit
) {
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val hikingState by viewModel.hikingState.collectAsStateWithLifecycle()
    val unitSystem by viewModel.unitSystem.collectAsStateWithLifecycle()
    val barometerHpa by viewModel.barometerHpa.collectAsStateWithLifecycle()
    val weather by viewModel.weatherInfo.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()

    val currentAlt = locationData.altitudeMeters
    val altFeet = currentAlt * 3.28084
    val pressure = barometerHpa ?: weather.pressureHpa.toFloat()

    // Terrain slope estimation based on elevation gain / horizontal travel
    val slopePercent = if (hikingState.totalDistanceMeters > 50) {
        ((hikingState.elevationGainMeters / hikingState.totalDistanceMeters) * 100).coerceIn(0.0, 100.0)
    } else 6.5

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "⛰️ ALTIMETER & PROFIL ELEVASI",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Big Altimeter Readout
            GarminCard(
                title = "GARMIN BARO-ALTIMETER",
                badgeText = "GPS + BARO",
                badgeColor = GarminCyan
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "KETINGGIAN SAAT INI",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", currentAlt),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = GarminOrange
                            )
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text(
                                text = "MDPL",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "≈ ${String.format(Locale.US, "%,.0f", altFeet)} kaki (feet) dpl",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TEKANAN UDARA",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", pressure),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GarminCyan
                        )
                        Text(
                            text = "hPa / mbar",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricCell(
                        label = "KEMIRINGAN MEDAN",
                        value = "${String.format(Locale.US, "%.1f", slopePercent)}%",
                        color = GarminAmber
                    )
                    MetricCell(
                        label = "GAIN TOTAL",
                        value = "+${String.format(Locale.US, "%.0f m", hikingState.elevationGainMeters)}",
                        color = GarminEmerald
                    )
                    MetricCell(
                        label = "KETINGGIAN MIN",
                        value = String.format(Locale.US, "%.0f m", hikingState.minElevationMeters),
                        color = Color.White
                    )
                    MetricCell(
                        label = "KETINGGIAN MAX",
                        value = String.format(Locale.US, "%.0f m", hikingState.maxElevationMeters),
                        color = GarminOrange
                    )
                }
            }

            // Advanced Interactive Real-Time Elevation Profile Graph
            RealtimeElevationProfileGraph(
                elevationPoints = hikingState.elevationHistory,
                currentElevation = currentAlt,
                minElevation = hikingState.minElevationMeters,
                maxElevation = hikingState.maxElevationMeters,
                totalDistanceMeters = hikingState.totalDistanceMeters,
                elevationGainMeters = hikingState.elevationGainMeters,
                elevationLossMeters = hikingState.elevationLossMeters
            )

            // Hands-Free Voice Waypoint Logging HUD
            VoiceCommandHud(
                voiceState = voiceState,
                onStartListening = { viewModel.startVoiceListening() },
                onStopListening = { viewModel.stopVoiceListening() },
                onSimulateCommand = { viewModel.simulateVoiceCommand(it) }
            )

            // Regional Altitude Reference Points (NTB / Rinjani)
            GarminCard(title = "TITIK REFERENSI ELEVASI LOMBOK") {
                val benchmarks = listOf(
                    Pair("Puncak Gunung Rinjani", "3.726 mdpl"),
                    Pair("Plawangan Sembalun", "2.639 mdpl"),
                    Pair("Danau Segara Anak", "2.008 mdpl"),
                    Pair("Pos 2 Tengengean", "1.500 mdpl"),
                    Pair("Perbukitan Kuripan", "145 mdpl"),
                    Pair("Garis Pantai Lombok Barat", "0 mdpl")
                )

                benchmarks.forEach { (name, alt) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = name, style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text(
                            text = alt,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GarminOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
