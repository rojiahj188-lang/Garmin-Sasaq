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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Navigation
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
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed
import com.example.ui.viewmodel.GarminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeritageScreen(
    viewModel: GarminViewModel,
    onBack: () -> Unit
) {
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val compassData by viewModel.compassData.collectAsStateWithLifecycle()
    val unitSystem by viewModel.unitSystem.collectAsStateWithLifecycle()
    val coordFormat by viewModel.coordinateFormat.collectAsStateWithLifecycle()

    val heritageLocations = savedLocations.filter { it.category == "HERITAGE" }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🏺 BENDA PUSAKA & CAGAR BUDAYA",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE879F9)
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
                containerColor = Color(0xFFD946EF),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Catat Titik Pusaka")
            }
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
            item {
                GarminCard(
                    title = "MODUL INVENTARISASI CAGAR BUDAYA & PUSAKA",
                    badgeText = "${heritageLocations.size} TITIK TERDATA",
                    badgeColor = Color(0xFFD946EF)
                ) {
                    Text(
                        text = "Pencatatan dan pencarian lokasi artefak peninggalan bersejarah, keris kuno, prasasti, dan situs cagar budaya leluhur berkoordinat GPS akurat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            item {
                Text(
                    text = "DAFTAR PENINGGALAN SEJARAH & PUSAKA",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFFE879F9)
                )
            }

            if (heritageLocations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkTacticalBorder, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface)
                    ) {
                        Text(
                            text = "Belum ada titik pusaka yang terdata. Tekan tombol '+' untuk mencatat temuan situs arkeologi baru.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(heritageLocations) { loc ->
                    val dist = CompassSensorManager.calculateDistanceMeters(
                        locationData.latitude, locationData.longitude, loc.latitude, loc.longitude
                    )
                    val bearing = CompassSensorManager.calculateBearing(
                        locationData.latitude, locationData.longitude, loc.latitude, loc.longitude
                    )
                    val relativeAngle = (bearing - compassData.azimuthDegrees + 360f) % 360f

                    HeritageLocationCard(
                        loc = loc,
                        distanceMeters = dist,
                        bearingDegrees = bearing,
                        relativeAngle = relativeAngle,
                        unitSystem = unitSystem,
                        coordFormat = coordFormat,
                        onToggleFavorite = { viewModel.toggleLocationFavorite(loc) },
                        onNavigate = {
                            viewModel.setActiveTarget(
                                NavigationTarget(
                                    id = loc.id,
                                    title = loc.title,
                                    latitude = loc.latitude,
                                    longitude = loc.longitude,
                                    altitude = loc.altitude,
                                    category = LocationCategory.HERITAGE,
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
                defaultCategory = LocationCategory.HERITAGE,
                onDismiss = { showAddDialog = false },
                onSave = { title, cat, lat, lon, alt, desc, details, priority ->
                    viewModel.saveLocation(title, cat, lat, lon, alt, desc, details, priority)
                }
            )
        }
    }
}

@Composable
fun HeritageLocationCard(
    loc: LocationEntity,
    distanceMeters: Double,
    bearingDegrees: Float,
    relativeAngle: Float,
    unitSystem: com.example.model.UnitSystem,
    coordFormat: CoordinateFormat,
    onToggleFavorite: () -> Unit,
    onNavigate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD946EF).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
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
                    Icon(Icons.Default.AutoStories, contentDescription = null, tint = Color(0xFFE879F9))
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
                color = GarminAmber
            )

            if (loc.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF26102F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD946EF).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "📜 Era & Konteks: ${loc.details}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF0ABFC),
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
                        tint = Color(0xFFE879F9),
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

                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (loc.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (loc.isFavorite) GarminRed else Color(0xFF64748B)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = GarminRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigate,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD946EF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lacak Situs Pusaka Ini", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
