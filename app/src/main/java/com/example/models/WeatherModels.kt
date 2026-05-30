package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather?,
    val daily: DailyWeather?,
    val hourly: HourlyWeather?
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    val time: String,
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "relative_humidity_2m") val humidity: Double,
    @Json(name = "wind_speed_10m") val windSpeed: Double,
    @Json(name = "weather_code") val weatherCode: Int
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    val time: List<String>,
    @Json(name = "temperature_2m_max") val maxTemp: List<Double>,
    @Json(name = "temperature_2m_min") val minTemp: List<Double>,
    @Json(name = "weather_code") val weatherCode: List<Int>
)

@JsonClass(generateAdapter = true)
data class HourlyWeather(
    val time: List<String>,
    @Json(name = "temperature_2m") val temperature: List<Double>,
    @Json(name = "relative_humidity_2m") val humidity: List<Double>,
    @Json(name = "wind_speed_10m") val windSpeed: List<Double>,
    @Json(name = "weather_code") val weatherCode: List<Int>
)

data class CityInfo(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val province: String
)

object PakistanCities {
    val list = listOf(
        CityInfo("Karachi", 24.8607, 67.0011, "Sindh"),
        CityInfo("Lahore", 31.5204, 74.3587, "Punjab"),
        CityInfo("Islamabad", 33.6844, 73.0479, "Federal Capital"),
        CityInfo("Faisalabad", 31.4504, 73.1350, "Punjab"),
        CityInfo("Rawalpindi", 33.5984, 73.0441, "Punjab"),
        CityInfo("Peshawar", 34.0151, 71.5249, "Khyber Pakhtunkhwa"),
        CityInfo("Quetta", 30.1798, 66.9750, "Balochistan"),
        CityInfo("Multan", 30.1575, 71.5249, "Punjab"),
        CityInfo("Sialkot", 32.4972, 74.5361, "Punjab"),
        CityInfo("Hyderabad", 25.3960, 68.3578, "Sindh"),
        CityInfo("Bahawalpur", 29.3544, 71.6911, "Punjab"),
        CityInfo("Sargodha", 32.0740, 72.6861, "Punjab"),
        CityInfo("Gujranwala", 32.1877, 74.1945, "Punjab"),
        CityInfo("Sukkur", 27.7244, 68.8228, "Sindh"),
        CityInfo("Gwadar", 25.1216, 62.3254, "Balochistan"),
        CityInfo("Gilgit", 35.9208, 74.3089, "Gilgit-Baltistan"),
        CityInfo("Muzaffarabad", 34.3700, 73.4708, "Azad Kashmir"),
        CityInfo("Murree", 33.9070, 73.3943, "Punjab")
    )
}
