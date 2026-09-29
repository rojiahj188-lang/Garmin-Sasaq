package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.local.OfflineMapTileEntity
import com.example.sensor.CompassSensorManager
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineMapScreen(
    viewModel: GarminViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val offlineTiles by viewModel.offlineMapTiles.collectAsStateWithLifecycle()
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val individualTilesCount by viewModel.cachedIndividualTilesCount.collectAsStateWithLifecycle()
    val individualTilesSizeBytes by viewModel.cachedIndividualTilesSizeBytes.collectAsStateWithLifecycle()

    var showDownloadDialog by remember { mutableStateOf(false) }
    var regionInput by remember { mutableStateOf("") }
    var selectedRadiusKm by remember { mutableDoubleStateOf(15.0) }

    // Check if current location is covered by any cached offline tile
    val isCurrentLocationCached = offlineTiles.any { tile ->
        val distKm = CompassSensorManager.calculateDistanceMeters(
            locationData.latitude, locationData.longitude, tile.centerLatitude, tile.centerLongitude
        ) / 1000.0
        distKm <= tile.radiusKm
    }

    val totalSizeMb = offlineTiles.sumOf { it.dataSizeBytes } / (1024.0 * 1024.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🗺️ PENYIMPANAN PETA OFFLINE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminCyan
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
                    // Left Column: Status, Storage, Downloader & Slippy cache
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OfflineStatusBanner(
                            isCurrentLocationCached = isCurrentLocationCached,
                            totalSizeMb = totalSizeMb,
                            offlineTilesCount = offlineTiles.size
                        )

                        DownloadAreaCard(
                            regionInput = regionInput,
                            onRegionInputChange = { regionInput = it },
                            selectedRadiusKm = selectedRadiusKm,
                            onRadiusSelect = { selectedRadiusKm = it },
                            onSave = {
                                val name = regionInput.ifBlank { "Wilayah Sekitar (${selectedRadiusKm.toInt()} km)" }
                                viewModel.cacheCurrentRegion(name, selectedRadiusKm)
                                regionInput = ""
                                Toast.makeText(context, "Paket peta topografi berhasil disimpan di Room DB!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        SlippyTilesCard(
                            individualTilesCount = individualTilesCount,
                            individualTilesSizeBytes = individualTilesSizeBytes,
                            onCacheZ15 = {
                                viewModel.cacheDetailedMapTiles(
                                    locationData.latitude,
                                    locationData.longitude,
                                    zoom = 15
                                )
                                Toast.makeText(context, "9 Ubin Topografi (Z15) disimpan ke Room DB!", Toast.LENGTH_SHORT).show()
                            },
                            onClearCache = {
                                viewModel.clearIndividualTileCache()
                                Toast.makeText(context, "Cache ubin Room dibersihkan", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    // Right Column: Saved Offline Map Packages
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "DAFTAR PAKET PETA OFFLINE TERSIMPAN (${offlineTiles.size})",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = GarminCyan
                        )

                        if (offlineTiles.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkTacticalBorder, RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface)
                            ) {
                                Text(
                                    text = "Belum ada paket peta offline tersimpan. Unduh wilayah di panel sebelah kiri untuk menyimpan peta topografi offline.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            offlineTiles.forEach { tile ->
                                OfflineTileCard(tile = tile, onDelete = { viewModel.deleteOfflineTile(tile) })
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
                    item {
                        OfflineStatusBanner(
                            isCurrentLocationCached = isCurrentLocationCached,
                            totalSizeMb = totalSizeMb,
                            offlineTilesCount = offlineTiles.size
                        )
                    }

                    item {
                        DownloadAreaCard(
                            regionInput = regionInput,
                            onRegionInputChange = { regionInput = it },
                            selectedRadiusKm = selectedRadiusKm,
                            onRadiusSelect = { selectedRadiusKm = it },
                            onSave = {
                                val name = regionInput.ifBlank { "Wilayah Sekitar (${selectedRadiusKm.toInt()} km)" }
                                viewModel.cacheCurrentRegion(name, selectedRadiusKm)
                                regionInput = ""
                                Toast.makeText(context, "Paket peta topografi berhasil disimpan di Room DB!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        SlippyTilesCard(
                            individualTilesCount = individualTilesCount,
                            individualTilesSizeBytes = individualTilesSizeBytes,
                            onCacheZ15 = {
                                viewModel.cacheDetailedMapTiles(
                                    locationData.latitude,
                                    locationData.longitude,
                                    zoom = 15
                                )
                                Toast.makeText(context, "9 Ubin Topografi (Z15) disimpan ke Room DB!", Toast.LENGTH_SHORT).show()
                            },
                            onClearCache = {
                                viewModel.clearIndividualTileCache()
                                Toast.makeText(context, "Cache ubin Room dibersihkan", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        Text(
                            text = "DAFTAR PAKET PETA OFFLINE TERSIMPAN (${offlineTiles.size})",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = GarminCyan
                        )
                    }

                    items(offlineTiles) { tile ->
                        OfflineTileCard(tile = tile, onDelete = { viewModel.deleteOfflineTile(tile) })
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
fun OfflineStatusBanner(
    isCurrentLocationCached: Boolean,
    totalSizeMb: Double,
    offlineTilesCount: Int
) {
    GarminCard(
        title = "STATUS CACHE PETA ROOM DATABASE",
        badgeText = if (isCurrentLocationCached) "TERCAKUP OFFLINE" else "PERLU CACHE",
        badgeColor = if (isCurrentLocationCached) GarminEmerald else GarminAmber
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOTAL PENYIMPANAN TERSIMPAN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(Locale.US, "%.1f", totalSizeMb),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = GarminCyan
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MB",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "PAKET OFFLINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "$offlineTilesCount Wilayah",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GarminOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isCurrentLocationCached)
                "✅ Posisi GPS Anda saat ini berada dalam wilayah peta offline lokal yang tersimpan. Navigasi peta radar berfungsi optimal tanpa sinyal internet."
            else
                "⚠️ Posisi GPS Anda belum tersimpan dalam paket peta offline. Disarankan untuk mengunduh cache wilayah sekitar.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1)
        )
    }
}

@Composable
fun DownloadAreaCard(
    regionInput: String,
    onRegionInputChange: (String) -> Unit,
    selectedRadiusKm: Double,
    onRadiusSelect: (Double) -> Unit,
    onSave: () -> Unit
) {
    GarminCard(title = "UNDUH & CACHE PETA WILAYAH SEKITAR") {
        Text(
            text = "Simpan ubin peta topografi dan kontur elevasi di sekitar koordinat GPS saat ini ke database Room lokal:",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = regionInput,
            onValueChange = onRegionInputChange,
            label = { Text("Nama Wilayah / Jalur") },
            placeholder = { Text("Misal: Ekspedisi Hutan Kuripan") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GarminCyan,
                unfocusedBorderColor = DarkTacticalBorder
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Radius Jangkauan Offline: ${selectedRadiusKm.toInt()} km",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(5.0, 15.0, 25.0, 50.0).forEach { r ->
                val isSel = selectedRadiusKm == r
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) GarminCyan else Color(0xFF1E293B),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onRadiusSelect(r) }
                ) {
                    Text(
                        text = "${r.toInt()} km",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSel) Color.Black else Color.White,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(containerColor = GarminCyan),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Cache Peta Offline", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SlippyTilesCard(
    individualTilesCount: Int,
    individualTilesSizeBytes: Long,
    onCacheZ15: () -> Unit,
    onClearCache: () -> Unit
) {
    GarminCard(
        title = "DATABASE ROOM: TABEL CACHED_MAP_TILES",
        badgeText = "SLIPPY TILE (Z/X/Y)",
        badgeColor = GarminEmerald
    ) {
        Text(
            text = "Skema tabel 'cached_map_tiles' menyimpan ubin raster/vektor individu dengan koordinat Z, X, Y, bounding box latitude/longitude, dan blob data untuk rendering peta offline berkecepatan tinggi.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("TOTAL UBIN INDIVIDUAL", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                Text(
                    "$individualTilesCount Ubin",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GarminEmerald
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("UKURAN BLOB ROOM", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                Text(
                    "${individualTilesSizeBytes / 1024} KB",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GarminCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCacheZ15,
                colors = ButtonDefaults.buttonColors(containerColor = GarminEmerald),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Cache 9 Ubin (Z15)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = onClearCache,
                colors = ButtonDefaults.buttonColors(containerColor = GarminRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Bersihkan Cache", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun OfflineTileCard(
    tile: OfflineMapTileEntity,
    onDelete: () -> Unit
) {
    val sizeMb = tile.dataSizeBytes / (1024.0 * 1024.0)
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

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
                    Icon(Icons.Default.DownloadDone, contentDescription = null, tint = GarminEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tile.regionName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = GarminRed)
                }
            }

            Text(
                text = "Disimpan: ${dateFormat.format(Date(tile.cachedTimestamp))} • ${tile.tileCount} Ubin",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Radius: ${tile.radiusKm.toInt()} km (Zoom L${tile.zoomLevel})",
                    style = MaterialTheme.typography.bodySmall,
                    color = GarminAmber
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", sizeMb)} MB",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GarminCyan
                )
            }

            if (tile.contoursJson.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tile.contoursJson,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
