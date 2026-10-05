package com.example.measurear.ui.measure

import com.example.measurear.data.repository.MeasurementRepository
import com.example.measurear.domain.MeasurementMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MeasureViewModelTest {

    private lateinit var viewModel: MeasureViewModel

    @Before
    fun setUp() {
        viewModel = MeasureViewModel(FakeMeasurementRepository())
        viewModel.setArSupported(isSupported = true)
    }

    @Test
    fun onPointPlaced_withTwoPoints_allowsLengthUpdateFromFrame() {
        viewModel.onPointPlaced(anchorCount = 1)
        viewModel.onPointPlaced(anchorCount = 2)
        viewModel.onFrameUpdated(emptyList(), lengthMeters = 1f)

        val state = viewModel.uiState.value

        assertEquals(1f, state.lengthMeters)
        assertEquals("100.0 cm", state.formattedLength)
        assertFalse(state.canPlaceMorePoints)
    }

    @Test
    fun resetMeasurement_clearsLengthAndPoints() {
        viewModel.onPointPlaced(anchorCount = 1)
        viewModel.onPointPlaced(anchorCount = 2)
        viewModel.onFrameUpdated(emptyList(), lengthMeters = 1f)

        viewModel.resetMeasurement()

        val state = viewModel.uiState.value
        assertNull(state.lengthMeters)
        assertTrue(state.canPlaceMorePoints)
    }

    @Test
    fun onModeSelected_unimplementedMode_showsComingSoon() {
        viewModel.onModeSelected(MeasurementMode.AREA)

        assertTrue(viewModel.uiState.value.showComingSoonMessage)
    }

    private class FakeMeasurementRepository : MeasurementRepository {
        override fun formatLength(meters: Float, useCentimeters: Boolean): String {
            return "${meters * 100f} cm"
        }
    }
}
