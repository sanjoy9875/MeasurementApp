package com.example.measurear.domain

import com.example.measurear.domain.model.WorldPoint
import kotlin.math.sqrt

object MeasurementCalculator {

    const val CENTIMETERS_PER_METER = 100f

    fun distanceMeters(start: WorldPoint, end: WorldPoint): Float {
        val deltaX = end.x - start.x
        val deltaY = end.y - start.y
        val deltaZ = end.z - start.z
        return sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
    }

    /**
     * Distance between two points projected onto a plane (for coplanar measurements).
     */
    fun distanceOnPlaneMeters(
        start: WorldPoint,
        end: WorldPoint,
        planeNormalX: Float,
        planeNormalY: Float,
        planeNormalZ: Float,
    ): Float {
        val deltaX = end.x - start.x
        val deltaY = end.y - start.y
        val deltaZ = end.z - start.z
        val dotProduct = deltaX * planeNormalX + deltaY * planeNormalY + deltaZ * planeNormalZ
        val projectedX = deltaX - dotProduct * planeNormalX
        val projectedY = deltaY - dotProduct * planeNormalY
        val projectedZ = deltaZ - dotProduct * planeNormalZ
        return sqrt(projectedX * projectedX + projectedY * projectedY + projectedZ * projectedZ)
    }

    fun formatLength(meters: Float, useCentimeters: Boolean): String {
        return if (useCentimeters) {
            val centimeters = meters * CENTIMETERS_PER_METER
            String.format("%.1f cm", centimeters)
        } else {
            String.format("%.2f m", meters)
        }
    }
}
