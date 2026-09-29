package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // TREASURE, MINERAL, HERITAGE, PRIORITY_ZONE, HIKING, MARINE, WAYPOINT
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val description: String = "",
    val details: String = "", // e.g. "Emas Aluvial 4.8 g/t", "Keris Majapahit Abad 14", "Pos SAR Induk"
    val priority: String = "SEDANG", // TINGGI, SEDANG, RENDAH
    val isFound: Boolean = false,
    val isFavorite: Boolean = false,
    val favoriteLabel: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val activityType: String, // PENDAKIAN, PELAYARAN
    val startTime: Long,
    val endTime: Long = 0L,
    val distanceMeters: Double = 0.0,
    val maxElevationMeters: Double = 0.0,
    val minElevationMeters: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val waypointsCount: Int = 0,
    val notes: String = "",
    val elevationPointsData: String = ""
)
