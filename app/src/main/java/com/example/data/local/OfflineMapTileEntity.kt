package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_map_tiles")
data class OfflineMapTileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val regionName: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusKm: Double,
    val zoomLevel: Int,
    val tileCount: Int,
    val dataSizeBytes: Long,
    val cachedTimestamp: Long = System.currentTimeMillis(),
    val contoursJson: String = "", // Topographic contour data points
    val isDownloaded: Boolean = true
)
