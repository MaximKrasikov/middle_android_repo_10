package ru.yandex.buggyweatherapp.api

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.yandex.buggyweatherapp.BuildConfig

interface WeatherApiService {

    companion object {
        // Ключ лежал в исходниках — берём из BuildConfig (local.properties).
        val API_KEY: String = BuildConfig.WEATHER_API_KEY

        // Был http:// — трафик шёл cleartext; используем HTTPS.
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    }

    @GET("weather")
    fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = API_KEY,
        @Query("units") units: String = "metric"
    ): Call<JsonObject>

    @GET("weather")
    fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = API_KEY,
        @Query("units") units: String = "metric"
    ): Call<JsonObject>

    @GET("forecast")
    fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = API_KEY,
        @Query("units") units: String = "metric"
    ): Call<JsonObject>
}