package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: GarminViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val unitSystem by viewModel.unitSystem.collectAsStateWithLifecycle()
    val coordFormat by viewModel.coordinateFormat.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("SEMUA") }

    val filteredLocations = savedLocations.filter {
        when (selectedFilter) {
            "SEMUA" -> true
            "FAVORIT" -> it.isFavorite
            "HARTA" -> it.category == "TREASURE"
            "MINERAL" -> it.category == "MINERAL"
            "PUSAKA" -> it.category == "HERITAGE"
            "PRIORITAS" -> it.category == "PRIORITY_ZONE"
            "PENDAKIAN" -> it.category == "HIKING"
            "PELAYARAN" -> it.category == "MARINE"
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📜 RIWAYAT & LOGBOOK LOKASI",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminOrange
                    )
                },
                actions = {
                    IconButton(onClick = {
                        val report = buildString {
                            appendLine("=== GARMIN FINDER • LOGBOOK TITIK LOKASI ===")
                            appendLine("Jelajah • Temukan • Manfaatkan")
                            appendLine("Total Titik: ${savedLocations.size}")
                            appendLine("----------------------------------------")
                            savedLocations.forEachIndexed { i, loc ->
                                appendLine("${i + 1}. [${loc.category}] ${loc.title}")
                                appendLine("   Koordinat: ${loc.latitude}, ${loc.longitude}")
                                if (loc.details.isNotBlank()) appendLine("   Detail: ${loc.details}")
                            }
                            appendLine("----------------------------------------")
                            appendLine("Dikembangkan oleh: Husni, S. Kom.")
                            appendLine("I. Beremi, Jagaraga, Kuripan, Lombok Barat")
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, report)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Bagikan Riwayat Garmin Finder"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Ekspor", tint = Color.White)
                    }
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
                    // Left Column: Summary, Category Filters, and Stats
                    Column(
                        modifier = Modifier
                            .weight(0.75f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        GarminCard(
                            title = "BASIS DATA KOORDINAT & LOGBOOK",
                            badgeText = "${savedLocations.size} TITIK",
                            badgeColor = GarminOrange
                        ) {
                            Text(
                                text = "Semua titik koordinat disimpan secara persisten di database lokal Room offline tanpa memerlukan koneksi internet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        GarminCard(title = "KATEGORI LOGBOOK") {
                            val tabs = listOf("SEMUA", "FAVORIT", "HARTA", "MINERAL", "PUSAKA", "PRIORITAS", "PENDAKIAN")
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                tabs.forEach { tab ->
                                    val isSelected = selectedFilter == tab
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) GarminOrange else Color(0xFF1E293B),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedFilter = tab }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = tab,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                            )
                                            val count = savedLocations.count {
                                                when (tab) {
                                                    "SEMUA" -> true
                                                    "FAVORIT" -> it.isFavorite
                                                    "HARTA" -> it.category == "TREASURE"
                                                    "MINERAL" -> it.category == "MINERAL"
                                                    "PUSAKA" -> it.category == "HERITAGE"
                                                    "PRIORITAS" -> it.category == "PRIORITY_ZONE"
                                                    "PENDAKIAN" -> it.category == "HIKING"
                                                    else -> true
                                                }
                                            }
                                            Text(
                                                text = "$count",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) Color.White else GarminCyan
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    // Right Column: Filtered Locations List
                    Column(
                        modifier = Modifier
                            .weight(1.25f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "DAFTAR KOORDINAT ($selectedFilter: ${filteredLocations.size})",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = GarminOrange
                        )

                        if (filteredLocations.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkTacticalBorder, RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface)
                            ) {
                                Text(
                                    text = "Tidak ada titik lokasi tersimpan dalam kategori ini.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            filteredLocations.forEach { loc ->
                                val dist = CompassSensorManager.calculateDistanceMeters(
                                    locationData.latitude, locationData.longitude, loc.latitude, loc.longitude
                                )

                                HistoryLocationCard(
                                    loc = loc,
                                    distanceMeters = dist,
                                    unitSystem = unitSystem,
                                    coordFormat = coordFormat,
                                    onCopy = {
                                        val coordStr = LocationServiceManager.formatCoordinates(loc.latitude, loc.longitude, coordFormat)
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Koordinat", coordStr))
                                        Toast.makeText(context, "Koordinat disalin: $coordStr", Toast.LENGTH_SHORT).show()
                                    },
                                    onToggleFavorite = { viewModel.toggleLocationFavorite(loc) },
                                    onNavigate = {
                                        val cat = try {
                                            LocationCategory.valueOf(loc.category)
                                        } catch (_: Exception) {
                                            LocationCategory.WAYPOINT
                                        }
                                        viewModel.setActiveTarget(
                                            NavigationTarget(
                                                id = loc.id,
                                                title = loc.title,
                                                latitude = loc.latitude,
                                                longitude = loc.longitude,
                                                altitude = loc.altitude,
                                                category = cat,
                                                details = loc.details
                                            )
                                        )
                                        onBack()
                                    },
                                    onDelete = { viewModel.deleteLocation(loc) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
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
                    // Summary Card
                    item {
                        GarminCard(
                            title = "BASIS DATA KOORDINAT & LOGBOOK",
                            badgeText = "${savedLocations.size} TERSIMPAN",
                            badgeColor = GarminOrange
                        ) {
                            Text(
                                text = "Semua titik koordinat disimpan secara persisten di penyimpanan database lokal Room offline tanpa memerlukan koneksi internet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    // Filter Tabs
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val tabs = listOf("SEMUA", "FAVORIT", "HARTA", "MINERAL", "PUSAKA", "PRIORITAS", "PENDAKIAN")
                            items(tabs) { tab ->
                                val isSelected = selectedFilter == tab
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) GarminOrange else Color(0xFF1E293B),
                                    modifier = Modifier.clickable { selectedFilter = tab }
                                ) {
                                    Text(
                                        text = tab,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (filteredLocations.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkTacticalBorder, RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface)
                            ) {
                                Text(
                                    text = "Tidak ada titik lokasi tersimpan dalam kategori ini.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(filteredLocations) { loc ->
                            val dist = CompassSensorManager.calculateDistanceMeters(
                                locationData.latitude, locationData.longitude, loc.latitude, loc.longitude
                            )

                            HistoryLocationCard(
                                loc = loc,
                                distanceMeters = dist,
                                unitSystem = unitSystem,
                                coordFormat = coordFormat,
                                onCopy = {
                                    val coordStr = LocationServiceManager.formatCoordinates(loc.latitude, loc.longitude, coordFormat)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Koordinat", coordStr))
                                    Toast.makeText(context, "Koordinat disalin: $coordStr", Toast.LENGTH_SHORT).show()
                                },
                                onToggleFavorite = { viewModel.toggleLocationFavorite(loc) },
                                onNavigate = {
                                    val cat = try {
                                        LocationCategory.valueOf(loc.category)
                                    } catch (_: Exception) {
                                        LocationCategory.WAYPOINT
                                    }
                                    viewModel.setActiveTarget(
                                        NavigationTarget(
                                            id = loc.id,
                                            title = loc.title,
                                            latitude = loc.latitude,
                                            longitude = loc.longitude,
                                            altitude = loc.altitude,
                                            category = cat,
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
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryLocationCard(
    loc: LocationEntity,
    distanceMeters: Double,
    unitSystem: com.example.model.UnitSystem,
    coordFormat: CoordinateFormat,
    onCopy: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigate: () -> Unit,
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
                Text(
                    text = loc.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                StatusBadge(priority = loc.priority)
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            Text(
                text = "${loc.category} • Disimpan ${dateFormat.format(Date(loc.timestamp))}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = LocationServiceManager.formatCoordinates(loc.latitude, loc.longitude, coordFormat),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = GarminCyan
            )

            if (loc.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = loc.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = GarminAmber
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jarak: ${LocationServiceManager.formatDistance(distanceMeters, unitSystem)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = Color(0xFF94A3B8))
                    }
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

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onNavigate,
                colors = ButtonDefaults.buttonColors(containerColor = GarminOrange),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Navigasi ke Titik Ini", fontWeight = FontWeight.Bold)
            }
        }
    }
}
