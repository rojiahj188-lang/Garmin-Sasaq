package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CoordinateFormat
import com.example.sensor.LocationServiceManager
import com.example.ui.components.GarminCard
import com.example.ui.theme.DarkTacticalBackground
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed
import com.example.ui.viewmodel.GarminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: GarminViewModel,
    onBack: () -> Unit
) {
    val weather by viewModel.weatherInfo.collectAsStateWithLifecycle()
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val coordFormat by viewModel.coordinateFormat.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🌦️ CUACA & SUHU LAPANGAN",
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
                        .widthIn(max = 1500.dp)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Primary Meteorological Telemetry
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        MainWeatherConditionCard(
                            weather = weather,
                            locationData = locationData,
                            coordFormat = coordFormat
                        )
                        WindAndEnvironmentCard(weather = weather)
                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    // Right Column: Ephemeris & Field Safety Advisory
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SunEphemerisCard(weather = weather)
                        FieldSafetyAdvisoryCard(weather = weather)
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            } else {
                // Mobile Handheld Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MainWeatherConditionCard(
                        weather = weather,
                        locationData = locationData,
                        coordFormat = coordFormat
                    )
                    WindAndEnvironmentCard(weather = weather)
                    SunEphemerisCard(weather = weather)
                    FieldSafetyAdvisoryCard(weather = weather)
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun MainWeatherConditionCard(
    weather: com.example.model.WeatherInfo,
    locationData: com.example.sensor.GpsLocationData,
    coordFormat: CoordinateFormat
) {
    GarminCard(
        title = "KONDISI METEOROLOGI GPS",
        badgeText = "SINKRONISASI GPS",
        badgeColor = GarminEmerald
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "${weather.temperatureC}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "°C",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminOrange
                    )
                }
                Text(
                    text = weather.condition,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GarminCyan
                )
            }

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(GarminOrange.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = GarminOrange,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Analisis Lapangan: ${weather.description}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1),
                modifier = Modifier.padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Lokasi Stasiun GPS: ${LocationServiceManager.formatCoordinates(locationData.latitude, locationData.longitude, coordFormat)}",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = Color(0xFF94A3B8)
        )
    }
}

@Composable
fun WindAndEnvironmentCard(weather: com.example.model.WeatherInfo) {
    GarminCard(title = "ANGIN, TEKANAN & KELEMBAPAN") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Wind column
            Column(modifier = Modifier.weight(1f)) {
                Text("KECEPATAN ANGIN", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Arah Angin",
                        tint = GarminCyan,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(weather.windDirectionDegrees)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${weather.windSpeedKmh} km/h",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
                Text(
                    text = "Arah Angin (${weather.windDirectionDegrees.toInt()}°)",
                    style = MaterialTheme.typography.labelSmall,
                    color = GarminCyan
                )
            }

            // Barometer column
            Column(modifier = Modifier.weight(1f)) {
                Text("TEKANAN UDARA", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                Text(
                    text = "${weather.pressureHpa} hPa",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GarminOrange
                )
                Text(
                    text = "Barometrik Terkalibrasi",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Humidity
            Column(modifier = Modifier.weight(1f)) {
                Text("KELEMBAPAN UDARA", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                Text(
                    text = "${weather.humidityPercent}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GarminEmerald
                )
            }

            // UV Index
            Column(modifier = Modifier.weight(1f)) {
                Text("INDEKS UV", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                Text(
                    text = "${weather.uvIndex} (${if (weather.uvIndex > 7) "Tinggi" else "Sedang"})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (weather.uvIndex > 7) GarminRed else GarminAmber
                )
            }
        }
    }
}

@Composable
fun SunEphemerisCard(weather: com.example.model.WeatherInfo) {
    GarminCard(title = "EFEMERIS MATAHARI (WAKTU LOKAL)") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = GarminAmber, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("TERBIT", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(weather.sunrise, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbTwilight, contentDescription = null, tint = GarminOrange, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("TERBENAM", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Text(weather.sunset, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FieldSafetyAdvisoryCard(weather: com.example.model.WeatherInfo) {
    GarminCard(
        title = "PANDUAN KESELAMATAN & HIDRASI",
        badgeText = "STANDAR LAPANGAN",
        badgeColor = GarminCyan
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = GarminEmerald, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (weather.temperatureC > 30) "Waspada Heatstroke: Siapkan air ekstra." else "Suhu Operasional Ideal untuk Eksplorasi Luar Ruang.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = GarminCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Konsumsi hidrasi disarankan: min. 500ml per jam perjalanan aktif.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
