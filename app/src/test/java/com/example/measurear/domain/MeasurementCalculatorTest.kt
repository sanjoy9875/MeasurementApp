package com.example.measurear.domain

import com.example.measurear.domain.model.WorldPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementCalculatorTest {

    @Test
    fun distanceMeters_returnsEuclideanDistance() {
        val start = WorldPoint(0f, 0f, 0f)
        val end = WorldPoint(3f, 4f, 0f)

        val distance = MeasurementCalculator.distanceMeters(start, end)

        assertEquals(5f, distance, 0.0001f)
    }

    @Test
    fun distanceOnPlaneMeters_ignoresComponentAlongNormal() {
        val start = WorldPoint(0f, 0f, 0f)
        val end = WorldPoint(0.3f, 0.5f, 0f)
        val normalY = 1f

        val distance = MeasurementCalculator.distanceOnPlaneMeters(
            start = start,
            end = end,
            planeNormalX = 0f,
            planeNormalY = normalY,
            planeNormalZ = 0f,
        )

        assertEquals(0.3f, distance, 0.0001f)
    }

    @Test
    fun formatLength_centimeters_formatsWithOneDecimal() {
        val formatted = MeasurementCalculator.formatLength(0.123f, useCentimeters = true)

        assertEquals("12.3 cm", formatted)
    }

    @Test
    fun formatLength_meters_formatsWithTwoDecimals() {
        val formatted = MeasurementCalculator.formatLength(1.234f, useCentimeters = false)

        assertEquals("1.23 m", formatted)
    }
}
