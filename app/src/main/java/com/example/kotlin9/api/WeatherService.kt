package com.example.kotlin9.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class GeoLocationUser(
    val cityName: String,
    val stateCode: String,
    val countryCode: String,
)

data class WeeklyForecastResponse(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<WeatherForecast>,
    val city: City
)

data class WeatherForecast(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,
    val rain: Rain?,
    val sys: Sys,
    val dt_txt: String
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val sea_level: Int,
    val grnd_level: Int,
    val humidity: Int,
    val temp_kf: Double
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Clouds(
    val all: Int
)

data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double
)

data class Rain(
    val `3h`: Double
)

data class Sys(
    val pod: String
)

data class City(
    val id: Int,
    val name: String,
    val coord: Coord,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long
)

data class Coord(
    val lat: Double,
    val lon: Double
)

data class LocalCity(
    val name: String,
    val localNames: Map<String, String>,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String
)

interface WeatherService {
    @GET("forecast")
    fun getWeeklyForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String
    ): Call<WeeklyForecastResponse>

    @GET("direct")
    fun getCities(
        @Query("q") q: String,
        @Query("limit") limit: Int,
        @Query("appid") appid: String
    ): Call<List<LocalCity>>
}