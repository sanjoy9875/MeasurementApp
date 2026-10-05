package com.example.measurear.ar

import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState

internal object ArHitTester {

    fun findMeasurementHit(
        frame: Frame,
        x: Float,
        y: Float,
        preferredPlane: Plane? = null,
        referenceDistanceMeters: Float? = null,
        allowInstantPlacement: Boolean = true,
    ): HitResult? {
        preferredPlane?.let { plane ->
            findHitOnPlaneInPolygon(frame, x, y, plane)?.let { return it }
        }

        findBestPlaneHitInPolygon(frame, x, y)?.let { return it }

        preferredPlane?.let { plane ->
            findHitOnPlane(frame, x, y, plane)?.let { return it }
        }

        findBestPlaneHit(frame, x, y)?.let { return it }

        if (!allowInstantPlacement) return null

        val placementDistance = referenceDistanceMeters
            ?: estimateDistanceFromNearbyPlanes(frame, x, y)
            ?: DEFAULT_INSTANT_PLACEMENT_DISTANCE_METERS

        return findInstantPlacementHit(frame, x, y, placementDistance)
    }

    private fun findBestPlaneHitInPolygon(frame: Frame, x: Float, y: Float): HitResult? {
        var bestHit: HitResult? = null
        var bestDistance = Float.MAX_VALUE

        for (hitResult in frame.hitTest(x, y)) {
            val plane = hitResult.trackable as? Plane ?: continue
            if (plane.trackingState != TrackingState.TRACKING) continue
            if (!plane.isPoseInPolygon(hitResult.hitPose)) continue
            if (hitResult.distance < bestDistance) {
                bestDistance = hitResult.distance
                bestHit = hitResult
            }
        }
        return bestHit
    }

    private fun findBestPlaneHit(frame: Frame, x: Float, y: Float): HitResult? {
        var bestHit: HitResult? = null
        var bestDistance = Float.MAX_VALUE

        for (hitResult in frame.hitTest(x, y)) {
            val plane = hitResult.trackable as? Plane ?: continue
            if (plane.trackingState != TrackingState.TRACKING) continue
            if (hitResult.distance < bestDistance) {
                bestDistance = hitResult.distance
                bestHit = hitResult
            }
        }
        return bestHit
    }

    private fun findHitOnPlaneInPolygon(
        frame: Frame,
        x: Float,
        y: Float,
        plane: Plane,
    ): HitResult? {
        if (plane.trackingState != TrackingState.TRACKING) return null

        var bestHit: HitResult? = null
        var bestDistance = Float.MAX_VALUE

        for (hitResult in frame.hitTest(x, y)) {
            if (hitResult.trackable != plane) continue
            if (!plane.isPoseInPolygon(hitResult.hitPose)) continue
            if (hitResult.distance < bestDistance) {
                bestDistance = hitResult.distance
                bestHit = hitResult
            }
        }
        return bestHit
    }

    private fun findHitOnPlane(
        frame: Frame,
        x: Float,
        y: Float,
        plane: Plane,
    ): HitResult? {
        if (plane.trackingState != TrackingState.TRACKING) return null

        var bestHit: HitResult? = null
        var bestDistance = Float.MAX_VALUE

        for (hitResult in frame.hitTest(x, y)) {
            if (hitResult.trackable != plane) continue
            if (hitResult.distance < bestDistance) {
                bestDistance = hitResult.distance
                bestHit = hitResult
            }
        }
        return bestHit
    }

    private fun estimateDistanceFromNearbyPlanes(
        frame: Frame,
        x: Float,
        y: Float,
    ): Float? {
        for (hitResult in frame.hitTest(x, y)) {
            val plane = hitResult.trackable as? Plane ?: continue
            if (plane.trackingState == TrackingState.TRACKING && hitResult.distance > 0f) {
                return hitResult.distance
            }
        }
        return null
    }

    private fun findInstantPlacementHit(
        frame: Frame,
        x: Float,
        y: Float,
        distanceMeters: Float,
    ): HitResult? {
        for (distanceCandidate in buildInstantDistanceCandidates(distanceMeters)) {
            for (hitResult in frame.hitTestInstantPlacement(x, y, distanceCandidate)) {
                return hitResult
            }
        }
        return null
    }

    private fun buildInstantDistanceCandidates(primaryDistanceMeters: Float): List<Float> {
        return listOf(
            primaryDistanceMeters,
            primaryDistanceMeters * 0.85f,
            primaryDistanceMeters * 1.15f,
            0.35f,
            0.5f,
            0.75f,
            1.0f,
        ).distinct()
    }

    private const val DEFAULT_INSTANT_PLACEMENT_DISTANCE_METERS = 0.55f
}
