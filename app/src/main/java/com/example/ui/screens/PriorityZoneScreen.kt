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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocationEntity
import com.example.model.CoordinateFormat
import com.example.model.LocationCategory
import com.example.model.NavigationTarget
import com.example.sensor.CompassSensorManager
import com.example.sensor.LocationServiceManager
import com.example.ui.components.AddLocationDialog
import com.example.ui.components.GarminCard
import com.example.ui.components.StatusBadge
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
fun PriorityZoneScreen(
    viewModel: GarminViewModel,
    onBack: () -> Unit
) {
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val compassData by viewModel.compassData.collectAsStateWithLifecycle()
    val unitSystem by viewModel.unitSystem.collectAsStateWithLifecycle()
    val coordFormat by viewModel.coordinateFormat.collectAsStateWithLifecycle()

    var selectedPriorityFilter by remember { mutableStateOf("SEMUA") }
    var showAddDialog by remember { mutableStateOf(false) }

    val priorityLocations = savedLocations.filter {
        it.category == "PRIORITY_ZONE" &&
                (selectedPriorityFilter == "SEMUA" || it.priority == selectedPriorityFilter)
    }.sortedBy {
        CompassSensorManager.calculateDistanceMeters(
            locationData.latitude, locationData.longitude, it.latitude, it.longitude
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📍 PRIORITAS WILAYAH & POS DARURAT",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminRed
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GarminRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Titik Prioritas")
            }
        },
        containerColor = DarkTacticalBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 1000.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GarminCard(
                    title = "SISTEM PENCARIAN PRIORITAS GEOSPASIAL",
                    badgeText = "TERURUT BERDASARKAN JARAK",
                    badgeColor = GarminRed
                ) {
                    Text(
                        text = "Katalog titik penting operasi lapangan: Pos SAR/Evakuasi, Sumber Air Tawar, Shelter Darurat, dan Zona Bahaya Bencana.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            // Filter Chips
            item {
                Text(
                    text = "FILTER TINGKAT PRIORITAS:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf("SEMUA", "TINGGI", "SEDANG", "RENDAH")
                    items(filters) { f ->
                        val isSelected = selectedPriorityFilter == f
                        val color = when (f) {
                            "TINGGI" -> GarminRed
                            "SEDANG" -> GarminAmber
                            "RENDAH" -> GarminEmerald
                            else -> GarminOrange
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) color else Color(0xFF1E293B),
                            modifier = Modifier.clickable { selectedPriorityFilter = f }
                        ) {
                            Text(
                                text = f,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (priorityLocations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkTacticalBorder, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface)
                    ) {
                        Text(
                            text = "Tidak ada lokasi prioritas wilayah untuk filter ini. Tekan tombol '+' untuk menambahkan titik pos darurat atau sumber air.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(priorityLocations) { loc ->
                    val dist = CompassSensorManager.calculateDistanceMeters(
                        locationData.latitude, locationData.longitude, loc.latitude, loc.longitude
                    )
                    val bearing = CompassSensorManager.calculateBearing(
                        locationData.latitude, locationData.longitude, loc.latitude, loc.longitude
                    )
                    val relativeAngle = (bearing - compassData.azimuthDegrees + 360f) % 360f

                    PriorityLocationCard(
                        loc = loc,
                        distanceMeters = dist,
                        bearingDegrees = bearing,
                        relativeAngle = relativeAngle,
                        unitSystem = unitSystem,
                        coordFormat = coordFormat,
                        onNavigate = {
                            viewModel.setActiveTarget(
                                NavigationTarget(
                                    id = loc.id,
                                    title = loc.title,
                                    latitude = loc.latitude,
                                    longitude = loc.longitude,
                                    altitude = loc.altitude,
                                    category = LocationCategory.PRIORITY_ZONE,
                                    details = loc.details
                                )
                            )
                            onBack()
                        },
                        onDelete = { viewModel.deleteLocation(loc) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        if (showAddDialog) {
            AddLocationDialog(
                currentLat = locationData.latitude,
                currentLon = locationData.longitude,
                currentAlt = locationData.altitudeMeters,
                defaultCategory = LocationCategory.PRIORITY_ZONE,
                onDismiss = { showAddDialog = false },
                onSave = { title, cat, lat, lon, alt, desc, details, priority ->
                    viewModel.saveLocation(title, cat, lat, lon, alt, desc, details, priority)
                }
            )
        }
    }
}

@Composable
fun PriorityLocationCard(
    loc: LocationEntity,
    distanceMeters: Double,
    bearingDegrees: Float,
    relativeAngle: Float,
    unitSystem: com.example.model.UnitSystem,
    coordFormat: CoordinateFormat,
    onNavigate: () -> Unit,
    onDelete: () -> Unit
) {
    val isDanger = loc.details.contains("Bahaya", ignoreCase = true) || loc.title.contains("Longsor", ignoreCase = true)
    val cardBorder = if (isDanger) GarminRed else DarkTacticalBorder

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
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
                    Icon(
                        imageVector = if (isDanger) Icons.Default.Warning else Icons.Default.Emergency,
                        contentDescription = null,
                        tint = if (isDanger) GarminRed else GarminEmerald
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = loc.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                StatusBadge(priority = loc.priority)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Koordinat: ${LocationServiceManager.formatCoordinates(loc.latitude, loc.longitude, coordFormat)}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = GarminCyan
            )

            if (loc.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder)
                ) {
                    Text(
                        text = "📍 Detail: ${loc.details}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDanger) GarminRed else GarminEmerald,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (loc.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = loc.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = GarminRed,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(relativeAngle)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Jarak: ${LocationServiceManager.formatDistance(distanceMeters, unitSystem)} (${bearingDegrees.toInt()}°)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = GarminRed)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigate,
                colors = ButtonDefaults.buttonColors(containerColor = GarminRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Navigasi ke Titik Prioritas Ini", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
