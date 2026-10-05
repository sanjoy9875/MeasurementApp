package com.example.measurear.ui.measure.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.example.measurear.ar.ArMeasureCallbacks
import com.example.measurear.ar.ArMeasureSurfaceView
import com.example.measurear.domain.model.ScreenPoint
import com.google.ar.core.TrackingState

@Composable
internal fun ArMeasurementViewport(
    projectedPoints: List<ScreenPoint>,
    tapFeedbackPosition: Offset?,
    isPlacementEnabled: Boolean,
    onTapFeedback: (Offset) -> Unit,
    onPlacementFailed: () -> Unit,
    onPointPlaced: (Int) -> Unit,
    onFrameUpdated: (List<ScreenPoint>, Float?) -> Unit,
    onTrackingStateChanged: (TrackingState) -> Unit,
    onCameraUnavailable: () -> Unit,
    onSurfaceViewReady: (ArMeasureSurfaceView) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isPlacementEnabledState = rememberUpdatedState(isPlacementEnabled)
    val onTapFeedbackState = rememberUpdatedState(onTapFeedback)
    val onPlacementFailedState = rememberUpdatedState(onPlacementFailed)
    val onPointPlacedState = rememberUpdatedState(onPointPlaced)
    val onFrameUpdatedState = rememberUpdatedState(onFrameUpdated)
    val onTrackingStateChangedState = rememberUpdatedState(onTrackingStateChanged)
    val onCameraUnavailableState = rememberUpdatedState(onCameraUnavailable)

    val surfaceView = remember {
        ArMeasureSurfaceView(context).apply {
            setArCallbacks(
                object : ArMeasureCallbacks {
                    override fun onTapQueued(x: Float, y: Float) = Unit

                    override fun onPlacementFailed() {
                        onPlacementFailedState.value()
                    }

                    override fun onPointPlaced(anchorCount: Int) {
                        onPointPlacedState.value(anchorCount)
                    }

                    override fun onFrameUpdated(
                        projectedPoints: List<ScreenPoint>,
                        lengthMeters: Float?,
                    ) {
                        onFrameUpdatedState.value(projectedPoints, lengthMeters)
                    }

                    override fun onTrackingStateChanged(trackingState: TrackingState) {
                        onTrackingStateChangedState.value(trackingState)
                    }

                    override fun onCameraUnavailable() {
                        onCameraUnavailableState.value()
                    }
                },
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f),
            factory = {
                onSurfaceViewReady(surfaceView)
                surfaceView
            },
            update = { view ->
                view.setPlacementEnabled(isPlacementEnabledState.value)
            },
        )
        MeasurementOverlayCanvas(
            projectedPoints = projectedPoints,
            tapFeedbackPosition = tapFeedbackPosition,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
        )
        MeasureTouchCaptureLayer(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f),
            onScreenTap = { tapOffset ->
                if (!isPlacementEnabledState.value) return@MeasureTouchCaptureLayer
                onTapFeedbackState.value(tapOffset)
                surfaceView.setPlacementEnabled(true)
                surfaceView.enqueueTap(tapOffset.x, tapOffset.y)
            },
        )
    }
}
