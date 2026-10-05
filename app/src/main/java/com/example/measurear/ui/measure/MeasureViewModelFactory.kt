package com.example.measurear.ui.measure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.measurear.data.repository.MeasurementRepository

internal class MeasureViewModelFactory(
    private val measurementRepository: MeasurementRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeasureViewModel::class.java)) {
            return MeasureViewModel(measurementRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
