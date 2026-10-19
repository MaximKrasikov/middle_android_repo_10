package ru.yandex.buggyweatherapp.model

// Был кастомный equals без hashCode - нарушен контракт equals/hashCode.
// Оставляем data class — equals/hashCode/toString генерирует компилятор.
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
)