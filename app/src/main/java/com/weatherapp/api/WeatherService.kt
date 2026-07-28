package com.weatherapp.api

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import com.google.android.gms.maps.model.LatLng

class WeatherService(private val context: Context) {

    private var weatherAPI: WeatherServiceAPI

    private val imageLoader = ImageLoader.Builder(context)
        .allowHardware(false)
        .build()

    init {
        val retrofitAPI = Retrofit.Builder()
            .baseUrl(WeatherServiceAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherAPI = retrofitAPI.create(WeatherServiceAPI::class.java)
    }

    suspend fun getName(lat: Double, lng: Double): String? =
        withContext(Dispatchers.IO) {
            search("$lat,$lng")?.name
        }

    suspend fun getLocation(name: String): LatLng? =
        withContext(Dispatchers.IO) {
            val loc = search(name)

            loc?.lat?.let { lat ->
                loc.lon?.let { lon ->
                    LatLng(lat, lon)
                }
            }
        }

    private fun search(query: String): APILocation? {
        val call: Call<List<APILocation>?> = weatherAPI.search(query)
        val result = call.execute().body()
        return if (!result.isNullOrEmpty()) result[0] else null
    }

    suspend fun getWeather(name: String): APICurrentWeather? =
        withContext(Dispatchers.IO) {
            val call: Call<APICurrentWeather?> = weatherAPI.weather(name)
            call.execute().body()
        }

    suspend fun getForecast(name: String): APIWeatherForecast? =
        withContext(Dispatchers.IO) {
            val call: Call<APIWeatherForecast?> = weatherAPI.forecast(name)
            call.execute().body()
        }

    suspend fun getBitmap(imgUrl: String): Bitmap? =
        withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(imgUrl)
                .allowHardware(false)
                .build()

            val response = imageLoader.execute(request)
            response.drawable?.toBitmap()
        }
}