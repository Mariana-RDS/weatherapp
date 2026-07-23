package com.weatherapp.model

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.weatherapp.api.WeatherService
import com.weatherapp.api.toForecast
import com.weatherapp.api.toWeather
import com.weatherapp.db.fb.FBDatabase
import com.weatherapp.db.fb.FBCity
import com.weatherapp.db.fb.FBUser
import com.weatherapp.db.fb.toFBCity
import com.weatherapp.db.local.LocalDatabase
import com.weatherapp.db.local.toLocalCity
import com.weatherapp.model.Forecast.Forecast
import com.weatherapp.monitor.ForecastMonitor
import com.weatherapp.ui.nav.Route

class MainViewModel(
    private val db: FBDatabase,
    private val service: WeatherService,
    private val monitor: ForecastMonitor,
    private val localDB: LocalDatabase
) : ViewModel(),
    FBDatabase.Listener {

    private val _forecast = mutableStateMapOf<String, List<Forecast>?>()
    private val _user = mutableStateOf<User?>(null)

    val user: User?
        get() = _user.value

    init {
        db.setListener(this)
    }

    fun forecast(name: String) = _forecast.getOrPut(name) {
        loadForecast(name)
        emptyList()
    }

    private fun loadForecast(name: String) {
        service.getForecast(name) { apiForecast ->
            apiForecast?.let {
                _forecast[name] = it.toForecast()
            }
        }
    }

    private var _city = mutableStateOf<String?>(null)
    private var _page = mutableStateOf<Route>(Route.Home)

    var page: Route
        get() = _page.value
        set(value) {
            _page.value = value
        }

    var city: String?
        get() = _city.value
        set(value) {
            _city.value = value
        }

    private val _cities = mutableStateMapOf<String, City>()
    val cities: List<City>
        get() = _cities.values.toList().sortedBy { it.name }

    private val _weather = mutableStateMapOf<String, Weather>()

    fun weather(name: String) = _weather.getOrPut(name) {
        loadWeather(name)
        Weather.LOADING
    }

    private fun loadWeather(name: String) {
        service.getWeather(name) { apiWeather ->
            apiWeather?.let {
                _weather[name] = it.toWeather()
                loadBitmap(name)
            }
        }
    }

    private fun loadBitmap(name: String) {
        _weather[name]?.let { weather ->
            service.getBitmap(weather.imgUrl) { bitmap ->
                _weather[name] = weather.copy(bitmap = bitmap)
            }
        }
    }

    fun addCity(name: String) {
        service.getLocation(name) { lat, lng ->
            if (lat != null && lng != null) {

                val city = City(
                    name = name,
                    location = LatLng(lat, lng)
                )

                db.add(city.toFBCity())

                localDB.insert(city.toLocalCity())
            }
        }
    }

    fun addCity(location: LatLng) {
        service.getName(location.latitude, location.longitude) { name ->
            if (name != null) {

                val city = City(
                    name = name,
                    location = location
                )

                db.add(city.toFBCity())

                localDB.insert(city.toLocalCity())
            }
        }
    }

    fun remove(city: City) {
        db.remove(city.toFBCity())

        localDB.delete(city.toLocalCity())
    }

    fun update(city: City) {
        db.update(city.toFBCity())

        localDB.update(city.toLocalCity())
    }

    fun add(name: String, location: LatLng? = null) {
        val city = City(name = name, location = location)

        db.add(city.toFBCity())

        localDB.insert(city.toLocalCity())
    }

    override fun onUserLoaded(user: FBUser) {
        _user.value = user.toUser()
    }

    override fun onUserSignOut() {
        _user.value = null
        _cities.clear()
        monitor.cancelAll()
    }

    override fun onCityAdded(city: FBCity) {
        val newCity = city.toCity()
        _cities[city.name!!] = newCity
        monitor.updateCity(newCity)
    }

    override fun onCityUpdated(city: FBCity) {
        val updatedCity = city.toCity()
        _cities.remove(city.name)
        _cities[city.name!!] = updatedCity
        monitor.updateCity(updatedCity)
    }

    override fun onCityRemoved(city: FBCity) {
        val updatedCity = city.toCity()
        _cities.remove(city.name)
        monitor.updateCity(updatedCity)
    }
}