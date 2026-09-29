package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class OpenMeteoResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "elevation") val elevation: Double? = 0.0,
    @Json(name = "current") val current: CurrentWeatherDto? = null
)

data class CurrentWeatherDto(
    @Json(name = "time") val time: String? = "",
    @Json(name = "temperature_2m") val temperatureC: Double = 28.0,
    @Json(name = "relative_humidity_2m") val relativeHumidity: Int = 75,
    @Json(name = "apparent_temperature") val apparentTemperatureC: Double = 30.0,
    @Json(name = "precipitation") val precipitationMm: Double = 0.0,
    @Json(name = "rain") val rainMm: Double = 0.0,
    @Json(name = "weather_code") val weatherCode: Int = 1,
    @Json(name = "wind_speed_10m") val windSpeedKmh: Double = 10.0,
    @Json(name = "wind_direction_10m") val windDirectionDegrees: Float = 120f,
    @Json(name = "surface_pressure") val surfacePressureHpa: Double = 1012.0
)

data class LiveWeatherOverlayData(
    val temperatureC: Double,
    val precipitationMm: Double,
    val rainMm: Double,
    val weatherCondition: String,
    val weatherCode: Int,
    val weatherEmoji: String,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionDegrees: Float,
    val pressureHpa: Int,
    val isRaining: Boolean,
    val isLiveFromApi: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getRealtimeWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,weather_code,wind_speed_10m,wind_direction_10m,surface_pressure",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}

object WeatherApiClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        })
        .build()

    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApiService::class.java)
    }

    fun parseWeatherCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> Pair("Cerah", "☀️")
            1 -> Pair("Cerah Berawan", "🌤️")
            2 -> Pair("Berawan Parsial", "⛅")
            3 -> Pair("Mendung Tebal", "☁️")
            45, 48 -> Pair("Kabut Lapangan", "🌫️")
            51, 53, 55 -> Pair("Gerimis Ringan", "🌦️")
            61, 63 -> Pair("Hujan", "🌧️")
            65 -> Pair("Hujan Lebat", "🌧️")
            80, 81, 82 -> Pair("Hujan Badai Lokal", "⛈️")
            95, 96, 99 -> Pair("Badai Petir Tropis", "⚡")
            else -> Pair("Berawan", "⛅")
        }
    }
}
