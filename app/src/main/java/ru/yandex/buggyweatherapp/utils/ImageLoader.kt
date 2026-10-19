package ru.yandex.buggyweatherapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * утечки: loadImageSync вызывался с Main в Compose DisposableEffect;
 * HashMap ImageView удерживал View; ConcurrentHashMap bitmap рос без лимита.
 * Исправляем: только suspend + IO, LRU-кэш, без ссылок на View.
 * В UI предпочтителен Coil
 */
object ImageLoader {

    private lateinit var appContext: Context

    private val imageCache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun loadImage(url: String): Bitmap? {
        imageCache.get(url)?.let { return it }

        return try {
            withContext(Dispatchers.IO) {
                val connection = URL(url).openConnection()
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()

                val inputStream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap != null) {
                    imageCache.put(url, bitmap)
                }
                bitmap
            }
        } catch (e: Exception) {
            null
        }
    }
}
