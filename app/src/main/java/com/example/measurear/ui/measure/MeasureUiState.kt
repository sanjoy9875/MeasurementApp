package com.example.measurear.ui.measure

import com.example.measurear.domain.MeasurementMode
import com.example.measurear.domain.model.ScreenPoint
import com.example.measurear.domain.model.WorldPoint

data class MeasureUiState(
    val selectedMode: MeasurementMode = MeasurementMode.LENGTH,
    val instruction: MeasureInstruction = MeasureInstruction.DETECT_SURFACES,
    val lengthMeters: Float? = null,
    val formattedLength: String? = null,
    val placedPoints: List<WorldPoint> = emptyList(),
    val projectedPoints: List<ScreenPoint> = emptyList(),
    val canPlaceMorePoints: Boolean = true,
    val showComingSoonMessage: Boolean = false,
    val isArSupported: Boolean = false,
)
