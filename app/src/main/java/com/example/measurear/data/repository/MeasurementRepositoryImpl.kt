package com.example.measurear.data.repository

import com.example.measurear.domain.MeasurementCalculator

class MeasurementRepositoryImpl : MeasurementRepository {

    override fun formatLength(meters: Float, useCentimeters: Boolean): String {
        return MeasurementCalculator.formatLength(meters, useCentimeters)
    }
}
