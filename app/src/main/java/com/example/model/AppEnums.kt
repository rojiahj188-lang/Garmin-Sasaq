package com.example.model

enum class LocationCategory(val label: String, val icon: String) {
    TREASURE("Harta Karun", "💰"),
    MINERAL("Mineral Bumi", "💎"),
    HERITAGE("Benda Pusaka", "🏺"),
    PRIORITY_ZONE("Prioritas Wilayah", "📍"),
    HIKING("Pendakian", "🥾"),
    MARINE("Pelayaran", "⛵"),
    WAYPOINT("Titik Acuan", "🚩")
}

enum class PriorityLevel(val label: String) {
    TINGGI("Tinggi"),
    SEDANG("Sedang"),
    RENDAH("Rendah")
}

enum class UnitSystem(val label: String) {
    METRIC("Metrik (km, m, km/h)"),
    NAUTICAL("Nautika (nm, knots)"),
    IMPERIAL("Imperial (mi, ft, mph)")
}

enum class CoordinateFormat(val label: String) {
    DECIMAL_DEGREES("Desimal (DD.ddddd°)"),
    DEGREES_MINUTES_SECONDS("DMS (DD° MM' SS\" N/S)")
}

data class WeatherInfo(
    val temperatureC: Double,
    val condition: String,
    val description: String,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionDegrees: Float,
    val pressureHpa: Int,
    val uvIndex: Int,
    val sunrise: String,
    val sunset: String
)

data class NavigationTarget(
    val id: Long = 0,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val category: LocationCategory,
    val details: String = ""
)
