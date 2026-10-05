package com.example.measurear.ui.measure

import androidx.lifecycle.ViewModel
import com.example.measurear.data.repository.MeasurementRepository
import com.example.measurear.domain.MeasurementMode
import com.example.measurear.domain.isImplemented
import com.example.measurear.domain.model.ScreenPoint
import com.google.ar.core.TrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
class MeasureViewModel(
    private val measurementRepository: MeasurementRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeasureUiState())
    val uiState: StateFlow<MeasureUiState> = _uiState.asStateFlow()

    private var placedPointCount = 0
    fun onModeSelected(mode: MeasurementMode) {
        if (mode.isImplemented()) {
            _uiState.update {
                it.copy(
                    selectedMode = mode,
                    showComingSoonMessage = false,
                )
            }
            return
        }
        _uiState.update {
            it.copy(showComingSoonMessage = true)
        }
    }

    fun dismissComingSoonMessage() {
        _uiState.update { it.copy(showComingSoonMessage = false) }
    }

    fun setArSupported(isSupported: Boolean) {
        _uiState.update {
            it.copy(
                isArSupported = isSupported,
                instruction = if (isSupported) {
                    MeasureInstruction.DETECT_SURFACES
                } else {
                    MeasureInstruction.AR_UNSUPPORTED
                },
            )
        }
    }

    fun onPlacementFailed() {
        updateInstruction(MeasureInstruction.POINT_NOT_ON_SURFACE)
    }

    fun onPointPlaced(anchorCount: Int) {
        if (_uiState.value.selectedMode != MeasurementMode.LENGTH) return

        placedPointCount = anchorCount.coerceAtMost(MAX_LENGTH_POINTS)

        _uiState.update {
            it.copy(
                canPlaceMorePoints = placedPointCount < MAX_LENGTH_POINTS,
                instruction = instructionForPointCount(placedPointCount),
            )
        }
    }

    fun onFrameUpdated(
        projectedPoints: List<ScreenPoint>,
        lengthMeters: Float?,
    ) {
        if (placedPointCount == 0) return

        val formattedLength = lengthMeters?.let { meters ->
            measurementRepository.formatLength(meters, useCentimeters = true)
        }

        _uiState.update { state ->
            state.copy(
                projectedPoints = projectedPoints,
                lengthMeters = lengthMeters ?: state.lengthMeters,
                formattedLength = formattedLength ?: state.formattedLength,
            )
        }
    }

    fun onTrackingStateChanged(trackingState: TrackingState) {
        when (trackingState) {
            TrackingState.TRACKING -> {
                if (placedPointCount == 0) {
                    updateInstruction(MeasureInstruction.TAP_FIRST_POINT)
                }
            }
            TrackingState.STOPPED -> updateInstruction(MeasureInstruction.AR_SESSION_STOPPED)
            TrackingState.PAUSED -> Unit
        }
    }

    fun onCameraUnavailable() {
        updateInstruction(MeasureInstruction.AR_SESSION_STOPPED)
    }

    fun resetMeasurement() {
        placedPointCount = 0
        _uiState.update {
            it.copy(
                placedPoints = emptyList(),
                projectedPoints = emptyList(),
                lengthMeters = null,
                formattedLength = null,
                canPlaceMorePoints = true,
                instruction = MeasureInstruction.TAP_FIRST_POINT,
            )
        }
    }

    private fun instructionForPointCount(pointCount: Int): MeasureInstruction {
        return when (pointCount) {
            0 -> MeasureInstruction.TAP_FIRST_POINT
            1 -> MeasureInstruction.TAP_SECOND_POINT
            else -> MeasureInstruction.MEASUREMENT_COMPLETE
        }
    }

    private fun updateInstruction(instruction: MeasureInstruction) {
        if (_uiState.value.instruction == instruction) return
        _uiState.update { it.copy(instruction = instruction) }
    }

    companion object {
        const val MAX_LENGTH_POINTS = 2
    }
}
