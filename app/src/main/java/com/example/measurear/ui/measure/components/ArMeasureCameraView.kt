package com.example.measurear.ui.measure.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.measurear.ar.ArMeasureCallbacks
import com.example.measurear.ar.ArMeasureSurfaceView
import com.example.measurear.domain.model.ScreenPoint
import com.google.ar.core.TrackingState

@Composable
internal fun ArMeasureCameraView(
    modifier: Modifier = Modifier,
    isPlacementEnabled: Boolean,
    onTapQueued: (Float, Float) -> Unit,
    onPlacementFailed: () -> Unit,
    onPointPlaced: (Int) -> Unit,
    onFrameUpdated: (List<ScreenPoint>, Float?) -> Unit,
    onTrackingStateChanged: (TrackingState) -> Unit,
    onCameraUnavailable: () -> Unit,
    onViewAttached: (ArMeasureSurfaceView) -> Unit,
) {
    val context = LocalContext.current
    val onTapQueuedState = rememberUpdatedState(onTapQueued)
    val onPlacementFailedState = rememberUpdatedState(onPlacementFailed)
    val onPointPlacedState = rememberUpdatedState(onPointPlaced)
    val onFrameUpdatedState = rememberUpdatedState(onFrameUpdated)
    val onTrackingStateChangedState = rememberUpdatedState(onTrackingStateChanged)
    val onCameraUnavailableState = rememberUpdatedState(onCameraUnavailable)

    val surfaceView = remember {
        ArMeasureSurfaceView(context).apply {
            setArCallbacks(
                object : ArMeasureCallbacks {
                    override fun onTapQueued(x: Float, y: Float) {
                        onTapQueuedState.value(x, y)
                    }

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

    AndroidView(
        modifier = modifier,
        factory = {
            onViewAttached(surfaceView)
            surfaceView
        },
        update = { view ->
            view.setPlacementEnabled(isPlacementEnabled)
        },
    )
}
