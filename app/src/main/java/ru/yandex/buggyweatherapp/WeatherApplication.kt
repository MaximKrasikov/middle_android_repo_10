package ru.yandex.buggyweatherapp

import android.app.Application
import android.content.Context
import ru.yandex.buggyweatherapp.utils.ImageLoader
import ru.yandex.buggyweatherapp.utils.LocationTracker

class WeatherApplication : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // глобальный контекст — только Application, не Activity.
        appContext = applicationContext

        ImageLoader.initialize(applicationContext)
        // singleton tracker с Application Context.
        LocationTracker.getInstance(applicationContext)
    }
}