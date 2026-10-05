package com.example.measurear.data.repository

interface MeasurementRepository {
    fun formatLength(meters: Float, useCentimeters: Boolean): String
}
