package com.weatherapp.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.weatherapp.api.WeatherService
import com.weatherapp.api.toForecast
import com.weatherapp.api.toWeather
import com.weatherapp.model.Forecast.Forecast
import com.weatherapp.monitor.ForecastMonitor
import com.weatherapp.repo.Repository
import com.weatherapp.ui.nav.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repo: Repository,
    private val service: WeatherService,
    private val monitor: ForecastMonitor
) : ViewModel() {

    private var _city = mutableStateOf<String?>(null)
    var city: String?
        get() = _city.value
        set(value) { _city.value = value }

    private var _page = mutableStateOf<Route>(Route.Home)
    var page: Route
        get() = _page.value
        set(value) { _page.value = value }

    private val _cities: Flow<Map<String, City>> =
        repo.cities.map { list ->
            list.associateBy { it.name }
        }

    val cities = _cities.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyMap()
    )

    private val _weather = MutableStateFlow<Map<String, Weather>>(emptyMap())
    val weather = _weather.asSharedFlow()

    private val _forecast = MutableStateFlow<Map<String, List<Forecast>?>>(emptyMap())
    val forecast = _forecast.asSharedFlow()

    val user = repo.user.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        null
    )

    fun remove(city: City) {
        repo.remove(city)
        monitor.cancelCity(city)
    }

    fun update(city: City) {
        repo.update(city)
        monitor.updateCity(city)
    }

    fun addCity(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val location = service.getLocation(name)
        repo.add(City(name = name, location = location))
    }

    fun addCity(location: LatLng) = viewModelScope.launch(Dispatchers.IO) {
        val name = service.getName(location.latitude, location.longitude)
        if (name != null) {
            repo.add(City(name = name, location = location))
        }
    }

    fun loadWeather(name: String) {
        if (_weather.value[name] != null) return

        viewModelScope.launch(Dispatchers.Main) {

            _weather.update { it + (name to Weather.LOADING) }

            runCatching {
                service.getWeather(name)?.toWeather()
            }.onSuccess { result ->
                _weather.update {
                    it + (name to (result ?: Weather.ERROR))
                }
            }.onFailure {
                _weather.update {
                    it + (name to Weather.ERROR)
                }
            }
        }
    }

    fun loadForecast(name: String) {
        if (_forecast.value[name] != null) return

        viewModelScope.launch(Dispatchers.Main) {

            _forecast.update { it + (name to null) }

            runCatching {
                service.getForecast(name)?.toForecast()
            }.onSuccess { result ->
                _forecast.update {
                    it + (name to result)
                }
            }.onFailure {
                _forecast.update {
                    it + (name to null)
                }
            }
        }
    }

    fun loadBitmap(name: String) {

        val weather = _weather.value[name]

        if (weather == null ||
            weather == Weather.LOADING ||
            weather == Weather.ERROR ||
            weather.bitmap != null
        ) return

        viewModelScope.launch(Dispatchers.IO) {

            val bmp = service.getBitmap(weather.imgUrl)

            _weather.update {
                it + (name to weather.copy(bitmap = bmp))
            }
        }
    }
}