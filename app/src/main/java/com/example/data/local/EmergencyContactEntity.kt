package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String, // e.g. "SAR / Evakuasi", "Keluarga Inti", "Posko Lapangan", "Rekan Tim"
    val isPrimary: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
