package ru.yandex.buggyweatherapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.yandex.buggyweatherapp.WeatherApplication
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.repository.LocationRepository
import ru.yandex.buggyweatherapp.repository.WeatherRepository
import ru.yandex.buggyweatherapp.utils.LocationTracker

/**
 *
 * UI не держит Activity Context / свой CoroutineScope / Timer;
 * data (Repository) отделён от presentation (ViewModel);
 * ресурсы освобождаются в onCleared.
 *
 *  раньше VM создавали через WeatherViewModel() в Activity —
 * onCleared() не вызывался. Нужен by viewModels() + AndroidViewModel.
 */
class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    // хранили Activity Context → утечка Activity.
    // Метод устранения: только applicationContext.
    private val appContext = application.applicationContext

    private val weatherRepository = WeatherRepository()
    private val locationRepository = LocationRepository(appContext)

    val weatherData = MutableLiveData<WeatherData>()
    val currentLocation = MutableLiveData<Location>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()
    val cityName = MutableLiveData<String>()

    // был свой CoroutineScope(Job()) без отмены.
    // Метод устранения: viewModelScope (отменяется в onCleared автоматически).

    // Timer не отменялся и плодился при каждом initialize.
    // Метод устранения: корутина + Job вместо java.util.Timer.
    private var refreshJob: Job? = null

    // initialize() каждый раз стартовал новый таймер.
    // Метод устранения: флаг initialized.
    private var initialized = false

    fun initialize() {
        if (initialized) return
        initialized = true
        fetchCurrentLocationWeather()
        startAutoRefresh()
    }

    fun fetchCurrentLocationWeather() {
        isLoading.value = true
        error.value = null

        locationRepository.getCurrentLocation { location ->
            if (location != null) {
                currentLocation.postValue(location)

                viewModelScope.launch {
                    // Geocoder уводим с Main (suspend + Dispatchers.IO в репозитории).
                    val cityNameFromLocation = locationRepository.getCityNameFromLocation(location)
                    cityName.value = cityNameFromLocation.orEmpty()
                    getWeatherForLocation(location)
                }
            } else {
                isLoading.value = false
                error.value = "Unable to get current location"
            }
        }
    }

    fun getWeatherForLocation(location: Location) {
        isLoading.postValue(true)
        error.postValue(null)

        weatherRepository.getWeatherData(location) { data, exception ->
            // LiveData с фонового потока — только postValue (не .value).
            isLoading.postValue(false)
            if (data != null) {
                weatherData.postValue(data)
            } else {
                error.postValue(exception?.message ?: "Unknown error")
            }
        }
    }

    fun searchWeatherByCity(city: String) {
        if (city.isBlank()) {
            error.value = "City name cannot be empty"
            return
        }

        isLoading.value = true
        error.value = null

        weatherRepository.getWeatherByCity(city) { data, location, exception ->
            // callback Retrofit не на Main — раньше писали .value с фона.
            isLoading.postValue(false)

            if (data != null) {
                weatherData.postValue(data)
                cityName.postValue(data.cityName)
                // ставили Location(0.0, 0.0) — ломали refresh по координатам.
                // Метод устранения: lat/lon из ответа API (через репозиторий).
                if (location != null) {
                    currentLocation.postValue(location)
                }
            } else {
                error.postValue(exception?.message ?: "Unknown error")
            }
        }
    }

    fun formatTemperature(temp: Double): String = "${temp.toInt()}°C"

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000)
                currentLocation.value?.let { location ->
                    getWeatherForLocation(location)
                }
            }
        }
    }

    fun toggleFavorite() {
        // мутация var на том же инстансе — UI мог не перерисоваться.
        // Метод устранения: copy() → новый объект в LiveData.
        weatherData.value?.let { current ->
            weatherData.value = current.copy(isFavorite = !current.isFavorite)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // отмена refresh и остановка location-подписок.
        refreshJob?.cancel()
        locationRepository.stopLocationUpdates()
        locationRepository.stopLocationTracking()
        try {
            LocationTracker.getInstance(WeatherApplication.appContext).clearListeners()
        } catch (_: Exception) {
            // Application ещё не готов
        }
    }
}
