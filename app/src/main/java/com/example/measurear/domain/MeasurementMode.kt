package com.example.measurear.domain

enum class MeasurementMode {
    LENGTH,
    AREA,
    DIAMETER,
    HEIGHT,
}

fun MeasurementMode.isImplemented(): Boolean = this == MeasurementMode.LENGTH
